from flask_mail import Message
from app.extensions import mail
from flask import current_app
from config import Config
from concurrent.futures import ThreadPoolExecutor
import logging

logger = logging.getLogger(__name__)

# Use a ThreadPoolExecutor for more efficient background tasks
email_executor = ThreadPoolExecutor(max_workers=4)

def send_async_email(app, msg):
    with app.app_context():
        # Set the sender name for all emails
        msg.sender = f"BRIG RADIO <{Config.MAIL_USERNAME}>"
        try:
            mail.send(msg)
            logger.info(f"Successfully sent email to {msg.recipients}")
        except Exception as e:
            logger.error(f"Error sending email to {msg.recipients}: {e}")

def send_otp_email(email, otp):
    """Send OTP to user email synchronously for verification"""
    app = current_app._get_current_object()
    msg = Message(
        subject="BRIG RADIO - Verification Code",
        recipients=[email],
        body=f"Your verification code is: {otp}\n\nThis code will expire in 5 minutes.\n\nRegards,\nBRIG RADIO Team"
    )
    try:
        with app.app_context():
            msg.sender = f"BRIG RADIO <{Config.MAIL_USERNAME}>"
            mail.send(msg)
            return True
    except Exception as e:
        logger.error(f"Error sending OTP to {email}: {e}")
        return False

def send_suggestion_approved_email(email, student_name, radio_title):
    """Send suggestion approval email asynchronously"""
    app = current_app._get_current_object()
    msg = Message(
        subject="BRIG RADIO - Suggestion Accepted!",
        recipients=[email],
        body=f"Hi {student_name},\n\nGreat news! Your suggestion '{radio_title}' has been reviewed and accepted by the admin.\n\nThank you for your valuable feedback regarding this radio show!\n\nBest regards,\nBRIG RADIO Team"
    )
    email_executor.submit(send_async_email, app, msg)
    return True

def send_admin_approval_email(email, name):
    """Send admin approval notification email asynchronously"""
    app = current_app._get_current_object()
    msg = Message(
        subject="Admin Request Approved - BRIG RADIO",
        recipients=[email],
        body=f"Hello {name},\n\nYour request to register as an Admin has been accepted by the Main Admin.\n\nYou can now log in to the Admin Dashboard using your registered email and password.\n\nThank you.\nBRIG RADIO Team"
    )
    email_executor.submit(send_async_email, app, msg)
    return True

def send_password_reset_otp(email, otp):
    """Send Password Reset OTP synchronously"""
    app = current_app._get_current_object()
    msg = Message(
        subject="Password Reset Request - BRIG RADIO",
        recipients=[email],
        body=f"Hello,\n\nYou requested to reset your password. Use the code below to verify your identity:\n\n{otp}\n\nThis code will expire in 5 minutes.\n\nIf you did not request this, please ignore this email.\n\nRegards,\nBRIG RADIO Security Team"
    )
    try:
        with app.app_context():
            msg.sender = f"BRIG RADIO <{Config.MAIL_USERNAME}>"
            mail.send(msg)
            return True
    except Exception as e:
        logger.error(f"Error sending reset OTP to {email}: {e}")
        return False

def send_password_reset_success(email):
    """Send Password Reset Confirmation asynchronously"""
    app = current_app._get_current_object()
    msg = Message(
        subject="Password Security Update - BRIG RADIO",
        recipients=[email],
        body=f"Hello,\n\nYour password has been successfully reset.\n\nIf you did not perform this action, please contact support immediately.\n\nRegards,\nBRIG RADIO Security Team"
    )
    email_executor.submit(send_async_email, app, msg)
    return True
