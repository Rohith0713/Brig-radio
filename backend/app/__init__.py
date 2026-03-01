from flask import Flask
import os
from config import config
from app.extensions import db, migrate, jwt, cors, mail

def create_app(config_name='development'):
    """Application factory pattern"""
    app = Flask(__name__)
    
    # Load configuration
    app.config.from_object(config[config_name])
    
    # Disable strict slashes globally
    app.url_map.strict_slashes = False
    
    # Initialize extensions
    db.init_app(app)
    migrate.init_app(app, db)
    jwt.init_app(app)
    cors.init_app(app)
    mail.init_app(app)
    
    # Create upload folder if it doesn't exist
    upload_folder = app.config['UPLOAD_FOLDER']
    if not os.path.exists(upload_folder):
        os.makedirs(upload_folder)
    
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
    
    # Podcast feature
    from app.routes import podcasts, placements, agora, hms
    app.register_blueprint(podcasts.bp)
    app.register_blueprint(placements.bp)
    app.register_blueprint(agora.bp)
    app.register_blueprint(hms.bp)
    
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
    
    return app
