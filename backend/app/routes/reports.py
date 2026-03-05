from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.extensions import db
from app.models.report import Report
from app.models.radio import Radio
from app.models.user import User, UserRole
from app.middleware.auth import admin_required
from app.utils.analytics_helper import log_event

reports_bp = Blueprint('reports', __name__)

@reports_bp.route('/', methods=['POST'])
@jwt_required()
def create_report():
    user_id = int(get_jwt_identity())
    current_user = User.query.get(user_id)
    if not current_user:
        return jsonify({'error': 'User not found'}), 404

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

        current_app.logger.info(
            f"Report submitted: user={current_user.name} type={issue_type} session={session_id}"
        )

        log_event('REPORT_SUBMITTED', user_id=current_user.id,
                  role=current_user.role,
                  metadata={'issue_type': issue_type, 'session_id': session_id})

        return jsonify({
            'message': 'Report submitted successfully',
            'report': new_report.to_dict()
        }), 201

    except Exception as e:
        current_app.logger.error(f"Error creating report: {e}")
        return jsonify({'message': 'Failed to submit report'}), 500

@reports_bp.route('/', methods=['GET'])
@admin_required
def get_reports():
    reports = Report.query.order_by(Report.created_at.desc()).all()
    return jsonify([report.to_dict() for report in reports]), 200
