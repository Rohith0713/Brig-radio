import firebase_admin
from firebase_admin import messaging, credentials
import os
import logging

logger = logging.getLogger(__name__)

# Initialize Firebase Admin
def init_firebase():
    """Initialize Firebase Admin SDK if not already initialized"""
    if not firebase_admin._apps:
        key_path = os.path.join(os.getcwd(), 'firebase-credentials.json')

        if os.path.exists(key_path):
            cred = credentials.Certificate(key_path)
            firebase_admin.initialize_app(cred)
            logger.info("Firebase Admin initialized with firebase-credentials.json")
        else:
            logger.warning("firebase-credentials.json not found. Firebase notifications will be mocked.")

def send_topic_notification(topic, title, body, data=None):
    """Send FCM notification to a topic"""
    try:
        init_firebase()

        if not firebase_admin._apps:
            # Fallback to mock if initialization failed
            logger.info(f"Mock Notification to topic {topic}: {title} - {body}")
            return True

        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body,
            ),
            data=data or {},
            topic=topic,
        )
        response = messaging.send(message)
        logger.info(f"Successfully sent FCM message: {response}")
        return True
    except Exception as e:
        logger.error(f"Error sending notification: {e}")
        return False
