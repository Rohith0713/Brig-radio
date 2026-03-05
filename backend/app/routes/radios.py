from datetime import datetime, timedelta
import os
from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.extensions import db
from app.models.radio import Radio, RadioStatus, MediaType, HostStatus
from app.models.user import User
from app.models.radio_subscription import RadioSubscription
from app.models.notification import Notification
from app.middleware.auth import admin_required
from app.utils.upload import save_upload, allowed_file
from app.utils.analytics_helper import log_event

bp = Blueprint('radios', __name__, url_prefix='/api/radios')
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

def sync_radio_statuses():
    """Helper to automatically update radio statuses based on current time"""
    now = datetime.now()
    
    # 1. Auto-Start: UPCOMING -> LIVE
    # If a radio has reached its start time but hasn't ended yet
    to_start = Radio.query.filter(
        Radio.status == RadioStatus.UPCOMING,
        Radio.start_time <= now,
        Radio.end_time > now
    ).all()
    
    for radio in to_start:
        radio.status = RadioStatus.LIVE
        # If it's a pre-recorded session (has media_url), auto-host it
        if radio.media_url:
            radio.host_status = HostStatus.HOSTING
            radio.media_type = MediaType.AUDIO
            radio.stream_started_at = radio.start_time
            
    # 2. Auto-End: LIVE/UPCOMING -> COMPLETED
    # If a radio has passed its end time
    expired = Radio.query.filter(
        Radio.end_time < now,
        Radio.status.in_([RadioStatus.LIVE, RadioStatus.UPCOMING])
    ).all()
    
    if expired:
        for radio in expired:
            radio.status = RadioStatus.COMPLETED
            if radio.host_status != HostStatus.ENDED:
                radio.host_status = HostStatus.ENDED
                
            # Cleanup audio file if it was a temporary recording/stream
            if radio.media_url and radio.media_type == MediaType.AUDIO:
                try:
                    filename = radio.media_url.split('/')[-1]
                    upload_folder = current_app.config.get('UPLOAD_FOLDER')
                    if not upload_folder:
                        upload_folder = os.path.join(current_app.root_path, 'static', 'uploads')
                    
                    file_path = os.path.join(upload_folder, filename)
                    if os.path.exists(file_path):
                        os.remove(file_path)
                    radio.media_url = None
                except Exception:
                    pass
                    
    if to_start or expired:
        try:
            db.session.commit()
        except Exception as e:
            current_app.logger.error(f"Error syncing radio statuses: {e}")
            db.session.rollback()

@bp.route('', methods=['GET'])
def get_radios():
    """Get all radios with optional filtering and search"""
    page = request.args.get('page', 1, type=int)
    limit = request.args.get('limit', 10, type=int)
    
    # Filter parameters
    status = request.args.get('status', type=str)
    category_id = request.args.get('category_id', type=int)
    search = request.args.get('search', type=str)
    date_from = request.args.get('date_from', type=str)
    date_to = request.args.get('date_to', type=str)
    sort_by = request.args.get('sort_by', 'start_time', type=str)
    sort_order = request.args.get('sort_order', 'desc', type=str)
    
    sync_radio_statuses()
    query = Radio.query
    
    # Filter by status
    if status:
        try:
            status_enum = RadioStatus[status.upper()]
            query = query.filter_by(status=status_enum)
        except KeyError:
            pass
    
    # Filter by category
    if category_id:
        query = query.filter_by(category_id=category_id)
    
    # Search by title or description
    if search:
        search_term = f'%{search}%'
        query = query.filter(
            db.or_(
                Radio.title.ilike(search_term),
                Radio.description.ilike(search_term)
            )
        )
    
    # Date range filter
    if date_from:
        try:
            from_date = datetime.fromisoformat(date_from.replace('Z', '+00:00'))
            query = query.filter(Radio.start_time >= from_date)
        except:
            pass
    
    if date_to:
        try:
            to_date = datetime.fromisoformat(date_to.replace('Z', '+00:00'))
            query = query.filter(Radio.end_time <= to_date)
        except:
            pass
    
    # Sorting
    sort_column = getattr(Radio, sort_by, Radio.start_time)
    if sort_order == 'asc':
        query = query.order_by(sort_column.asc())
    else:
        query = query.order_by(sort_column.desc())
    
    # Paginate
    pagination = query.paginate(page=page, per_page=limit, error_out=False)
    
    return jsonify({
        'radios': [radio.to_dict() for radio in pagination.items],
        'total': pagination.total,
        'page': page,
        'pages': pagination.pages
    }), 200

