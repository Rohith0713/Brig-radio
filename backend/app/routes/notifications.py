from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.extensions import db
from app.models.notification import Notification

bp = Blueprint('notifications', __name__, url_prefix='/api/notifications')

@bp.route('', methods=['GET'])
@jwt_required()
def get_notifications():
    """Get user notifications"""
    user_id = int(get_jwt_identity())
    page = request.args.get('page', 1, type=int)
    limit = request.args.get('limit', 20, type=int)
    
    pagination = Notification.query.filter_by(user_id=user_id)\
        .order_by(Notification.created_at.desc())\
        .paginate(page=page, per_page=limit, error_out=False)
        
    return jsonify({
        'notifications': [n.to_dict() for n in pagination.items],
        'total': pagination.total,
        'page': page,
        'pages': pagination.pages
    }), 200

@bp.route('/<int:notification_id>/read', methods=['PUT'])
@jwt_required()
def mark_as_read(notification_id):
    """Mark notification as read"""
    user_id = int(get_jwt_identity())
    notification = Notification.query.get(notification_id)
    
    if not notification:
        return jsonify({'error': 'Notification not found'}), 404
        
    if notification.user_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403
        
    notification.is_read = True
    db.session.commit()
    
    return jsonify(notification.to_dict()), 200

@bp.route('/read-all', methods=['PUT'])
@jwt_required()
def mark_all_as_read():
    """Mark all notifications as read"""
    user_id = int(get_jwt_identity())
    
    Notification.query.filter_by(user_id=user_id, is_read=False)\
        .update({Notification.is_read: True})
        
    db.session.commit()
    
    return jsonify({'message': 'All notifications marked as read'}), 200

@bp.route('/clear-all', methods=['DELETE'])
@jwt_required()
def clear_all_notifications():
    """Delete all notifications for the current user"""
    user_id = int(get_jwt_identity())
    
    Notification.query.filter_by(user_id=user_id).delete()
    db.session.commit()
    
    return jsonify({'message': 'All notifications cleared successfully'}), 200
