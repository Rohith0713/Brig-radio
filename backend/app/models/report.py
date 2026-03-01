from app import db
from datetime import datetime

class Report(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    student_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    session_id = db.Column(db.Integer, db.ForeignKey('radios.id'), nullable=True)
    issue_type = db.Column(db.String(50), nullable=False)
    description = db.Column(db.Text, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    # Relationships
    student = db.relationship('User', backref='reports')
    radio = db.relationship('Radio', backref='reports')

    def to_dict(self):
        return {
            'id': self.id,
            'student_id': self.student_id,
            'student_name': self.student.name if self.student else "Unknown",
            'session_id': self.session_id,
            'session_title': self.radio.title if self.radio else "General",
            'issue_type': self.issue_type,
            'description': self.description,
            'created_at': self.created_at.isoformat()
        }
