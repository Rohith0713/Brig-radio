from flask import Flask, request
import os
import logging
from config import config
from app.extensions import db, migrate, jwt, cors, mail

def create_app(config_name='development'):
    """Application factory pattern"""
    app = Flask(__name__)
    
    # Load configuration
    app.config.from_object(config[config_name])
    
    # Disable strict slashes globally
    app.url_map.strict_slashes = False
    
    # ── Logging ──────────────────────────────────────────────
    if config_name == 'production':
        logging.basicConfig(level=logging.INFO)
        app.logger.setLevel(logging.INFO)
    else:
        logging.basicConfig(level=logging.DEBUG)
        app.logger.setLevel(logging.DEBUG)

    handler = logging.StreamHandler()
    handler.setFormatter(logging.Formatter(
        '[%(asctime)s] %(levelname)s in %(module)s: %(message)s'
    ))
    app.logger.addHandler(handler)
    
    # Initialize extensions
    db.init_app(app)
    migrate.init_app(app, db)
    jwt.init_app(app)
    cors.init_app(app, resources={
        r"/api/*": {
            "origins": "*",
            "methods": ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
            "allow_headers": ["Content-Type", "Authorization"],
            "supports_credentials": True
        }
    })
    mail.init_app(app)
    
    # Create upload folder if it doesn't exist
    upload_folder = app.config['UPLOAD_FOLDER']
    if not os.path.exists(upload_folder):
        os.makedirs(upload_folder)
    
    # ── Request / Response Logging ───────────────────────────
    @app.after_request
    def log_response(response):
        app.logger.info(f'{request.method} {request.path} → {response.status_code}')
        return response

    @app.errorhandler(Exception)
    def handle_exception(e):
        app.logger.error(f'Unhandled exception: {e}', exc_info=True)
        from flask import jsonify
        return jsonify({'error': 'Internal server error'}), 500
    
    # Register blueprints
    from app.routes import auth, radios, suggestions, dashboard
    app.register_blueprint(auth.bp)
    app.register_blueprint(radios.bp)
    app.register_blueprint(suggestions.bp)
    app.register_blueprint(dashboard.bp)
    
    from app.routes import reviews
    app.register_blueprint(reviews.bp)
    
    # New feature blueprints
    from app.routes import categories, favorites, comments, analytics, notifications, college_updates
    app.register_blueprint(categories.bp)
    app.register_blueprint(favorites.bp)
    app.register_blueprint(comments.bp)
    app.register_blueprint(analytics.bp)
    app.register_blueprint(notifications.bp)
    app.register_blueprint(college_updates.bp)
    
    from app.routes import reports, marquee
    app.register_blueprint(reports.reports_bp, url_prefix='/api/reports')
    app.register_blueprint(marquee.bp)
    
    # Placements feature
    from app.routes import placements
    app.register_blueprint(placements.bp)
    
    # Issues feature
    from app.routes import issues
    app.register_blueprint(issues.bp)

    # Banners feature
    from app.routes import banners
    app.register_blueprint(banners.banners_bp, url_prefix='/api')
    
    # Serve uploaded files
    from flask import send_from_directory
    @app.route('/uploads/<path:filename>')
    def serve_upload(filename):
        return send_from_directory(app.config['UPLOAD_FOLDER'], filename)
    
    # Register error handlers
    from app.errors import handlers
    handlers.register_error_handlers(app)
    
    app.logger.info(f'CampusWave app created with [{config_name}] config')
    
    return app

