import os
import uuid
from flask import Blueprint, request, jsonify, current_app
from werkzeug.utils import secure_filename
from app.extensions import db
from app.models.banner import Banner
from app.middleware.auth import admin_required

banners_bp = Blueprint('banners', __name__)

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'webp'}

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def get_banners_upload_folder():
    """Get absolute path to banners upload folder, creating it if needed."""
    folder = os.path.join(current_app.config['UPLOAD_FOLDER'], 'banners')
    os.makedirs(folder, exist_ok=True)
    return folder

@banners_bp.route('/banners', methods=['POST'])
@admin_required
def upload_banner():
    upload_folder = get_banners_upload_folder()
    
    if 'banner' not in request.files:
        return jsonify({'error': 'No file part'}), 400
        
    file = request.files['banner']
    
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
        
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        # Unique filename to prevent overwrite
        unique_filename = f"{uuid.uuid4()}_{filename}"
        file_path = os.path.join(upload_folder, unique_filename)
        file.save(file_path)
        
        # Store path relative to uploads root (served via /uploads/<path>)
        image_url = f"/uploads/banners/{unique_filename}"
        
        new_banner = Banner(image_url=image_url)
        db.session.add(new_banner)
        db.session.commit()
        
        return jsonify({'message': 'Banner uploaded successfully', 'banner': new_banner.to_dict()}), 201
        
    return jsonify({'error': 'Invalid file type'}), 400

@banners_bp.route('/banners', methods=['GET'])
def get_banners():
    banners = Banner.query.order_by(Banner.created_at.desc()).all()
    return jsonify({'banners': [b.to_dict() for b in banners]}), 200

@banners_bp.route('/banners/<int:banner_id>', methods=['DELETE'])
@admin_required
def delete_banner(banner_id):
    banner = Banner.query.get(banner_id)
    if not banner:
        return jsonify({'error': 'Banner not found'}), 404
        
    # Delete file from filesystem using absolute path
    try:
        if banner.image_url:
            # image_url is like "/uploads/banners/filename.jpg"
            relative = banner.image_url.lstrip('/')
            # Strip "uploads/" prefix since UPLOAD_FOLDER already points to uploads/
            if relative.startswith('uploads/'):
                relative = relative[len('uploads/'):]
            abs_path = os.path.join(current_app.config['UPLOAD_FOLDER'], relative)
            if os.path.exists(abs_path):
                os.remove(abs_path)
    except Exception as e:
        current_app.logger.error(f"Error deleting banner file: {e}")
        
    db.session.delete(banner)
    db.session.commit()
    
    return jsonify({'message': 'Banner deleted successfully'}), 200
