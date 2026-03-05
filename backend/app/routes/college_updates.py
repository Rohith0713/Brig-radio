from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.extensions import db
from app.models.college_update import CollegeUpdate, CollegeUpdateLike, CollegeUpdateView
from app.models.user import User, UserRole
from app.middleware.auth import admin_required
from app.utils.upload import save_upload, allowed_file
from app.utils.notifications import send_topic_notification
from app.utils.analytics_helper import log_event

bp = Blueprint('college_updates', __name__, url_prefix='/api/college-updates')

@bp.route('', methods=['POST'])
@admin_required
def create_update():
    """Create a new college update (Admin only)"""
    try:
        user_id = int(get_jwt_identity())
        


        # Check for image or video file
        media_file = None
        media_type = 'IMAGE'
        
        # Try generic 'media' first, then specific 'image' or 'video'
        if 'media' in request.files:
            media_file = request.files['media']
            # Try to detect type from mimetype
            if media_file.content_type and media_file.content_type.startswith('video'):
                media_type = 'VIDEO'
        elif 'image' in request.files:
            media_file = request.files['image']
        elif 'video' in request.files:
            media_file = request.files['video']
            media_type = 'VIDEO'
        
        if not media_file:

            return jsonify({'error': 'Image or Video is mandatory'}), 400
            
        if media_file.filename == '':
            return jsonify({'error': 'No file selected'}), 400
        

            
        if not allowed_file(media_file.filename):
            return jsonify({'error': 'Invalid file type'}), 400
            
        # Check for caption
        caption = request.form.get('caption')
        if not caption:
            return jsonify({'error': 'Caption is mandatory'}), 400
            
        # Save media
        filename = save_upload(media_file)
        if not filename:
            return jsonify({'error': 'Failed to save media'}), 500
        

            
        # Create update record
        update = CollegeUpdate(
            admin_id=user_id,
            image_url=f"/uploads/{filename}",
            media_type=media_type,
            caption=caption
        )
        
        db.session.add(update)
        db.session.commit()
        
        # Send push notification
        try:
            send_topic_notification(
                topic='students',
                title='New College Update',
                body='A new college event update has been posted. Tap to view.'
            )
        except Exception as e:
            current_app.logger.warning(f"Failed to send notification: {e}")
            # Don't fail the request if notification fails
            
        log_event('POST_CREATED', user_id=user_id, role='ADMIN', metadata={'update_id': update.id, 'media_type': media_type})
            
        return jsonify({
            'message': 'College update posted successfully.',
            'update': update.to_dict()
        }), 201
        
    except Exception as e:
        import traceback
        current_app.logger.error(f"ERROR in create_update: {str(e)}")
        return jsonify({'error': f'Server error: {str(e)}'}), 500


@bp.route('', methods=['GET'])
@jwt_required(optional=True)
def get_updates():
    """Get all college updates"""
    user_id = get_jwt_identity()
    user_id = int(user_id) if user_id else None
    
    page = request.args.get('page', 1, type=int)
    limit = request.args.get('limit', 20, type=int)
    
    pagination = CollegeUpdate.query.order_by(CollegeUpdate.created_at.desc())\
        .paginate(page=page, per_page=limit, error_out=False)
        
    return jsonify({
        'updates': [update.to_dict(current_user_id=user_id) for update in pagination.items],
        'total': pagination.total,
        'page': page,
        'pages': pagination.pages
    }), 200

@bp.route('/<int:update_id>', methods=['DELETE'])
@admin_required
def delete_update(update_id):
    """Delete a college update (Admin only)"""
    update = CollegeUpdate.query.get(update_id)
    if not update:
        return jsonify({'error': 'Update not found'}), 404
        
    # Delete media file from storage
    try:
        if update.image_url:
            # image_url is like "/uploads/filename.jpg"
            import os
            relative = update.image_url.lstrip('/')
            if relative.startswith('uploads/'):
                relative = relative[len('uploads/'):]
            abs_path = os.path.join(current_app.config['UPLOAD_FOLDER'], relative)
            if os.path.exists(abs_path):
                os.remove(abs_path)
    except Exception as e:
        current_app.logger.error(f"Error deleting media file: {e}")
        
    db.session.delete(update)
    db.session.commit()
    
    return jsonify({'message': 'College update deleted successfully'}), 200

@bp.route('/<int:update_id>/like', methods=['POST'])
@jwt_required()
def toggle_like(update_id):
    """Toggle like on a college update"""
    user_id = int(get_jwt_identity())
    
    # Check if user is a student (Admin requirement: optional, but "Student Side" implies mostly students)
    # But usually anyone can like. The prompt says "Students can like or unlike". 
    # I'll check role if strictly needed, but efficient to just let any user like.
    # Requirement: "Student Side – ... Like Feature: Students can like or unlike a post"
    # I'll allow it for now.
    
    update = CollegeUpdate.query.get(update_id)
    if not update:
        return jsonify({'error': 'Update not found'}), 404
        
    existing_like = CollegeUpdateLike.query.filter_by(
        update_id=update_id,
        student_id=user_id
    ).first()
    
    if existing_like:
        db.session.delete(existing_like)
        liked = False
    else:
        new_like = CollegeUpdateLike(update_id=update_id, student_id=user_id)
        db.session.add(new_like)
        liked = True
        
    db.session.commit()
    
    log_event('LIKE_TOGGLE', user_id=user_id, metadata={'update_id': update_id, 'liked': liked})
    
    return jsonify({
        'message': 'Like updated',
        'is_liked': liked,
        'like_count': update.likes.count() # Re-query count
    }), 200

@bp.route('/<int:update_id>/view', methods=['POST'])
@jwt_required()
def record_view(update_id):
    """Record a unique view for a college update"""
    user_id = int(get_jwt_identity())
    
    update = CollegeUpdate.query.get(update_id)
    if not update:
        return jsonify({'error': 'Update not found'}), 404
        
    existing_view = CollegeUpdateView.query.filter_by(
        update_id=update_id,
        user_id=user_id
    ).first()
    
    if not existing_view:
        new_view = CollegeUpdateView(update_id=update_id, user_id=user_id)
        db.session.add(new_view)
        db.session.commit()
        
    log_event('POST_VIEWED', user_id=user_id, metadata={'update_id': update_id})
        
    return jsonify({
        'message': 'View recorded',
        'view_count': update.views.count()
    }), 200

# Comments feature removed


@bp.route('/<int:update_id>/analytics', methods=['GET'])
@admin_required
def get_update_analytics(update_id):
    """Get detailed analytics for a single college update (Admin only)"""
    update = CollegeUpdate.query.get(update_id)
    if not update:
        return jsonify({'error': 'Update not found'}), 404
        
    return jsonify({
        'id': update.id,
        'caption': update.caption,
        'created_at': update.created_at.isoformat(),
        'views': update.views.count(),
        'likes': update.likes.count(),
        # Add more details if needed, e.g., view trend
    }), 200
