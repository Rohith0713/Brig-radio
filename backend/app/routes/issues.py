from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from datetime import datetime
from app.extensions import db
from app.models.issue import Issue, IssueStatus, IssueMessage
from app.models.user import User, UserRole
from app.models.notification import Notification
from app.middleware.auth import admin_required, student_required
from app.utils.analytics_helper import log_event

bp = Blueprint('issues', __name__, url_prefix='/api/issues')


@bp.route('', methods=['POST'])
@student_required
def create_issue():
    """Create a new issue (student only)"""
    user_id = int(get_jwt_identity())
    data = request.get_json()
    
    # Validate required fields
    if not data or not data.get('title'):
        return jsonify({'error': 'Title is required'}), 400
    if not data.get('description'):
        return jsonify({'error': 'Description is required'}), 400
    
    # Create issue
    issue = Issue(
        title=data['title'].strip(),
        description=data['description'].strip(),
        student_id=user_id,
        status=IssueStatus.OPEN
    )
    
    db.session.add(issue)
    db.session.commit()
    
    # Notify admins about new issue
    admins = User.query.filter(User.role.in_([UserRole.ADMIN, UserRole.MAIN_ADMIN])).all()
    for admin in admins:
        notification = Notification(
            user_id=admin.id,
            title="New Issue Reported",
            message=f"A new issue has been reported: {issue.title}",
            type="ISSUE_REPORTED",
            related_id=issue.id
        )
        db.session.add(notification)
    
    db.session.commit()
    
    log_event('ISSUE_REPORTED', user_id=user_id, role='STUDENT', metadata={'issue_id': issue.id, 'title': issue.title})
    
    return jsonify({
        'message': 'Issue submitted successfully',
        'issue': issue.to_dict()
    }), 201


@bp.route('', methods=['GET'])
@admin_required
def get_all_issues():
    """Get all open/in-discussion issues (admin only)"""
    issues = Issue.query.filter(
        Issue.status.in_([IssueStatus.OPEN, IssueStatus.IN_DISCUSSION])
    ).order_by(Issue.created_at.desc()).all()
    
    return jsonify([issue.to_dict() for issue in issues]), 200


@bp.route('/resolved', methods=['GET'])
@admin_required
def get_resolved_issues():
    """Get all resolved issues (admin only)"""
    issues = Issue.query.filter_by(
        status=IssueStatus.RESOLVED
    ).order_by(Issue.resolved_at.desc()).all()
    
    return jsonify([issue.to_dict() for issue in issues]), 200


@bp.route('/my', methods=['GET'])
@student_required
def get_my_issues():
    """Get student's own issues"""
    user_id = int(get_jwt_identity())
    
    issues = Issue.query.filter_by(
        student_id=user_id
    ).order_by(Issue.created_at.desc()).all()
    
    return jsonify([issue.to_dict() for issue in issues]), 200


