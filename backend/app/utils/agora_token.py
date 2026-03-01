import os
import time
from agora_token_builder import RtcTokenBuilder

def generate_rtc_token(channel_name, uid, role='publisher', expire_time_seconds=3600):
    """
    Generates an Agora RTC token for a given channel and user ID.
    """
    app_id = os.getenv('AGORA_APP_ID')
    app_certificate = os.getenv('AGORA_APP_CERTIFICATE')
    
    # Validation
    if not app_id:
        return None
        
    # If certificate is missing, Agora won't allow token generation 
    # unless App Certificate is disabled in the console (not recommended).
    # If it's disabled, return None to indicate no token is needed (app id only).
    if not app_certificate or app_certificate == 'YOUR_AGORA_APP_CERTIFICATE_HERE':
        return None
        
    # role: 1 is kRolePublisher, 2 is kRoleAttendee (audience)
    if role == 'publisher':
        role_num = 1
    else:
        role_num = 2
        
    current_time = int(time.time())
    privilege_expired_ts = current_time + expire_time_seconds
    
    try:
        token = RtcTokenBuilder.buildTokenWithUid(
            app_id,
            app_certificate,
            channel_name,
            uid,
            role_num,
            privilege_expired_ts
        )
        return token
    except Exception as e:
        print(f"Error generating Agora token: {e}")
        return None
