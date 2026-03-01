import os
from dotenv import load_dotenv
from app import create_app

# Load environment variables
load_dotenv()

# Create Flask application
app = create_app(os.getenv('FLASK_ENV', 'development'))

if __name__ == "__main__":
    # Bind to 0.0.0.0 to allow connections from Android devices on the network
    app.run(host='0.0.0.0', port=5000, debug=True)

