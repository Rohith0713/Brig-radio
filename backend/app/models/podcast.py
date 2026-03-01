from app.extensions import db
from datetime import datetime
import enum


class PodcastStatus(enum.Enum):
    SCHEDULED = "SCHEDULED"
    LIVE = "LIVE"
    ENDED = "ENDED"


class HandRaiseStatus(enum.Enum):
    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    IGNORED = "IGNORED"


# Association table for podcast viewers
podcast_viewers = db.Table('podcast_viewers',
    db.Column('podcast_id', db.Integer, db.ForeignKey('podcasts.id'), primary_key=True),
    db.Column('user_id', db.Integer, db.ForeignKey('users.id'), primary_key=True),
    db.Column('joined_at', db.DateTime, default=datetime.now)
)


class Podcast(db.Model):
    __tablename__ = 'podcasts'
    
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(200), nullable=False)
    description = db.Column(db.Text)
    scheduled_start_time = db.Column(db.DateTime, nullable=False, index=True)
    status = db.Column(db.Enum(PodcastStatus), nullable=False, default=PodcastStatus.SCHEDULED, index=True)
    is_muted = db.Column(db.Boolean, default=False)
    created_by = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    started_at = db.Column(db.DateTime, nullable=True)
    ended_at = db.Column(db.DateTime, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.now)
    updated_at = db.Column(db.DateTime, default=datetime.now, onupdate=datetime.now)
    
    # Relationships
    viewers = db.relationship('User', secondary=podcast_viewers, backref='viewed_podcasts', lazy='dynamic')
    creator = db.relationship('User', foreign_keys=[created_by], backref='created_podcasts')
    hand_raises = db.relationship('HandRaise', backref='podcast', lazy='dynamic', cascade='all, delete-orphan')
    
    @property
    def viewer_count(self):
        """Get count of current viewers"""
        return self.viewers.count()
    
    def to_dict(self, include_creator=False):
        """Convert to dictionary for JSON response"""
        result = {
            'id': self.id,
            'title': self.title,
            'description': self.description,
            'scheduled_start_time': self.scheduled_start_time.isoformat() if self.scheduled_start_time else None,
            'status': self.status.value,
            'is_muted': self.is_muted,
            'created_by': self.created_by,
            'started_at': self.started_at.isoformat() if self.started_at else None,
            'ended_at': self.ended_at.isoformat() if self.ended_at else None,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None,
            'viewer_count': self.viewer_count
        }
        
        if include_creator and self.creator:
            result['creator_name'] = self.creator.name
            result['creator_email'] = self.creator.email
        
        return result
    
    def __repr__(self):
        return f'<Podcast {self.title}>'


class HandRaise(db.Model):
    __tablename__ = 'hand_raises'
    
    id = db.Column(db.Integer, primary_key=True)
    podcast_id = db.Column(db.Integer, db.ForeignKey('podcasts.id'), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    status = db.Column(db.Enum(HandRaiseStatus), nullable=False, default=HandRaiseStatus.PENDING)
    created_at = db.Column(db.DateTime, default=datetime.now)
    responded_at = db.Column(db.DateTime, nullable=True)
    
    # Relationships
    user = db.relationship('User', backref='hand_raises')
    
    def to_dict(self, include_user=True):
        """Convert to dictionary for JSON response"""
        result = {
            'id': self.id,
            'podcast_id': self.podcast_id,
            'user_id': self.user_id,
            'status': self.status.value,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'responded_at': self.responded_at.isoformat() if self.responded_at else None
        }
        
        if include_user and self.user:
            result['user_name'] = self.user.name
            result['user_email'] = self.user.email
        
        return result
    
    def __repr__(self):
        return f'<HandRaise podcast={self.podcast_id} user={self.user_id}>'
