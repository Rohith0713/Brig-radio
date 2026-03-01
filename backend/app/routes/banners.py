import os
import uuid
from flask import Blueprint, request, jsonify
from werkzeug.utils import secure_filename
from app.extensions import db
from app.models.banner import Banner
from app.middleware.auth import token_required, admin_required

banners_bp = Blueprint('banners', __name__)

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'webp'}
UPLOAD_FOLDER = 'uploads/banners'

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def ensure_upload_folder():
    if not os.path.exists(UPLOAD_FOLDER):
        os.makedirs(UPLOAD_FOLDER)

@banners_bp.route('/banners', methods=['POST'])
@token_required
@admin_required
def upload_banner(current_user):
    ensure_upload_folder()
    
    if 'banner' not in request.files:
        return jsonify({'error': 'No file part'}), 400
        
    file = request.files['banner']
    
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
        
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        # Unique filename to prevent overwrite
        unique_filename = f"{uuid.uuid4()}_{filename}"
        file_path = os.path.join(UPLOAD_FOLDER, unique_filename)
        file.save(file_path)
        
        # Store relative path or URL
        image_url = f"uploads/banners/{unique_filename}"
        
        new_banner = Banner(image_url=image_url)
        db.session.add(new_banner)
        db.session.commit()
        
        return jsonify({'message': 'Banner uploaded successfully', 'banner': new_banner.to_dict()}), 201
        
    return jsonify({'error': 'Invalid file type'}), 400

@banners_bp.route('/banners', methods=['GET'])
def get_banners():
    # Public endpoint, but maybe we want to sort by latest?
    banners = Banner.query.order_by(Banner.created_at.desc()).all()
    return jsonify({'banners': [b.to_dict() for b in banners]}), 200

@banners_bp.route('/banners/<int:banner_id>', methods=['DELETE'])
@token_required
@admin_required
def delete_banner(current_user, banner_id):
    banner = Banner.query.get(banner_id)
    if not banner:
        return jsonify({'error': 'Banner not found'}), 404
        
    # Delete file from filesystem
    try:
        # Construct absolute path if needed, assuming image_url is relative to root
        # If image_url is like "uploads/banners/...", we can use it directly if CWD is root
        if os.path.exists(banner.image_url):
            os.remove(banner.image_url)
    except Exception as e:
        print(f"Error deleting file: {e}")
        # Continue to delete from DB even if file delete fails (orphaned file is better than broken UI)
        
    db.session.delete(banner)
    db.session.commit()
    
    return jsonify({'message': 'Banner deleted successfully'}), 200
