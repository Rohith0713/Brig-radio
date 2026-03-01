# Media Flow Analysis Report

I have analyzed the media lifecycle in the Campus Wave project, from upload to serving. Below are the details for each of your 6 points.

## 1. Storage Location
Files are stored locally on the server in the following directory:
- **Path**: `backend/uploads/`
- **Subdirectories**: Some features use subfolders, such as `uploads/profiles/` for profile pictures.
- **Initialization**: The directory is automatically created by the Flask app factory (`app/__init__.py`) if it does not exist.

## 2. Handling (Local vs Cloud)
Uploads are handled entirely on the **local filesystem**.
- **Sanitization**: Uses `werkzeug.utils.secure_filename` to prevent directory traversal attacks.
- **Conflict Resolution**: The `save_upload` utility appends a timestamp (`YYYYMMDD_HHMMSS`) to every filename to ensure uniqueness.
- **Config**: Maximum file size is capped at **200MB** (defined in `config.py`), which supports video uploads for college updates.

## 3. Upload Routes
The following API endpoints handle file uploads:
- **Profile Pictures**: `POST /api/auth/profile/picture`
- **Radio Banners**: `POST /api/radios/{id}/upload-banner`
- **Radio Media (Audio/Video)**: `POST /api/radios/{id}/upload-media`
- **College Updates**: `POST /api/college-updates` (Handles 'media', 'image', or 'video' parts)
- **App Banners**: `POST /api/banners`
- **Placement Posters**: `POST /api/placements/posters`

## 4. Serving Files back to Users
Files are served back via a dedicated Flask route:
- **Route**: `GET /uploads/<path:filename>`
- **Implementation**: Uses Flask's `send_from_directory` to map the URL path directly to the local `uploads` folder.
- **Android Integration**: The Android app uses `UPLOADS_URL` (e.g., `http://10.99.37.110:5000/uploads/`) to fetch these assets.

## 5. Production Safety
While functional, the current setup has several production considerations:
- **Scaling**: ⚠️ **Not Production-Safe** for horizontally scaled environments (e.g., multiple containers/servers) without a shared persistent volume or migration to a cloud provider like AWS S3 or Azure Blob Storage.
- **Security**: The app uses `allowed_file` check against a whitelist and `secure_filename`.
- **Cleanup**: Some logic exists in `radios.py` to delete audio files when a session ends, but a general cleanup strategy for orphaned files is missing.

## 6. Hardcoded Local Paths
- **Backend**: No hardcoded absolute paths were found in the Python code. It uses relative path calculation based on the file's location.
- **Android**: There are hardcoded server IPs in `ApiConfig.kt` (`10.99.37.110`) and `RadioSignalingClient.kt` (`10.36.12.110`) which will need to be updated to a production domain/IP.
