from app.extensions import db
from datetime import datetime

class Marquee(db.Model):
    __tablename__ = 'marquees'
    
    id = db.Column(db.Integer, primary_key=True)
    message = db.Column(db.String(500), nullable=False)
    is_active = db.Column(db.Boolean, default=False)
    text_color = db.Column(db.String(20), default='#6366F1')
    font_style = db.Column(db.String(20), default='Bold')
    font_size = db.Column(db.String(10), default='Medium')
    bg_color = db.Column(db.String(20), default='#6366F1')
    bg_gradient_end = db.Column(db.String(20), nullable=True)
    scroll_speed = db.Column(db.String(10), default='Normal')
    text_alignment = db.Column(db.String(10), default='Left')
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    def to_dict(self):
        return {
            'id': self.id,
            'message': self.message,
            'is_active': self.is_active,
            'text_color': self.text_color or '#6366F1',
            'font_style': self.font_style or 'Bold',
            'font_size': self.font_size or 'Medium',
            'bg_color': self.bg_color or '#6366F1',
            'bg_gradient_end': self.bg_gradient_end,
            'scroll_speed': self.scroll_speed or 'Normal',
            'text_alignment': self.text_alignment or 'Left',
            'created_at': self.created_at.isoformat(),
            'updated_at': self.updated_at.isoformat()
        }
