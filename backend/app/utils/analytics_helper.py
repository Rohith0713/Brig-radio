from app.extensions import db
from app.models.system_event import SystemEvent
from flask_jwt_extended import get_jwt_identity
from app.models.user import User
import logging

logger = logging.getLogger(__name__)

def log_event(event_type, user_id=None, role=None, metadata=None):
    """
    Log a system event for analytics.
    
    :param event_type: Type of the event (e.g., 'LOGIN', 'RADIO_START')
    :param user_id: ID of the user performing the action
    :param role: Role of the user
    :param metadata: Dictionary of additional information
    """
    try:
        # If user_id is not provided, try to get it from JWT if in request context
        if user_id is None:
            try:
                user_id = get_jwt_identity()
            except:
                pass
        
        # If still no user_id, it's an anonymous event or background task
        
        # Get role if not provided but user_id exists
        if role is None and user_id:
            user = User.query.get(user_id)
            if user:
                role = user.role.value if hasattr(user.role, 'value') else str(user.role)
        
        event = SystemEvent(
            user_id=user_id,
            role=role,
            event_type=event_type,
            event_metadata=metadata or {}
        )
        
        db.session.add(event)
        db.session.commit()
        logger.info(f"Logged event: {event_type} for user {user_id}")
    except Exception as e:
        logger.error(f"Failed to log event {event_type}: {str(e)}")
        # Don't raise error to avoid breaking main application flow
        db.session.rollback()
