from app import db
from datetime import datetime
import enum

class IssueStatus(enum.Enum):
    OPEN = "open"
    IN_DISCUSSION = "in_discussion"
    RESOLVED = "resolved"

class Issue(db.Model):
    __tablename__ = 'issues'
    
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(200), nullable=False)
    description = db.Column(db.Text, nullable=False)
    student_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    status = db.Column(db.Enum(IssueStatus), default=IssueStatus.OPEN, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    resolved_at = db.Column(db.DateTime, nullable=True)
    
    # Relationships
    student = db.relationship('User', backref='issues')
    messages = db.relationship('IssueMessage', backref='issue', lazy='dynamic', order_by='IssueMessage.created_at')
    
    def to_dict(self, include_messages=False):
        data = {
            'id': self.id,
            'title': self.title,
            'description': self.description,
            'student_id': self.student_id,
            'student_name': self.student.name if self.student else "Unknown",
            'student_email': self.student.email if self.student else None,
            'student_roll_number': getattr(self.student.student_profile, 'roll_number', None) if self.student and hasattr(self.student, 'student_profile') and self.student.student_profile else None,
            'status': self.status.value,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'resolved_at': self.resolved_at.isoformat() if self.resolved_at else None,
            'message_count': self.messages.count() if self.messages else 0
        }
        if include_messages:
            data['messages'] = [msg.to_dict() for msg in self.messages.all()]
        return data


class IssueMessage(db.Model):
    __tablename__ = 'issue_messages'
    
    id = db.Column(db.Integer, primary_key=True)
    issue_id = db.Column(db.Integer, db.ForeignKey('issues.id'), nullable=False)
    sender_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    sender_role = db.Column(db.String(20), nullable=False)  # 'admin' or 'student'
    message = db.Column(db.Text, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relationships
    sender = db.relationship('User', backref='issue_messages')
    
    def to_dict(self):
        return {
            'id': self.id,
            'issue_id': self.issue_id,
            'sender_id': self.sender_id,
            'sender_name': self.sender.name if self.sender else "Unknown",
            'sender_role': self.sender_role,
            'message': self.message,
            'created_at': self.created_at.isoformat() if self.created_at else None
        }
