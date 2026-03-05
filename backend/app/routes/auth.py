from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity
from app.extensions import db
from app.models.user import User, UserRole
from app.models.student import Student
from app.models.admin import Admin
from app.models.admin_request import AdminRequest, RequestStatus
from app.utils.email import send_otp_email, send_password_reset_otp
from app.utils.password_validator import validate_password
from app.utils.email_validator import validate_email
from werkzeug.security import generate_password_hash
from datetime import datetime, timedelta
import re
import random
from app.models.otp import OTP
from app.utils.analytics_helper import log_event

bp = Blueprint('auth', __name__, url_prefix='/api/auth')

@bp.route('/register', methods=['POST'])
def register():
    """Register a new user"""
    data = request.get_json()
    
    # Validate required fields
    if not data or not data.get('email') or not data.get('password') or not data.get('name'):
        return jsonify({'error': 'Email, password, and name are required'}), 400
    
    # Enhanced email validation
    email = data['email'].lower().strip()
    is_valid_email, email_error = validate_email(email)
    if not is_valid_email:
        return jsonify({'error': email_error}), 400
    existing_user_email = User.query.filter_by(email=email).first()
    if existing_user_email:
        if existing_user_email.is_verified:
            return jsonify({'error': 'Email already registered and verified'}), 409
        else:
            # User exists but is unverified, allow re-registration by deleting old record
            db.session.delete(existing_user_email)
            db.session.commit()
            
    # Check if phone number already exists
    if data.get('phone_number'):
        existing_user_phone = User.query.filter_by(phone_number=data['phone_number']).first()
        if existing_user_phone:
            if existing_user_phone.is_verified:
                return jsonify({'error': 'Phone number already registered and verified'}), 409
            else:
                # Phone exists but is unverified, allow re-registration by deleting old record
                db.session.delete(existing_user_phone)
                db.session.commit()
    
    # Validate password strength
    is_valid, errors = validate_password(
        password=data['password'],
        name=data['name'],
        email=email,
        phone=data.get('phone_number')
    )
    if not is_valid:
        return jsonify({'error': errors[0] if errors else 'Password does not meet requirements'}), 400
    
    if data.get('role') == 'ADMIN':
        # Check if email is already in user table
        if User.query.filter_by(email=email).first():
            return jsonify({'error': 'Email already registered'}), 409
            
        # Check if there is already a pending request
        if AdminRequest.query.filter_by(email=email, status=RequestStatus.PENDING).first():
            return jsonify({'error': 'An admin registration request for this email is already pending approval.'}), 400
            
        # Create Admin Request
        admin_req = AdminRequest(
            name=data['name'],
            email=email,
            password=generate_password_hash(data['password']),
            department=data.get('department'),
            reason_for_access=data.get('reason_for_access')
        )
        db.session.add(admin_req)
        db.session.commit()
        
        return jsonify({
            'message': 'Your admin registration request has been sent to the Main Admin for approval.',
            'admin_request_id': admin_req.id
        }), 201

    # Create new student user
    user = User(
        email=email,
        phone_number=data.get('phone_number'),
        role=UserRole.STUDENT
    )
    user.set_password(data['password'])
    
    db.session.add(user)
    db.session.flush() # Get user.id
    
    # Create profile
    profile = Student(
        id=user.id,
        name=data['name'],
        college_pin=data.get('college_pin')
    )
    
    db.session.add(profile)
    db.session.commit()
    
    # Generate OTP
    otp_code = ''.join([str(random.randint(0, 9)) for _ in range(6)])
    expires_at = datetime.utcnow() + timedelta(minutes=5)
    
    otp = OTP(identifier=user.email, expires_at=expires_at)
    otp.set_otp(otp_code)
    db.session.add(otp)
    db.session.commit()
    
    # SEND GMAIL OTP
    if not send_otp_email(user.email, otp_code):
        # Rollback user creation if email fails (or handle as unverified)
        db.session.delete(user)
        db.session.commit()
        return jsonify({'error': 'Failed to send verification email. Please check your email address or try again later.'}), 500
    
    log_event('REGISTER', user_id=user.id, role='STUDENT', metadata={'email': user.email})
    
    return jsonify({
        'message': 'Registration successful. Please verify your OTP.',
        'verification_required': True,
        'email': user.email
    }), 201

