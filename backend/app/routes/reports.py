from flask import Blueprint, request, jsonify
from app import db
from app.models.report import Report
from app.models.radio import Radio  # Ensure explicit import for relationship
from app.middleware.auth import token_required
from app.utils.analytics_helper import log_event

reports_bp = Blueprint('reports', __name__)

@reports_bp.route('/', methods=['POST'])
@token_required
def create_report(current_user):
    data = request.get_json()
    
    # Extract data
    session_id = data.get('session_id')
    issue_type = data.get('issue_type')
    description = data.get('description')
    
    if not issue_type:
        return jsonify({'message': 'Issue type is required'}), 400
        
    # Verify session exists if provided
    if session_id:
        radio = Radio.query.get(session_id)
        if not radio:
            return jsonify({'message': 'Radio session not found'}), 404
            
    try:
        new_report = Report(
            student_id=current_user.id,
            session_id=session_id,
            issue_type=issue_type,
            description=description
        )
        
        db.session.add(new_report)
        db.session.commit()
        
        # In a real app, notify admin here (e.g., email or notification)
        print(f"NEW REPORT: Student {current_user.name} reported {issue_type} for Session ID {session_id}")
        
        log_event('REPORT_SUBMITTED', user_id=current_user.id, role=current_user.role, metadata={'issue_type': issue_type, 'session_id': session_id})
        
        return jsonify({
            'message': 'Report submitted successfully',
            'report': new_report.to_dict()
        }), 201
        
    except Exception as e:
        print(f"Error creating report: {e}")
        return jsonify({'message': 'Internal Message: Failed to submit report'}), 500

@reports_bp.route('/', methods=['GET'])
@token_required
def get_reports(current_user):
    # Admin only check (if we had check_admin middleware, use that)
    if current_user.role != 'ADMIN':
        return jsonify({'message': 'Unauthorized'}), 403
        
    reports = Report.query.order_by(Report.created_at.desc()).all()
    return jsonify([report.to_dict() for report in reports]), 200
