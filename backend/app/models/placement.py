from app.extensions import db
from datetime import datetime

class Placement(db.Model):
    __tablename__ = 'placements'
    
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(255), nullable=False)
    company = db.Column(db.String(255), nullable=False)
    location = db.Column(db.String(255), nullable=False)
    salary = db.Column(db.String(100), nullable=True)
    applicants_count = db.Column(db.Integer, default=0)
    deadline = db.Column(db.String(100), nullable=True)
    description = db.Column(db.Text, nullable=True)
    application_link = db.Column(db.String(500), nullable=True)
    posted_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    def to_dict(self):
        return {
            'id': self.id,
            'title': self.title,
            'company': self.company,
            'location': self.location,
            'salary': self.salary,
            'applicantsCount': self.applicants_count,
            'deadline': self.deadline,
            'description': self.description,
            'applicationLink': self.application_link,
            'postedAt': self.posted_at.strftime('%Y-%m-%d %H:%M:%S') if self.posted_at else None
        }

    def __repr__(self):
        return f'<Placement {self.title} at {self.company}>'

class PlacementPoster(db.Model):
    __tablename__ = 'placement_posters'
    
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(255), nullable=False)
    company = db.Column(db.String(255), nullable=True)
    description = db.Column(db.Text, nullable=True)
    poster_image = db.Column(db.String(255), nullable=False)
    is_visible = db.Column(db.Boolean, default=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    def to_dict(self):
        return {
            'id': self.id,
            'title': self.title,
            'company': self.company,
            'description': self.description,
            'posterImage': self.poster_image,
            'isVisible': self.is_visible,
            'createdAt': self.created_at.strftime('%Y-%m-%d %H:%M:%S') if self.created_at else None
        }

class PlacementBookmark(db.Model):
    __tablename__ = 'placement_bookmarks'
    
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), primary_key=True)
    placement_id = db.Column(db.Integer, db.ForeignKey('placements.id'), primary_key=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relationships
    user = db.relationship('User', backref=db.backref('placement_bookmarks', lazy='dynamic'))
    placement = db.relationship('Placement', backref=db.backref('bookmarked_by', lazy='dynamic'))
    
    def to_dict(self):
        return {
            'user_id': self.user_id,
            'placement_id': self.placement_id,
            'created_at': self.created_at.strftime('%Y-%m-%d %H:%M:%S') if self.created_at else None
        }