@bp.route('/live', methods=['GET'])
def get_live_radios():
    """Get currently live radios"""
    sync_radio_statuses()
    now = datetime.now()  # Use local time to match Android app
    radios = Radio.query.filter(
        Radio.status == RadioStatus.LIVE,
        # Remove strict start_time check so early starts work
        Radio.end_time >= now
    ).all()
    
    return jsonify([radio.to_dict() for radio in radios]), 200

@bp.route('/upcoming', methods=['GET'])
@jwt_required(optional=True)
def get_upcoming_radios():
    """Get upcoming radios"""
    sync_radio_statuses()
    user_id = get_jwt_identity()
    user_id = int(user_id) if user_id else None
    
    now = datetime.now()  # Use local time to match Android app
    radios = Radio.query.filter(
        Radio.status == RadioStatus.UPCOMING,
        # Allow radios that have started but not yet hosted (late start) to appear
        Radio.end_time > now
    ).order_by(Radio.start_time).all()
    
    # Get user subscriptions if logged in
    subscribed_ids = set()
    if user_id:
        subscriptions = RadioSubscription.query.filter_by(user_id=user_id).all()
        subscribed_ids = {sub.radio_id for sub in subscriptions}
    
    result = []
    for radio in radios:
        data = radio.to_dict()
        data['is_subscribed'] = radio.id in subscribed_ids
        result.append(data)
    
    return jsonify(result), 200

@bp.route('/missed', methods=['GET'])
def get_missed_radios():
    """Get missed/completed radios (radios that have ended)"""
    sync_radio_statuses()
    now = datetime.now()  # Use local time to match Android app
    radios = Radio.query.filter(
        Radio.end_time < now,
        Radio.status != RadioStatus.DRAFT
    ).order_by(Radio.end_time.desc()).limit(10).all()
    
    return jsonify([radio.to_dict() for radio in radios]), 200

@bp.route('/<int:radio_id>', methods=['GET'])
def get_radio(radio_id):
    """Get single radio session details"""
    sync_radio_statuses()
    radio = Radio.query.get(radio_id)
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    return jsonify(radio.to_dict()), 200

@bp.route('', methods=['POST'])
@admin_required
def create_radio():
    """Create new radio (admin only)"""
    user_id = int(get_jwt_identity())
    data = request.get_json()
    
    # Validate required fields
    required = ['title', 'start_time', 'end_time', 'category_id']
    if not all(field in data for field in required):
        return jsonify({'error': 'Missing required fields (title, start_time, end_time, category_id)'}), 400
    
    # Parse datetime strings
    try:
        start_time = datetime.fromisoformat(data['start_time'].replace('Z', '+00:00'))
        end_time = datetime.fromisoformat(data['end_time'].replace('Z', '+00:00'))
    except (ValueError, AttributeError):
        return jsonify({'error': 'Invalid datetime format'}), 400
    
    # Create radio
    radio = Radio(
        title=data['title'],
        description=data.get('description', ''),
        location=data.get('location', ''),
        media_url=data.get('media_url', ''), # Add media_url support
        start_time=start_time,
        end_time=end_time,
        category_id=data['category_id'],
        status=RadioStatus[data.get('status', 'UPCOMING').upper()] if data.get('status') else RadioStatus.UPCOMING,
        created_by=user_id
    )
    
    db.session.add(radio)
    db.session.commit()
    
    return jsonify(radio.to_dict()), 201

@bp.route('/<int:radio_id>', methods=['PUT'])
@admin_required
def update_radio(radio_id):
    """Update radio session (admin only)"""
    radio = Radio.query.get(radio_id)
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    data = request.get_json()
    
    # Update fields
    if 'title' in data:
        radio.title = data['title']
    if 'description' in data:
        radio.description = data['description']
    if 'media_url' in data:
        radio.media_url = data['media_url']
    if 'location' in data:
        radio.location = data['location']
    if 'start_time' in data:
        radio.start_time = datetime.fromisoformat(data['start_time'].replace('Z', '+00:00'))
    if 'end_time' in data:
        radio.end_time = datetime.fromisoformat(data['end_time'].replace('Z', '+00:00'))
    if 'status' in data:
        radio.status = RadioStatus[data['status'].upper()]
    if 'category_id' in data:
        radio.category_id = data['category_id']
    
    db.session.commit()
    
    return jsonify(radio.to_dict()), 200

@bp.route('/<int:radio_id>', methods=['DELETE'])
@admin_required
def delete_radio(radio_id):
    """Delete radio session (admin only)"""
    radio = Radio.query.get(radio_id)
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    # Delete associated files
    try:
        import os
        for file_attr in ['banner_image', 'media_url']:
            file_url = getattr(radio, file_attr)
            if file_url:
                # file_url is like "/uploads/filename.ext"
                relative = file_url.lstrip('/')
                if relative.startswith('uploads/'):
                    relative = relative[len('uploads/'):]
                abs_path = os.path.join(current_app.config['UPLOAD_FOLDER'], relative)
                if os.path.exists(abs_path):
                    os.remove(abs_path)
    except Exception as e:
        current_app.logger.error(f"Error deleting radio files: {e}")
        
    db.session.delete(radio)
    db.session.commit()
    
    return jsonify({'message': 'Radio session deleted successfully'}), 200

