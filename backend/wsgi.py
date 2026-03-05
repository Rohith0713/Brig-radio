"""
WSGI entry point for production deployment.
Used by Gunicorn: gunicorn wsgi:app
"""
import os
from dotenv import load_dotenv

# Load environment variables before creating the app
load_dotenv()

from app import create_app

app = create_app(os.getenv('FLASK_ENV', 'production'))
