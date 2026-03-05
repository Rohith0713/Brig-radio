import os
from werkzeug.utils import secure_filename
from flask import current_app

def allowed_file(filename):
    """Check if file extension is allowed"""
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in current_app.config['ALLOWED_EXTENSIONS']

def save_upload(file, subfolder=None):
    """Save uploaded file and return relative path (e.g. 'radios/banner_20260305.jpg').
    
    Args:
        file: Werkzeug FileStorage object
        subfolder: Optional subdirectory under UPLOAD_FOLDER (e.g. 'radios', 'banners', 'profiles')
    """
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        
        # Add timestamp to avoid filename conflicts
        from datetime import datetime
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        name, ext = os.path.splitext(filename)
        filename = f"{name}_{timestamp}{ext}"
        
        # Build target directory, creating subfolder if needed
        target_dir = current_app.config['UPLOAD_FOLDER']
        if subfolder:
            target_dir = os.path.join(target_dir, subfolder)
            os.makedirs(target_dir, exist_ok=True)
        
        filepath = os.path.join(target_dir, filename)
        file.save(filepath)
        
        # Return path relative to UPLOAD_FOLDER for DB storage
        return f"{subfolder}/{filename}" if subfolder else filename
    return None

def delete_file(filename):
    """Delete uploaded file"""
    if filename:
        filepath = os.path.join(current_app.config['UPLOAD_FOLDER'], filename)
        if os.path.exists(filepath):
            os.remove(filepath)
            return True
    return False