@bp.route('/<int:radio_id>/upload-banner', methods=['POST'])
@admin_required
def upload_banner(radio_id):
    """Upload radio banner image (admin only)"""
    radio = Radio.query.get(radio_id)
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    if 'banner' not in request.files:
        return jsonify({'error': 'No file provided'}), 400
    
    file = request.files['banner']
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400
    
    if not allowed_file(file.filename):
        return jsonify({'error': 'Invalid file type'}), 400
    
    # Save file
    filename = save_upload(file)
    if not filename:
        return jsonify({'error': 'Failed to save file'}), 500
    
    # Update radio
    radio.banner_image = filename
    db.session.commit()
    
    return jsonify({
        'message': 'Banner uploaded successfully',
        'banner_image': f'/uploads/{filename}',
        'banner_url': f'/uploads/{filename}'
    }), 200

@bp.route('/<int:radio_id>/upload-media', methods=['POST'])
@admin_required
def upload_media(radio_id):
    """Upload radio audio/media file (admin only)"""
    radio = Radio.query.get(radio_id)
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    if 'media' not in request.files:
        return jsonify({'error': 'No file provided'}), 400
    
    file = request.files['media']
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400
    
    if not allowed_file(file.filename):
        return jsonify({'error': 'Invalid file type'}), 400
    
    # Save file
    filename = save_upload(file)
    if not filename:
        return jsonify({'error': 'Failed to save file'}), 500
    
    # Update radio
    radio.media_url = f'/uploads/{filename}'
    db.session.commit()
    
    return jsonify({
        'message': 'Media uploaded successfully',
        'media_url': radio.media_url
    }), 200


@bp.route('/<int:radio_id>/subscribe', methods=['POST'])
@jwt_required()
def toggle_subscription(radio_id):
    """Toggle radio subscription"""
    user_id = int(get_jwt_identity())
    radio = Radio.query.get(radio_id)
    
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
        
    subscription = RadioSubscription.query.filter_by(
        user_id=user_id,
        radio_id=radio_id
    ).first()
    
    if subscription:
        db.session.delete(subscription)
        message = "Unsubscribed from radio session"
        is_subscribed = False
    else:
        subscription = RadioSubscription(user_id=user_id, radio_id=radio_id)
        db.session.add(subscription)
        message = "Subscribed to radio session"
        is_subscribed = True
        
    db.session.commit()
    
    return jsonify({
        'message': message,
        'is_subscribed': is_subscribed
    }), 200

# ==================== Live Hosting Endpoints ====================

@bp.route('/<int:radio_id>/start-hosting', methods=['POST'])
@admin_required
def start_hosting(radio_id):
    """Start hosting a radio session (admin only)"""
    user_id = int(get_jwt_identity())
    radio = Radio.query.get(radio_id)
    
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    # Check if radio is ready to be hosted (time has arrived or is upcoming)
    now = datetime.now()  # Use local time to match Android app
    if radio.start_time > now:
        # Allow hosting up to 5 minutes before start time
        time_diff = (radio.start_time - now).total_seconds()
        if time_diff > 300:  # More than 5 minutes before
            return jsonify({'error': 'Radio session cannot be hosted yet. Wait until closer to start time.'}), 400
    
    # Get media type from request
    data = request.get_json() or {}
    media_type_str = data.get('media_type', 'AUDIO').upper()
    
    try:
        media_type = MediaType[media_type_str]
    except KeyError:
        return jsonify({'error': 'Invalid media type. Use AUDIO or VIDEO.'}), 400
    
    # Validate media_url exists for audio/video sessions
    if media_type in (MediaType.AUDIO, MediaType.VIDEO) and not radio.media_url:
        return jsonify({
            'error': f'Cannot start {media_type_str} hosting: no media file uploaded. '
                     f'Please upload an audio/video file first via the media upload endpoint.'
        }), 400
    
    # Update radio
    radio.status = RadioStatus.LIVE
    radio.host_status = HostStatus.HOSTING
    radio.media_type = media_type
    radio.hosted_by = user_id
    radio.stream_started_at = datetime.now()  # Use local time
    radio.last_resumed_at = datetime.now()
    radio.accumulated_duration = 0
    
    db.session.commit()
    
    # Notify subscribers
    subscribers = RadioSubscription.query.filter_by(radio_id=radio_id).all()
    for sub in subscribers:
        notification = Notification(
            user_id=sub.user_id,
            title=f"Radio Live: {radio.title}",
            message=f"{radio.title} is now live! Join the {media_type_str.lower()} stream.",
            type="RADIO_LIVE",
            related_id=radio.id
        )
        db.session.add(notification)
    
    if subscribers:
        db.session.commit()
    
    return jsonify({
        'message': f'Radio session is now live as {media_type_str}',
        'radio': radio.to_dict()
    }), 200
    
    log_event('RADIO_START', user_id=user_id, role='ADMIN', metadata={'radio_id': radio_id, 'media_type': media_type_str})


