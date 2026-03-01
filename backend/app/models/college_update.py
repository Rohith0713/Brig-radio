from app.extensions import db
from datetime import datetime

class CollegeUpdate(db.Model):
    __tablename__ = 'college_updates'
    
    id = db.Column(db.Integer, primary_key=True)
    admin_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    image_url = db.Column(db.String(500), nullable=False)
    media_type = db.Column(db.String(20), default='IMAGE', nullable=False) # 'IMAGE' or 'VIDEO'
    caption = db.Column(db.Text, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    
    # Relationships
    likes = db.relationship('CollegeUpdateLike', backref='update', lazy='dynamic', cascade="all, delete-orphan")
    views = db.relationship('CollegeUpdateView', backref='update', lazy='dynamic', cascade="all, delete-orphan")
    
    def to_dict(self, current_user_id=None):
        data = {
            'id': self.id,
            'image_url': self.image_url,
            'media_type': self.media_type,
            'caption': self.caption,
            'created_at': self.created_at.isoformat(),
            'like_count': self.likes.count(),
            'view_count': self.views.count(),
            'is_liked': False
        }
        
        if current_user_id:
            data['is_liked'] = self.likes.filter_by(student_id=current_user_id).first() is not None
            
        return data

# Removed CollegeUpdateComment


class CollegeUpdateView(db.Model):
    __tablename__ = 'college_update_views'
    
    id = db.Column(db.Integer, primary_key=True)
    update_id = db.Column(db.Integer, db.ForeignKey('college_updates.id'), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    __table_args__ = (db.UniqueConstraint('update_id', 'user_id', name='unique_user_update_view'),)

class CollegeUpdateLike(db.Model):
    __tablename__ = 'college_update_likes'
    
    id = db.Column(db.Integer, primary_key=True)
    update_id = db.Column(db.Integer, db.ForeignKey('college_updates.id'), nullable=False)
    student_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    __table_args__ = (db.UniqueConstraint('update_id', 'student_id', name='unique_user_update_like'),)
