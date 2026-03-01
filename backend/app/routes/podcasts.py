from datetime import datetime
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.extensions import db
from app.models.podcast import Podcast, PodcastStatus, HandRaise, HandRaiseStatus, podcast_viewers
from app.models.user import User, UserRole
from app.middleware.auth import admin_required

bp = Blueprint('podcasts', __name__, url_prefix='/api/podcasts')


# ==================== Podcast CRUD ====================

@bp.route('', methods=['POST'])
@jwt_required()
@admin_required
def create_podcast():
    """Create a new scheduled podcast (admin only)"""
    current_user_id = get_jwt_identity()
    data = request.get_json()
    
    # Validate required fields
    if not data.get('title'):
        return jsonify({'error': 'Podcast title is required'}), 400
    if not data.get('scheduled_start_time'):
        return jsonify({'error': 'Scheduled start time is required'}), 400
    
    try:
        scheduled_time = datetime.fromisoformat(data['scheduled_start_time'].replace('Z', '+00:00'))
    except ValueError:
        return jsonify({'error': 'Invalid date format for scheduled_start_time'}), 400
    
    podcast = Podcast(
        title=data['title'],
        description=data.get('description', ''),
        scheduled_start_time=scheduled_time,
        status=PodcastStatus.SCHEDULED,
        created_by=current_user_id
    )
    
    db.session.add(podcast)
    db.session.commit()
    
    return jsonify({
        'message': 'Podcast created successfully',
        'podcast': podcast.to_dict(include_creator=True)
    }), 201


@bp.route('/active', methods=['GET'])
@jwt_required()
def get_active_podcasts():
    """Get scheduled and live podcasts"""
    podcasts = Podcast.query.filter(
        Podcast.status.in_([PodcastStatus.SCHEDULED, PodcastStatus.LIVE])
    ).order_by(Podcast.scheduled_start_time.asc()).all()
    
    return jsonify({
        'podcasts': [p.to_dict(include_creator=True) for p in podcasts]
    })


@bp.route('/live', methods=['GET'])
@jwt_required()
def get_live_podcast():
    """Get the currently live podcast (if any)"""
    podcast = Podcast.query.filter_by(status=PodcastStatus.LIVE).first()
    
    if not podcast:
        return jsonify({'podcast': None, 'message': 'No live podcast at the moment'})
    
    return jsonify({
        'podcast': podcast.to_dict(include_creator=True)
    })


@bp.route('/<int:podcast_id>', methods=['GET'])
@jwt_required()
def get_podcast(podcast_id):
    """Get podcast details"""
    podcast = Podcast.query.get_or_404(podcast_id)
    return jsonify({'podcast': podcast.to_dict(include_creator=True)})


@bp.route('/<int:podcast_id>', methods=['DELETE'])
@jwt_required()
@admin_required
def delete_podcast(podcast_id):
    """Delete a scheduled podcast (admin only)"""
    podcast = Podcast.query.get_or_404(podcast_id)
    
    if podcast.status == PodcastStatus.LIVE:
        return jsonify({'error': 'Cannot delete a live podcast. End it first.'}), 400
    
    db.session.delete(podcast)
    db.session.commit()
    
    return jsonify({'message': 'Podcast deleted successfully'})


# ==================== Podcast Control (Admin) ====================

@bp.route('/<int:podcast_id>/go-live', methods=['POST'])
@jwt_required()
@admin_required
def go_live(podcast_id):
    """Start a podcast (make it live) - admin only"""
    # Check if another podcast is already live
    existing_live = Podcast.query.filter_by(status=PodcastStatus.LIVE).first()
    if existing_live:
        return jsonify({
            'error': 'Another podcast is already live',
            'live_podcast': existing_live.to_dict()
        }), 400
    
    podcast = Podcast.query.get_or_404(podcast_id)
    
    if podcast.status == PodcastStatus.LIVE:
        return jsonify({'error': 'Podcast is already live'}), 400
    
    if podcast.status == PodcastStatus.ENDED:
        return jsonify({'error': 'Cannot restart an ended podcast'}), 400
    
    podcast.status = PodcastStatus.LIVE
    podcast.started_at = datetime.now()
    db.session.commit()
    
    return jsonify({
        'message': 'Podcast is now live!',
        'podcast': podcast.to_dict(include_creator=True)
    })


