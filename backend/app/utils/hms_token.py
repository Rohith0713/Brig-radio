import jwt
import uuid
import time
import os
from datetime import datetime, timedelta

def generate_hms_token(room_id, user_id, role):
    """
    Generate a 100ms Auth Token for a peer to join a room.
    """
    access_key = os.environ.get('HMS_ACCESS_KEY')
    app_secret = os.environ.get('HMS_SECRET')
    
    if not access_key or not app_secret:
        print("Warning: HMS_ACCESS_KEY or HMS_SECRET not set in environment.")
        return None

    # Payload for 100ms Auth Token
    now = int(time.time())
    exp = now + (24 * 3600) # 24 hours expiry
    
    payload = {
        "access_key": access_key,
        "type": "app",
        "version": 2,  # Check if this version is still correct
        "role": role,
        "room_id": room_id,
        "user_id": str(user_id),
        "iat": now,
        "nbf": now,
        "exp": exp,
        "jti": str(uuid.uuid4())
    }
    
    try:
        token = jwt.encode(payload, app_secret, algorithm="HS256")
        return token
    except Exception as e:
        print(f"Error generating HMS token: {e}")
        return None
