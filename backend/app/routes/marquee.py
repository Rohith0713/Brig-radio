from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.extensions import db
from app.models.marquee import Marquee
from app.models.user import User, UserRole
from datetime import datetime

bp = Blueprint('marquee', __name__, url_prefix='/api/marquee')

@bp.route('/active', methods=['GET'])
def get_active_marquee():
    """Get the latest active marquee message"""
    marquee = Marquee.query.filter_by(is_active=True).order_by(Marquee.updated_at.desc()).first()
    if not marquee:
        return jsonify({'message': None}), 200
    return jsonify(marquee.to_dict()), 200

@bp.route('', methods=['GET'])
@jwt_required()
def get_all_marquees():
    """Admin only: Get all marquee messages"""
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    if not user or user.role not in [UserRole.ADMIN, UserRole.MAIN_ADMIN]:
        return jsonify({'error': 'Unauthorized'}), 403
        
    marquees = Marquee.query.order_by(Marquee.created_at.desc()).all()
    return jsonify([m.to_dict() for m in marquees]), 200

@bp.route('', methods=['POST'])
@jwt_required()
def create_or_update_marquee():
    """Admin only: Create or update a marquee message"""
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    if not user or user.role not in [UserRole.ADMIN, UserRole.MAIN_ADMIN]:
        return jsonify({'error': 'Unauthorized'}), 403
        
    data = request.get_json()
    message = data.get('message')
    is_active = data.get('is_active', False)
    marquee_id = data.get('id')
    
    # Style fields
    text_color = data.get('text_color', '#6366F1')
    font_style = data.get('font_style', 'Bold')
    font_size = data.get('font_size', 'Medium')
    bg_color = data.get('bg_color', '#6366F1')
    bg_gradient_end = data.get('bg_gradient_end')
    scroll_speed = data.get('scroll_speed', 'Normal')
    text_alignment = data.get('text_alignment', 'Left')
    
    if not message:
        return jsonify({'error': 'Message is required'}), 400
        
    if is_active:
        # If we are activating this one, deactivate all others
        Marquee.query.filter(Marquee.id != marquee_id).update({Marquee.is_active: False})
        
    if marquee_id:
        marquee = Marquee.query.get(marquee_id)
        if not marquee:
            return jsonify({'error': 'Marquee not found'}), 404
        marquee.message = message
        marquee.is_active = is_active
    else:
        marquee = Marquee(message=message, is_active=is_active)
        db.session.add(marquee)
    
    # Apply style fields
    marquee.text_color = text_color
    marquee.font_style = font_style
    marquee.font_size = font_size
    marquee.bg_color = bg_color
    marquee.bg_gradient_end = bg_gradient_end
    marquee.scroll_speed = scroll_speed
    marquee.text_alignment = text_alignment
        
    db.session.commit()
    return jsonify(marquee.to_dict()), 201

@bp.route('/<int:id>/toggle', methods=['PATCH'])
@jwt_required()
def toggle_marquee(id):
    """Admin only: Toggle marquee active status"""
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    if not user or user.role not in [UserRole.ADMIN, UserRole.MAIN_ADMIN]:
        return jsonify({'error': 'Unauthorized'}), 403
        
    marquee = Marquee.query.get_or_404(id)
    
    # If we are activating this one, deactivate all others
    if not marquee.is_active:
        Marquee.query.filter(Marquee.id != id).update({Marquee.is_active: False})
        marquee.is_active = True
    else:
        marquee.is_active = False
        
    db.session.commit()
    return jsonify(marquee.to_dict()), 200

@bp.route('/<int:id>', methods=['DELETE'])
@jwt_required()
def delete_marquee(id):
    """Admin only: Delete a marquee message"""
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    if not user or user.role not in [UserRole.ADMIN, UserRole.MAIN_ADMIN]:
        return jsonify({'error': 'Unauthorized'}), 403
        
    marquee = Marquee.query.get_or_404(id)
    db.session.delete(marquee)
    db.session.commit()
    return jsonify({'message': 'Marquee deleted successfully'}), 200