@bp.route('/<int:podcast_id>/end', methods=['POST'])
@jwt_required()
@admin_required
def end_podcast(podcast_id):
    """End a live podcast - admin only"""
    podcast = Podcast.query.get_or_404(podcast_id)
    
    if podcast.status != PodcastStatus.LIVE:
        return jsonify({'error': 'Podcast is not live'}), 400
    
    # Clear all viewers
    podcast.viewers = []
    
    # Clear pending hand raises
    HandRaise.query.filter_by(
        podcast_id=podcast_id,
        status=HandRaiseStatus.PENDING
    ).update({'status': HandRaiseStatus.IGNORED, 'responded_at': datetime.now()})
    
    podcast.status = PodcastStatus.ENDED
    podcast.ended_at = datetime.now()
    db.session.commit()
    
    return jsonify({
        'message': 'Podcast ended successfully',
        'podcast': podcast.to_dict()
    })


@bp.route('/<int:podcast_id>/toggle-mute', methods=['POST'])
@jwt_required()
@admin_required
def toggle_mute(podcast_id):
    """Toggle mute state of a live podcast - admin only"""
    podcast = Podcast.query.get_or_404(podcast_id)
    
    if podcast.status != PodcastStatus.LIVE:
        return jsonify({'error': 'Podcast is not live'}), 400
    
    podcast.is_muted = not podcast.is_muted
    db.session.commit()
    
    return jsonify({
        'message': f'Podcast {"muted" if podcast.is_muted else "unmuted"}',
        'is_muted': podcast.is_muted
    })


# ==================== Viewer Management ====================

@bp.route('/<int:podcast_id>/join', methods=['POST'])
@jwt_required()
def join_podcast(podcast_id):
    """Join a live podcast as a viewer"""
    current_user_id = get_jwt_identity()
    podcast = Podcast.query.get_or_404(podcast_id)
    
    if podcast.status != PodcastStatus.LIVE:
        return jsonify({'error': 'Podcast is not live'}), 400
    
    user = User.query.get(current_user_id)
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    # Add to viewers if not already viewing
    if user not in podcast.viewers:
        podcast.viewers.append(user)
        db.session.commit()
    
    return jsonify({
        'message': 'Joined podcast successfully',
        'viewer_count': podcast.viewer_count
    })


@bp.route('/<int:podcast_id>/leave', methods=['POST'])
@jwt_required()
def leave_podcast(podcast_id):
    """Leave a podcast as a viewer"""
    current_user_id = get_jwt_identity()
    podcast = Podcast.query.get_or_404(podcast_id)
    
    user = User.query.get(current_user_id)
    if user and user in podcast.viewers:
        podcast.viewers.remove(user)
        db.session.commit()
    
    return jsonify({
        'message': 'Left podcast successfully',
        'viewer_count': podcast.viewer_count
    })


@bp.route('/<int:podcast_id>/viewers', methods=['GET'])
@jwt_required()
def get_viewer_count(podcast_id):
    """Get current viewer count"""
    podcast = Podcast.query.get_or_404(podcast_id)
    return jsonify({'viewer_count': podcast.viewer_count})


# ==================== Hand Raise Management ====================

@bp.route('/<int:podcast_id>/hand-raise', methods=['POST'])
@jwt_required()
def raise_hand(podcast_id):
    """Request to speak (student raises hand)"""
    current_user_id = get_jwt_identity()
    podcast = Podcast.query.get_or_404(podcast_id)
    
    if podcast.status != PodcastStatus.LIVE:
        return jsonify({'error': 'Podcast is not live'}), 400
    
    # Check if already has pending hand raise
    existing = HandRaise.query.filter_by(
        podcast_id=podcast_id,
        user_id=current_user_id,
        status=HandRaiseStatus.PENDING
    ).first()
    
    if existing:
        return jsonify({'error': 'You already have a pending hand raise request'}), 400
    
    hand_raise = HandRaise(
        podcast_id=podcast_id,
        user_id=current_user_id,
        status=HandRaiseStatus.PENDING
    )
    
    db.session.add(hand_raise)
    db.session.commit()
    
    return jsonify({
        'message': 'Hand raised successfully',
        'hand_raise': hand_raise.to_dict()
    })