@bp.route('/<int:issue_id>', methods=['GET'])
@jwt_required()
def get_issue_details(issue_id):
    """Get issue details with messages"""
    user_id = int(get_jwt_identity())
    user = User.query.get(user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    issue = Issue.query.get(issue_id)
    if not issue:
        return jsonify({'error': 'Issue not found'}), 404
    
    # Check access - only owner or admin can view
    is_admin = user.role in [UserRole.ADMIN, UserRole.MAIN_ADMIN]
    is_owner = issue.student_id == user_id
    
    if not is_admin and not is_owner:
        return jsonify({'error': 'Unauthorized access'}), 403
    
    return jsonify(issue.to_dict(include_messages=True)), 200


@bp.route('/<int:issue_id>/message', methods=['POST'])
@jwt_required()
def send_message(issue_id):
    """Send a message in the issue chat"""
    user_id = int(get_jwt_identity())
    user = User.query.get(user_id)
    data = request.get_json()
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    issue = Issue.query.get(issue_id)
    if not issue:
        return jsonify({'error': 'Issue not found'}), 404
    
    # Check if issue is resolved
    if issue.status == IssueStatus.RESOLVED:
        return jsonify({'error': 'Cannot send messages to resolved issues'}), 400
    
    # Check access
    is_admin = user.role in [UserRole.ADMIN, UserRole.MAIN_ADMIN]
    is_owner = issue.student_id == user_id
    
    if not is_admin and not is_owner:
        return jsonify({'error': 'Unauthorized access'}), 403
    
    # Validate message
    if not data or not data.get('message'):
        return jsonify({'error': 'Message is required'}), 400
    
    # Determine sender role
    sender_role = 'admin' if is_admin else 'student'
    
    # Create message
    message = IssueMessage(
        issue_id=issue_id,
        sender_id=user_id,
        sender_role=sender_role,
        message=data['message'].strip()
    )
    
    db.session.add(message)
    
    # If this is the first admin reply, change status to IN_DISCUSSION
    if is_admin and issue.status == IssueStatus.OPEN:
        issue.status = IssueStatus.IN_DISCUSSION
    
    # Create notification for the other party
    if is_admin:
        # Notify student
        notification = Notification(
            user_id=issue.student_id,
            title="New Message on Your Issue",
            message=f"Admin replied to your issue: {issue.title}",
            type="ISSUE_MESSAGE",
            related_id=issue.id
        )
        db.session.add(notification)
    else:
        # Notify admins
        admins = User.query.filter(User.role.in_([UserRole.ADMIN, UserRole.MAIN_ADMIN])).all()
        for admin in admins:
            notification = Notification(
                user_id=admin.id,
                title="New Message on Issue",
                message=f"Student replied to issue: {issue.title}",
                type="ISSUE_MESSAGE",
                related_id=issue.id
            )
            db.session.add(notification)
    
    db.session.commit()
    
    return jsonify({
        'message': 'Message sent successfully',
        'issue_message': message.to_dict(),
        'issue_status': issue.status.value
    }), 201


@bp.route('/<int:issue_id>/resolve', methods=['PUT'])
@admin_required
def resolve_issue(issue_id):
    """Mark issue as resolved (admin only)"""
    issue = Issue.query.get(issue_id)
    
    if not issue:
        return jsonify({'error': 'Issue not found'}), 404
    
    if issue.status == IssueStatus.RESOLVED:
        return jsonify({'error': 'Issue is already resolved'}), 400
    
    issue.status = IssueStatus.RESOLVED
    issue.resolved_at = datetime.utcnow()
    
    # Notify student
    notification = Notification(
        user_id=issue.student_id,
        title="Issue Resolved",
        message=f"Your issue '{issue.title}' has been marked as resolved by an admin.",
        type="ISSUE_RESOLVED",
        related_id=issue.id
    )
    db.session.add(notification)
    
    db.session.commit()
    
    return jsonify({
        'message': 'Issue resolved successfully',
        'issue': issue.to_dict()
    }), 200


@bp.route('/stats', methods=['GET'])
@admin_required
def get_issue_stats():
    """Get issue statistics for admin dashboard"""
    open_count = Issue.query.filter_by(status=IssueStatus.OPEN).count()
    discussion_count = Issue.query.filter_by(status=IssueStatus.IN_DISCUSSION).count()
    resolved_count = Issue.query.filter_by(status=IssueStatus.RESOLVED).count()
    
    return jsonify({
        'open_issues': open_count,
        'in_discussion': discussion_count,
        'resolved_issues': resolved_count,
        'total_active': open_count + discussion_count
    }), 200
@bp.route('/<int:issue_id>', methods=['DELETE'])
@jwt_required()
def delete_issue(issue_id):
    """Delete an issue (admin: any issue, student: own issues only)"""
    user_id = int(get_jwt_identity())
    user = User.query.get(user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    issue = Issue.query.get(issue_id)
    if not issue:
        return jsonify({'error': 'Issue not found'}), 404
    
    # Admins can delete any issue, students can only delete their own
    is_admin = user.role in [UserRole.ADMIN, UserRole.MAIN_ADMIN]
    if not is_admin and issue.student_id != user_id:
        return jsonify({'error': 'Unauthorized to delete this issue'}), 403
    
    # Delete related messages first (if not cascading)
    IssueMessage.query.filter_by(issue_id=issue_id).delete()
    
    db.session.delete(issue)
    db.session.commit()
    
    role = 'ADMIN' if is_admin else 'STUDENT'
    log_event('ISSUE_DELETED', user_id=user_id, role=role, metadata={'issue_id': issue_id})
    
    return jsonify({'message': 'Issue deleted successfully'}), 200