@bp.route('/login', methods=['POST'])
def login():
    """Login user and return JWT token"""
    data = request.get_json()
    
    # Validate required fields
    if not data or not data.get('email') or not data.get('password'):
        return jsonify({'error': 'Email and password are required'}), 400
    
    # Find user
    email = data['email'].lower()
    user = User.query.filter_by(email=email).first()
    
    # Verify credentials
    if not user or not user.check_password(data['password']):
        return jsonify({'error': 'Invalid email or password'}), 401
        
    # Check verification status
    if not user.is_verified:
        return jsonify({
            'error': 'Account not verified',
            'verification_required': True,
            'email': user.email
        }), 403
    
    # Create access token (identity must be string for proper subject claim)
    access_token = create_access_token(identity=str(user.id))
    
    log_event('LOGIN', user_id=user.id, role=user.role.value, metadata={'email': user.email})
    
    return jsonify({
        'access_token': access_token,
        'user': user.to_dict()
    }), 200

@bp.route('/me', methods=['GET'])
@jwt_required()
def get_current_user():
    """Get current user information"""
    user_id = int(get_jwt_identity())  # Convert string back to int
    user = User.query.get(user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    return jsonify(user.to_dict()), 200

@bp.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    """Logout user (client should delete token)"""
    return jsonify({'message': 'Logged out successfully'}), 200

@bp.route('/verify-otp', methods=['POST'])
def verify_otp():
    """Verify OTP and return JWT token"""
    data = request.get_json()
    
    if not data or not data.get('email') or not data.get('otp'):
        return jsonify({'error': 'Email and OTP are required'}), 400
        
    # Check for valid OTP
    email = data['email'].lower()
    otp_record = OTP.query.filter_by(identifier=email).order_by(OTP.created_at.desc()).first()
    
    if not otp_record or not otp_record.is_valid():
        return jsonify({'error': 'Invalid, expired, or blocked OTP. Please request a new one.'}), 400
        
    # Increment attempts
    otp_record.attempts += 1
    db.session.commit()
    
    if not otp_record.check_otp(data['otp']):
        if otp_record.attempts >= 3:
            return jsonify({'error': 'Too many failed attempts. This OTP is now invalid.'}), 400
        return jsonify({'error': f'Invalid OTP. {3 - otp_record.attempts} attempts remaining.'}), 400
        
    # Mark user as verified
    user = User.query.filter_by(email=email).first()
    if not user:
        return jsonify({'error': 'User not found'}), 404
        
    user.is_verified = True
    
    # Delete used OTP
    db.session.delete(otp_record)
    db.session.commit()
    
    # Login successful
    access_token = create_access_token(identity=str(user.id))
    
    log_event('VERIFY_OTP', user_id=user.id, role=user.role.value, metadata={'email': user.email})
    
    return jsonify({
        'message': 'Verification successful',
        'access_token': access_token,
        'user': user.to_dict()
    }), 200

@bp.route('/resend-otp', methods=['POST'])
def resend_otp():
    """Resend OTP"""
    data = request.get_json()
    
    if not data or not data.get('email'):
        return jsonify({'error': 'Email is required'}), 400
        
    email = data['email'].lower().strip()
    is_valid_email, email_error = validate_email(email)
    if not is_valid_email:
        return jsonify({'error': email_error}), 400

    user = User.query.filter_by(email=email).first()
    if not user:
        return jsonify({'error': 'User not found'}), 404
        
    if user.is_verified:
        return jsonify({'message': 'User already verified'}), 200
        
    # Generate new OTP
    otp_code = ''.join([str(random.randint(0, 9)) for _ in range(6)])
    expires_at = datetime.utcnow() + timedelta(minutes=5)
    
    # Delete old OTPs for this user
    OTP.query.filter_by(identifier=user.email).delete()
    
    otp = OTP(identifier=user.email, expires_at=expires_at)
    otp.set_otp(otp_code)
    db.session.add(otp)
    db.session.commit()
    
    # SEND GMAIL OTP
    if not send_otp_email(user.email, otp_code):
        return jsonify({'error': 'Failed to send verification email. Please check your email address or try again later.'}), 500
    
    return jsonify({
        'message': 'OTP resent successfully'
    }), 200

@bp.route('/forgot-password', methods=['POST'])
def forgot_password():
    """Send OTP for password reset"""
    data = request.get_json()
    if not data or not data.get('email'):
        return jsonify({'error': 'Email is required'}), 400
        
    email = data['email'].lower().strip()
    
    # 1. Validate email format & typos
    is_valid_email, email_error = validate_email(email)
    if not is_valid_email:
        return jsonify({'error': email_error}), 400
        
    user = User.query.filter_by(email=email).first()
    if not user:
        return jsonify({'error': 'Email not found in our records'}), 404
        
    # Generate OTP
    otp_code = ''.join([str(random.randint(0, 9)) for _ in range(6)])
    expires_at = datetime.utcnow() + timedelta(minutes=5)
    
    # Delete old OTPs for this user
    OTP.query.filter_by(identifier=user.email).delete()
    
    otp = OTP(identifier=user.email, expires_at=expires_at)
    otp.set_otp(otp_code)
    db.session.add(otp)
    db.session.commit()
    
    # Send Email (Synchronous)
    if not send_password_reset_otp(user.email, otp_code):
        return jsonify({'error': 'Failed to send password reset email. Please try again later.'}), 500
    
    return jsonify({
        'message': 'OTP sent to your email for password reset'
    }), 200

@bp.route('/verify-reset-otp', methods=['POST'])
def verify_reset_otp():
    """Verify OTP for password reset without changing it yet"""
    data = request.get_json()
    if not data or not data.get('email') or not data.get('otp'):
        return jsonify({'error': 'Email and OTP are required'}), 400
        
    email = data['email'].lower()
    otp_record = OTP.query.filter_by(identifier=email).order_by(OTP.created_at.desc()).first()
    
    if not otp_record or not otp_record.is_valid():
        return jsonify({'error': 'Invalid, expired, or blocked OTP'}), 400
        
    # Increment attempts
    otp_record.attempts += 1
    db.session.commit()
    
    if not otp_record.check_otp(data['otp']):
        if otp_record.attempts >= 3:
            return jsonify({'error': 'Too many failed attempts. This OTP is now invalid.'}), 400
        return jsonify({'error': f'Invalid OTP. {3 - otp_record.attempts} attempts remaining.'}), 400
        
    return jsonify({'message': 'OTP verified successfully. Proceed to reset password.'}), 200

@bp.route('/reset-password', methods=['POST'])
def reset_password():
    """Reset password after OTP verification"""
    data = request.get_json()
    if not data or not data.get('email') or not data.get('otp') or not data.get('password'):
        return jsonify({'error': 'Email, OTP, and new password are required'}), 400
        
    email = data['email'].lower()
    otp_record = OTP.query.filter_by(identifier=email).order_by(OTP.created_at.desc()).first()
    
    if not otp_record:
        return jsonify({'error': 'No OTP found. Please request a new one.'}), 400
    
    # Only check expiration, not attempts (verify_reset_otp already incremented attempts)
    if datetime.utcnow() >= otp_record.expires_at:
        return jsonify({'error': 'OTP has expired. Please request a new one.'}), 400
    
    if not otp_record.check_otp(data['otp']):
        return jsonify({'error': 'Invalid OTP'}), 400
        
    user = User.query.filter_by(email=email).first()
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    # Validate new password strength
    is_valid, errors = validate_password(
        password=data['password'],
        name=user.name,
        email=email
    )
    if not is_valid:
        return jsonify({'error': errors[0] if errors else 'Password does not meet requirements'}), 400
        
    # Reset Password
    user.set_password(data['password'])
    
    # Mark as verified - user proved email ownership by verifying OTP
    user.is_verified = True
    
    # Delete OTP
    db.session.delete(otp_record)
    db.session.commit()
    
    return jsonify({'message': 'Password reset successful. Please log in.'}), 200

@bp.route('/profile', methods=['PATCH'])
@jwt_required()
def update_profile():
    """Update user profile information"""
    user_id = int(get_jwt_identity())
    user = User.query.get(user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    data = request.get_json()
    
    # Update allowed fields based on profile
    if user.role == UserRole.STUDENT:
        profile = user.student_profile
        if not profile:
            profile = Student(id=user.id)
            db.session.add(profile)
    else:
        profile = user.admin_profile
        if not profile:
            profile = Admin(id=user.id, admin_type=user.role.value)
            db.session.add(profile)

    if data.get('name'):
        profile.name = data['name']
    
    if user.role == UserRole.STUDENT:
        if data.get('college_pin'):
            profile.college_pin = data['college_pin']
        if data.get('department'):
            profile.department = data['department']
        if data.get('year'):
            profile.year = data['year']
        if data.get('branch'):
            profile.branch = data['branch']
    
    db.session.commit()
    
    return jsonify(user.to_dict()), 200

@bp.route('/profile/picture', methods=['POST'])
@jwt_required()
def upload_profile_picture():
    """Upload user profile picture"""
    import os
    from werkzeug.utils import secure_filename
    
    user_id = int(get_jwt_identity())
    user = User.query.get(user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    if 'picture' not in request.files:
        return jsonify({'error': 'No picture file provided'}), 400
    
    file = request.files['picture']
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
    
    # Validate file extension
    allowed_extensions = {'png', 'jpg', 'jpeg', 'gif', 'webp'}
    file_ext = file.filename.rsplit('.', 1)[-1].lower() if '.' in file.filename else ''
    if file_ext not in allowed_extensions:
        return jsonify({'error': 'Invalid file type. Allowed: png, jpg, jpeg, gif, webp'}), 400
    
    # Create profiles upload directory if it doesn't exist
    upload_folder = os.path.join(current_app.config['UPLOAD_FOLDER'], 'profiles')
    os.makedirs(upload_folder, exist_ok=True)
    
    # Generate unique filename
    filename = f"profile_{user_id}_{int(datetime.now().timestamp())}.{file_ext}"
    filepath = os.path.join(upload_folder, filename)
    
    # Delete old profile picture if exists
    if user.profile_picture:
        old_path = os.path.join(current_app.config['UPLOAD_FOLDER'], user.profile_picture.replace('/uploads/', '', 1))
        if os.path.exists(old_path):
            try:
                os.remove(old_path)
            except:
                pass  # Ignore deletion errors
    
    # Save new file
    file.save(filepath)
    
    # Update profile picture path in appropriate profile table
    profile_path = f"/uploads/profiles/{filename}"
    
    if user.role == UserRole.STUDENT:
        if not user.student_profile:
            user.student_profile = Student(id=user.id)
        user.student_profile.profile_picture = profile_path
    else:
        if not user.admin_profile:
            user.admin_profile = Admin(id=user.id, admin_type=user.role.value)
        user.admin_profile.profile_picture = profile_path
        
    db.session.commit()
    
    return jsonify({
        'message': 'Profile picture uploaded successfully',
        'profile_picture': profile_path
    }), 200

@bp.route('/admin-requests', methods=['GET'])
@jwt_required()
def get_admin_requests():
    """Get all pending admin registration requests (Main Admin only)"""
    user_id = int(get_jwt_identity())
    current_user = User.query.get(user_id)
    
    if not current_user or current_user.role != UserRole.MAIN_ADMIN:
        return jsonify({'error': 'Unauthorized. Main Admin access only.'}), 403
        
    requests = AdminRequest.query.filter_by(status=RequestStatus.PENDING).all()
    return jsonify([req.to_dict() for req in requests]), 200

@bp.route('/approve-admin/<int:request_id>', methods=['POST'])
@jwt_required()
def approve_admin(request_id):
    """Approve or reject an admin registration request (Main Admin only)"""
    user_id = int(get_jwt_identity())
    current_user = User.query.get(user_id)
    
    if not current_user or current_user.role != UserRole.MAIN_ADMIN:
        return jsonify({'error': 'Unauthorized. Main Admin access only.'}), 403
        
    admin_req = AdminRequest.query.get_or_404(request_id)
    if admin_req.status != RequestStatus.PENDING:
        return jsonify({'error': 'Request has already been processed.'}), 400
        
    data = request.get_json()
    action = data.get('action') # 'APPROVE' or 'REJECT'
    
    if action == 'APPROVE':
        # Create User
        user = User(
            email=admin_req.email,
            role=UserRole.ADMIN,
            is_verified=True # Pre-verify since approved by main admin
        )
        user.password = admin_req.password # Already hashed
        db.session.add(user)
        db.session.flush()
        
        # Create Admin Profile
        profile = Admin(
            id=user.id,
            name=admin_req.name,
            admin_type="ADMIN"
        )
        db.session.add(profile)
        
        admin_req.status = RequestStatus.APPROVED
        db.session.commit()
        
        # Notify admin via email
        from app.utils.email import send_admin_approval_email
        send_admin_approval_email(user.email, profile.name)
        
        return jsonify({'message': f'Admin request for {admin_req.name} approved successfully.'}), 200
        
    elif action == 'REJECT':
        admin_req.status = RequestStatus.REJECTED
        db.session.commit()
        return jsonify({'message': f'Admin request for {admin_req.name} rejected.'}), 200
    
    else:
        return jsonify({'error': "Invalid action. Use 'APPROVE' or 'REJECT'."}), 400

# Password Reset Flow

@bp.route('/request-password-reset', methods=['POST'])
@jwt_required()
def request_password_reset():
    """Request a password reset OTP (Authenticated user from Profile)"""
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
        
    # Generate 6-digit OTP
    otp_code = str(random.randint(100000, 999999))
    
    # Store OTP
    
    # Check for existing OTP to prevent spam (optional rate limiting logic could go here)
    # For now, just create a new one
    
    otp_entry = OTP(
        identifier=user.email,
        expires_at=datetime.utcnow() + timedelta(minutes=5)
    )
    otp_entry.set_otp(otp_code)
    
    db.session.add(otp_entry)
    db.session.commit()
    
    # Send Email
    from app.utils.email import send_password_reset_otp
    send_password_reset_otp(user.email, otp_code)
    
    return jsonify({'message': f'OTP sent to {user.email}'}), 200

@bp.route('/verify-profile-reset-otp', methods=['POST'])
@jwt_required()
def verify_profile_reset_otp():
    """Verify OTP and return a reset token"""
    data = request.get_json()
    otp_input = data.get('otp')
    
    if not otp_input:
        return jsonify({'error': 'OTP is required'}), 400
        
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
        
    # Find latest valid OTP
    
    otp_record = OTP.query.filter_by(identifier=user.email)\
        .order_by(OTP.created_at.desc())\
        .first()
        
    if not otp_record or not otp_record.is_valid():
        return jsonify({'error': 'Invalid or expired OTP'}), 400
        
    if not otp_record.check_otp(otp_input):
        otp_record.attempts += 1
        db.session.commit()
        return jsonify({'error': 'Invalid OTP'}), 400
        
    # OTP Valid - Create Reset Token
    # We can use a short-lived JWT with a special claim 'type': 'reset'
    reset_token = create_access_token(
        identity=user.id, 
        expires_delta=timedelta(minutes=5),
        additional_claims={'type': 'password_reset'}
    )
    
    # Invalidate OTP immediately to prevent reuse
    db.session.delete(otp_record)
    db.session.commit()
    
    return jsonify({
        'message': 'OTP verified',
        'reset_token': reset_token
    }), 200

@bp.route('/complete-password-reset', methods=['POST'])
@jwt_required()
def complete_password_reset():
    """Set new password using the reset token"""
    # Verify this is a reset token
    from flask_jwt_extended import get_jwt
    claims = get_jwt()
    if claims.get('type') != 'password_reset':
        return jsonify({'error': 'Invalid token type for password reset'}), 403
        
    data = request.get_json()
    new_password = data.get('new_password')
    
    if not new_password:
        return jsonify({'error': 'New password is required'}), 400
        
    # Validate password strength
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404

    is_valid, errors = validate_password(new_password, user.name or "User", user.email)
    if not is_valid:
         return jsonify({'error': errors[0] if errors else 'Password validation failed'}), 400
         
    # Update Password
    user.set_password(new_password)
    db.session.commit()
    
    # Send Confirmation
    from app.utils.email import send_password_reset_success
    send_password_reset_success(user.email)
    
    return jsonify({'message': 'Password updated successfully'}), 200