@bp.route('/<int:podcast_id>/hand-raise', methods=['DELETE'])
@jwt_required()
def cancel_hand_raise(podcast_id):
    """Cancel a hand raise request"""
    current_user_id = get_jwt_identity()
    
    hand_raise = HandRaise.query.filter_by(
        podcast_id=podcast_id,
        user_id=current_user_id,
        status=HandRaiseStatus.PENDING
    ).first()
    
    if not hand_raise:
        return jsonify({'error': 'No pending hand raise to cancel'}), 404
    
    db.session.delete(hand_raise)
    db.session.commit()
    
    return jsonify({'message': 'Hand raise cancelled'})


@bp.route('/<int:podcast_id>/hand-raise/status', methods=['GET'])
@jwt_required()
def get_hand_raise_status(podcast_id):
    """Get current user's hand raise status"""
    current_user_id = get_jwt_identity()
    
    hand_raise = HandRaise.query.filter_by(
        podcast_id=podcast_id,
        user_id=current_user_id
    ).order_by(HandRaise.created_at.desc()).first()
    
    if not hand_raise:
        return jsonify({'status': None, 'hand_raise': None})
    
    return jsonify({
        'status': hand_raise.status.value,
        'hand_raise': hand_raise.to_dict()
    })


@bp.route('/<int:podcast_id>/hand-raises', methods=['GET'])
@jwt_required()
@admin_required
def get_hand_raises(podcast_id):
    """Get all pending hand raises for a podcast (admin only)"""
    podcast = Podcast.query.get_or_404(podcast_id)
    
    hand_raises = HandRaise.query.filter_by(
        podcast_id=podcast_id,
        status=HandRaiseStatus.PENDING
    ).order_by(HandRaise.created_at.asc()).all()
    
    return jsonify({
        'hand_raises': [hr.to_dict(include_user=True) for hr in hand_raises],
        'count': len(hand_raises)
    })


@bp.route('/<int:podcast_id>/hand-raises/<int:user_id>/accept', methods=['POST'])
@jwt_required()
@admin_required
def accept_hand_raise(podcast_id, user_id):
    """Accept a hand raise request (admin only)"""
    hand_raise = HandRaise.query.filter_by(
        podcast_id=podcast_id,
        user_id=user_id,
        status=HandRaiseStatus.PENDING
    ).first()
    
    if not hand_raise:
        return jsonify({'error': 'Hand raise request not found'}), 404
    
    hand_raise.status = HandRaiseStatus.ACCEPTED
    hand_raise.responded_at = datetime.now()
    db.session.commit()
    
    return jsonify({
        'message': 'Hand raise accepted',
        'hand_raise': hand_raise.to_dict(include_user=True)
    })


@bp.route('/<int:podcast_id>/hand-raises/<int:user_id>/ignore', methods=['POST'])
@jwt_required()
@admin_required
def ignore_hand_raise(podcast_id, user_id):
    """Ignore a hand raise request (admin only)"""
    hand_raise = HandRaise.query.filter_by(
        podcast_id=podcast_id,
        user_id=user_id,
        status=HandRaiseStatus.PENDING
    ).first()
    
    if not hand_raise:
        return jsonify({'error': 'Hand raise request not found'}), 404
    
    hand_raise.status = HandRaiseStatus.IGNORED
    hand_raise.responded_at = datetime.now()
    db.session.commit()
    
    return jsonify({
        'message': 'Hand raise ignored',
        'hand_raise': hand_raise.to_dict(include_user=True)
    })
