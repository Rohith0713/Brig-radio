import os
import uuid
from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from werkzeug.utils import secure_filename
from app.extensions import db
from app.models.placement import Placement, PlacementPoster, PlacementBookmark
from app.middleware.auth import admin_required
from datetime import datetime

bp = Blueprint('placements', __name__, url_prefix='/api/placements')

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'webp'}

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def get_placements_upload_folder():
    """Get absolute path to placements upload folder, creating it if needed."""
    folder = os.path.join(current_app.config['UPLOAD_FOLDER'], 'placements')
    os.makedirs(folder, exist_ok=True)
    return folder

@bp.route('', methods=['GET'])
@jwt_required()
def get_placements():
    user_id = get_jwt_identity()
    placements = Placement.query.order_by(Placement.posted_at.desc()).all()
    
    # Get user's bookmarks
    user_bookmarks = PlacementBookmark.query.filter_by(user_id=user_id).all()
    bookmarked_ids = {b.placement_id for b in user_bookmarks}
    
    results = []
    for p in placements:
        p_dict = p.to_dict()
        p_dict['isBookmarked'] = p.id in bookmarked_ids
        results.append(p_dict)
        
    return jsonify(results), 200

@bp.route('/saved', methods=['GET'])
@jwt_required()
def get_saved_placements():
    user_id = get_jwt_identity()
    
    # Find placements bookmarked by user
    bookmarks = PlacementBookmark.query.filter_by(user_id=user_id).all()
    placement_ids = [b.placement_id for b in bookmarks]
    
    if not placement_ids:
        return jsonify([]), 200
        
    placements = Placement.query.filter(Placement.id.in_(placement_ids)).order_by(Placement.posted_at.desc()).all()
    
    results = []
    for p in placements:
        p_dict = p.to_dict()
        p_dict['isBookmarked'] = True
        results.append(p_dict)
        
    return jsonify(results), 200

@bp.route('/<int:id>/bookmark', methods=['POST'])
@jwt_required()
def toggle_bookmark(id):
    user_id = get_jwt_identity()
    placement = Placement.query.get_or_404(id)
    
    bookmark = PlacementBookmark.query.filter_by(user_id=user_id, placement_id=id).first()
    
    if bookmark:
        db.session.delete(bookmark)
        db.session.commit()
        return jsonify({'message': 'Removed from saved', 'isBookmarked': False}), 200
    else:
        new_bookmark = PlacementBookmark(user_id=user_id, placement_id=id)
        db.session.add(new_bookmark)
        db.session.commit()
        return jsonify({'message': 'Saved to placements', 'isBookmarked': True}), 201

@bp.route('', methods=['POST'])
@admin_required
def create_placement():
    data = request.get_json()
    
    if not data or not data.get('title') or not data.get('company'):
        return jsonify({'message': 'Title and Company are required'}), 400
        
    placement = Placement(
        title=data.get('title'),
        company=data.get('company'),
        location=data.get('location', 'Remote'),
        salary=data.get('salary'),
        deadline=data.get('deadline'),
        description=data.get('description'),
        application_link=data.get('applicationLink')
    )
    
    db.session.add(placement)
    db.session.commit()
    
    return jsonify(placement.to_dict()), 201

@bp.route('/<int:id>', methods=['DELETE'])
@admin_required
def delete_placement(id):
    placement = Placement.query.get_or_404(id)
    # Delete associated bookmarks first to avoid foreign key constraint errors
    PlacementBookmark.query.filter_by(placement_id=id).delete()
    db.session.delete(placement)
    db.session.commit()
    return jsonify({'message': 'Placement deleted'}), 200

@bp.route('/<int:id>/apply', methods=['POST'])
@jwt_required()
def apply_placement(id):
    placement = Placement.query.get_or_404(id)
    placement.applicants_count += 1
    db.session.commit()
    return jsonify({'message': 'Application tracked', 'applicantsCount': placement.applicants_count}), 200

# ==================== Placement Posters ====================

@bp.route('/posters', methods=['POST'])
@admin_required
def upload_poster():
    upload_folder = get_placements_upload_folder()
    
    title = request.form.get('title')
    company = request.form.get('company')
    description = request.form.get('description')
    
    if not title:
        return jsonify({'error': 'Title is required'}), 400
        
    if 'poster' not in request.files:
        return jsonify({'error': 'No file part'}), 400
        
    file = request.files['poster']
    
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
        
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        unique_filename = f"{uuid.uuid4()}_{filename}"
        file_path = os.path.join(upload_folder, unique_filename)
        file.save(file_path)
        
        poster_image = f"/uploads/placements/{unique_filename}"
        
        new_poster = PlacementPoster(
            title=title,
            company=company,
            description=description,
            poster_image=poster_image
        )
        db.session.add(new_poster)
        db.session.commit()
        
        return jsonify(new_poster.to_dict()), 201
        
    return jsonify({'error': 'Invalid file type'}), 400

@bp.route('/posters', methods=['GET'])
@jwt_required()
def get_posters():
    posters = PlacementPoster.query.order_by(PlacementPoster.created_at.desc()).all()
    return jsonify([p.to_dict() for p in posters]), 200

@bp.route('/posters/<int:id>', methods=['PATCH'])
@admin_required
def update_poster(id):
    poster = PlacementPoster.query.get_or_404(id)
    data = request.get_json()
    
    if 'title' in data:
        poster.title = data['title']
    if 'company' in data:
        poster.company = data['company']
    if 'description' in data:
        poster.description = data['description']
    if 'isVisible' in data:
        poster.is_visible = data['isVisible']
        
    db.session.commit()
    return jsonify(poster.to_dict()), 200

@bp.route('/posters/<int:id>', methods=['DELETE'])
@admin_required
def delete_poster(id):
    poster = PlacementPoster.query.get_or_404(id)
    
    # Delete file using absolute path
    try:
        if poster.poster_image:
            relative = poster.poster_image.lstrip('/')
            if relative.startswith('uploads/'):
                relative = relative[len('uploads/'):]
            abs_path = os.path.join(current_app.config['UPLOAD_FOLDER'], relative)
            if os.path.exists(abs_path):
                os.remove(abs_path)
    except Exception as e:
        current_app.logger.error(f"Error deleting poster file: {e}")
        
    db.session.delete(poster)
    db.session.commit()
    return jsonify({'message': 'Poster deleted'}), 200