@bp.route('/<int:radio_id>/pause-hosting', methods=['PUT'])
@admin_required
def pause_hosting(radio_id):
    """Pause a live radio (admin only)"""
    radio = Radio.query.get(radio_id)
    
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    if radio.host_status != HostStatus.HOSTING:
        return jsonify({'error': 'Radio session is not currently being hosted'}), 400
    
    now = datetime.now()
    if radio.last_resumed_at:
        elapsed = (now - radio.last_resumed_at).total_seconds()
        radio.accumulated_duration += int(elapsed)
    
    radio.host_status = HostStatus.PAUSED
    radio.last_resumed_at = None
    db.session.commit()
    
    return jsonify({
        'message': 'Radio session paused',
        'radio': radio.to_dict()
    }), 200
    
    log_event('RADIO_PAUSE', metadata={'radio_id': radio_id})


@bp.route('/<int:radio_id>/resume-hosting', methods=['PUT'])
@admin_required
def resume_hosting(radio_id):
    """Resume a paused radio (admin only)"""
    radio = Radio.query.get(radio_id)
    
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    if radio.host_status != HostStatus.PAUSED:
        return jsonify({'error': 'Radio session is not paused'}), 400
    
    radio.host_status = HostStatus.HOSTING
    radio.last_resumed_at = datetime.now()
    db.session.commit()
    
    return jsonify({
        'message': 'Radio session resumed',
        'radio': radio.to_dict()
    }), 200
    
    log_event('RADIO_RESUME', metadata={'radio_id': radio_id})


@bp.route('/<int:radio_id>/end-hosting', methods=['PUT'])
@admin_required
def end_hosting(radio_id):
    """End a live radio (admin only)"""
    radio = Radio.query.get(radio_id)
    
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    if radio.host_status not in [HostStatus.HOSTING, HostStatus.PAUSED]:
        return jsonify({'error': 'Radio session is not being hosted'}), 400
    
    now = datetime.now()
    if radio.host_status == HostStatus.HOSTING and radio.last_resumed_at:
        elapsed = (now - radio.last_resumed_at).total_seconds()
        radio.accumulated_duration += int(elapsed)
    
    radio.status = RadioStatus.COMPLETED
    radio.host_status = HostStatus.ENDED
    radio.last_resumed_at = None
    radio.end_time = now  # Use local time for immediate sync
    
    # Delete audio file if it exists
    if radio.media_url and radio.media_type == MediaType.AUDIO:
        try:
            # Construct absolute path assuming media_url is relative to static/uploads or similar
            # Based on standard Flask upload folder config
             # Assuming media_url might be like "/uploads/filename.mp3" or just "filename.mp3"
            filename = radio.media_url.split('/')[-1]
            upload_folder = current_app.config.get('UPLOAD_FOLDER')
            if not upload_folder:
                # Fallback if config not set/found
                upload_folder = os.path.join(current_app.root_path, 'static', 'uploads')
            
            file_path = os.path.join(upload_folder, filename)
            
            if os.path.exists(file_path):
                os.remove(file_path)
                # Clear the URL from DB since file is gone
                radio.media_url = None
        except Exception as e:
            current_app.logger.error(f"Error deleting audio file: {e}")
    
    db.session.commit()
    
    return jsonify({
        'message': 'Radio session ended successfully',
        'radio': radio.to_dict()
    }), 200
    
    log_event('RADIO_STOP', metadata={'radio_id': radio_id})


@bp.route('/<int:radio_id>/stream-info', methods=['GET'])
def get_stream_info(radio_id):
    """Get stream info for students to view"""
    radio = Radio.query.get(radio_id)
    
    if not radio:
        return jsonify({'error': 'Radio session not found'}), 404
    
    sync_radio_statuses()
    return jsonify({
        'radio_id': radio.id,
        'title': radio.title,
        'status': radio.status.value,
        'host_status': radio.host_status.value if radio.host_status else 'NOT_STARTED',
        'media_type': radio.media_type.value if radio.media_type else 'NONE',
        'media_url': radio.media_url,
        'is_live': radio.status == RadioStatus.LIVE and radio.host_status == HostStatus.HOSTING,
        'is_paused': radio.host_status == HostStatus.PAUSED
    }), 200
