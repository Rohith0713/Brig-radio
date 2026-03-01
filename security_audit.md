# Security Audit Report

I have conducted a security review of the backend and signaling infrastructure. Below is the assessment of the 7 points you requested.

## 1. Debug Mode
- **Status**: 🔴 **ENABLED**
- **Details**: `debug=True` is explicitly passed to `app.run()` in `app.py`. Additionally, the `DevelopmentConfig` in `config.py` defaults to `DEBUG = True`.
- **Recommendation**: Disable debug mode in production to prevent detailed error leakage and potential remote code execution via the Werkzeug debugger.

## 2. Hardcoded Secrets
- **Status**: 🟡 **PARTIAL RISK**
- **Details**: `config.py` contains default strings for `SECRET_KEY` and `JWT_SECRET_KEY` (`'dev-secret-key-change-in-production'`).
- **Recommendation**: Ensure these are overridden by `.env` file variables in any deployed environment. Never commit the actual production secrets to Git.

## 3. Exposed Admin Routes
- **Status**: 🟢 **SECURE**
- **Details**: All administrative routes (e.g., creating radios, managing requests, analytics) are correctly decorated with `@admin_required`, which verifies both JWT validly and user role.
- **Exceptions**: The signaling server (see point 6) bypasses these checks.

## 4. JWT Protection Gaps
- **Status**: 🟡 **LOW RISK**
- **Details**: Most radio-fetching routes (`get_radios`, `get_live_radios`) are public. While this allows students to view content without logging in, `get_stream_info` returns the `media_url`, exposing local storage paths to unauthenticated users.
- **Recommendation**: Require `@jwt_required()` for internal stream metadata.

## 5. CORS Configuration
- **Status**: 🟡 **PERMISSIVE**
- **Details**: `cors.init_app(app)` is used in `app/__init__.py`. By default, this allows all origins (`*`).
- **Recommendation**: Restrict `ORIGINS` to the specific frontend domain or mobile app scheme in production.

## 6. Open WebSocket Ports
- **Status**: 🔴 **CRITICAL VULNERABILITY**
- **Details**: `signaling_server.py` (Port 8765) has **zero authentication**. 
  - Any client can join any `room_id` (which are predictable integer IDs).
  - Any client can send `admin_pause`, `radio_resume`, or `radio_stop` messages, which the server will broadcast to all listeners, effectively giving any user administrative control over the live stream.
- **Recommendation**: Implement a token-based handshake for WebSocket connections.

## 7. Public File Access Vulnerabilities
- **Status**: 🟡 **LOW RISK**
- **Details**: Uploaded files are served via `/uploads/<filename>`. 
  - Flask’s `send_from_directory` is safe from traversal, and filenames are sanitized with `secure_filename`.
  - However, there is no authorization check once a filename is known. Anyone with the link can download any file.
- **Recommendation**: If sensitive media is hosted, implement an authorized file serving route that checks user permissions before calling `send_from_directory`.
