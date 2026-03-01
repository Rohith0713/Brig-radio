from app.extensions import db
from datetime import datetime
import json

class SystemEvent(db.Model):
    __tablename__ = 'system_events'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=True)
    role = db.Column(db.String(50), nullable=True)
    event_type = db.Column(db.String(100), nullable=False, index=True)
    metadata_json = db.Column(db.Text, nullable=True) # Stored as JSON string
    timestamp = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    
    # Optional relationship
    user = db.relationship('User', backref=db.backref('events', lazy='dynamic'))

    @property
    def event_metadata(self):
        if self.metadata_json:
            try:
                return json.loads(self.metadata_json)
            except:
                return {}
        return {}

    @event_metadata.setter
    def event_metadata(self, value):
        self.metadata_json = json.dumps(value)

    def to_dict(self):
        return {
            'id': self.id,
            'user_id': self.user_id,
            'role': self.role,
            'event_type': self.event_type,
            'metadata': self.event_metadata,
            'timestamp': self.timestamp.isoformat()
        }

    def __repr__(self):
        return f'<SystemEvent {self.event_type} by {self.user_id}>'
