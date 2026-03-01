from flask import Blueprint, jsonify
from flask_jwt_extended import jwt_required
from datetime import datetime, timedelta
from sqlalchemy import func
from app.extensions import db
from app.models.radio import Radio, RadioStatus
from app.routes.radios import sync_radio_statuses
from app.models.user import User, UserRole
from app.models.comment import Comment
from app.models.favorite import Favorite
from app.models.category import Category
from app.models.college_update import CollegeUpdate, CollegeUpdateLike, CollegeUpdateView
from app.models.placement import Placement
from app.models.system_event import SystemEvent
from app.middleware.auth import admin_required

bp = Blueprint('analytics', __name__, url_prefix='/api')

@bp.route('/admin/analytics', methods=['GET'])
@admin_required
def get_admin_analytics():
    """Retrieve system-wide analytics for administrators (Frontend compatible)"""
    sync_radio_statuses()
    # User distribution by role
    users_by_role = db.session.query(User.role, func.count(User.id)).group_by(User.role).all()
    users_by_role_dict = {r[0].value if hasattr(r[0], 'value') else str(r[0]): r[1] for r in users_by_role}
    
    total_users = sum(users_by_role_dict.values())
    
    # 2. Total Radios and Engagement
    total_radios = Radio.query.count()
    total_participants = db.session.query(func.sum(Radio.participant_count)).scalar() or 0
    total_favorites = Favorite.query.count()
    
    # 3. Registration Trend (Last 7 Days)
    reg_trend = []
    now = datetime.utcnow()
    for i in range(6, -1, -1):
        day = now - timedelta(days=i)
        day_start = day.replace(hour=0, minute=0, second=0, microsecond=0)
        day_end = day_start + timedelta(days=1)
        date_str = day_start.strftime('%Y-%m-%d')
        
        count = User.query.filter(
            User.created_at >= day_start,
            User.created_at < day_end
        ).count()
        reg_trend.append({'date': date_str, 'count': count})
        
    # 4. Top Performing Radios (participant_count is a property, so we fetch and sort in Python)
    all_radios = Radio.query.all()
    sorted_radios = sorted(all_radios, key=lambda r: r.participant_count, reverse=True)[:5]
    top_radios = [{
        'id': r.id, 
        'title': r.title, 
        'participant_count': r.participant_count, 
        'favorite_count': r.favorite_count
    } for r in sorted_radios]
    
    # 5. College Updates Stats
    total_update_views = CollegeUpdateView.query.count()
    total_update_likes = CollegeUpdateLike.query.count()
    total_update_comments = db.session.query(func.count(SystemEvent.id)).filter_by(event_type='COMMENT_ADDED').scalar() or 0

    
    # 6. Top College Updates
    top_updates_data = CollegeUpdate.query.limit(5).all()
    top_updates = [{
        'id': u.id,
        'caption': u.caption[:50] + '...' if len(u.caption) > 50 else u.caption,
        'view_count': u.views.count(),
        'like_count': u.likes.count(),
        'comment_count': db.session.query(func.count(SystemEvent.id)).filter(
            SystemEvent.event_type == 'COMMENT_ADDED',
            SystemEvent.metadata_json.like(f'%"update_id": {u.id}%')
        ).scalar() or 0
    } for u in top_updates_data]

    # 7. Placement Stats (handle case where table doesn't exist yet)
    try:
        total_placements = Placement.query.count()
    except Exception:
        total_placements = 0

    return jsonify({
        'total_users': total_users,
        'users_by_role': users_by_role_dict,
        'total_radios': total_radios,
        'total_participants': total_participants,
        'total_favorites': total_favorites,
        'total_update_views': total_update_views,
        'total_update_likes': total_update_likes,
        'total_update_comments': total_update_comments,
        'total_placements': total_placements,

        'registration_trend': reg_trend,
        'top_radios': top_radios,
        'top_updates': top_updates,
        'departmental_context': None
    }), 200


@bp.route('/analytics/overview', methods=['GET'])
@admin_required
def get_overview():
    """Get overview analytics for admin dashboard"""
    sync_radio_statuses()
    now = datetime.now()
    week_ago = now - timedelta(days=7)
    month_ago = now - timedelta(days=30)
    
    # Total counts
    total_radios = Radio.query.count()
    total_users = User.query.count()
    total_students = User.query.filter_by(role=UserRole.STUDENT).count()
    total_admins = User.query.filter_by(role=UserRole.ADMIN).count()
    total_comments = Comment.query.count()
    total_favorites = Favorite.query.count()
    
    # Radio stats
    live_radios = Radio.query.filter_by(status=RadioStatus.LIVE).count()
    upcoming_radios = Radio.query.filter_by(status=RadioStatus.UPCOMING).count()
    completed_radios = Radio.query.filter_by(status=RadioStatus.COMPLETED).count()
    
    # Recent activity
    radios_this_week = Radio.query.filter(Radio.created_at >= week_ago).count()
    radios_this_month = Radio.query.filter(Radio.created_at >= month_ago).count()
    users_this_week = User.query.filter(User.created_at >= week_ago).count()
    comments_this_week = Comment.query.filter(Comment.created_at >= week_ago).count()
    
    return jsonify({
        'totals': {
            'radios': total_radios,
            'users': total_users,
            'students': total_students,
            'admins': total_admins,
            'comments': total_comments,
            'favorites': total_favorites
        },
        'radios': {
            'live': live_radios,
            'upcoming': upcoming_radios,
            'completed': completed_radios
        },
        'recent': {
            'radios_this_week': radios_this_week,
            'radios_this_month': radios_this_month,
            'users_this_week': users_this_week,
            'comments_this_week': comments_this_week
        }
    }), 200


@bp.route('/analytics/radios', methods=['GET'])
@admin_required
def get_radio_analytics():
    """Get detailed radio analytics"""
    sync_radio_statuses()
    # Top radios by favorites
    top_favorited = db.session.query(
        Radio.id, Radio.title, func.count(Favorite.radio_id).label('favorite_count')
    ).outerjoin(Favorite).group_by(Radio.id).order_by(
        func.count(Favorite.radio_id).desc()
    ).limit(10).all()
    
    # Top radios by comments
    top_commented = db.session.query(
        Radio.id, Radio.title, func.count(Comment.radio_id).label('comment_count')
    ).outerjoin(Comment).group_by(Radio.id).order_by(
        func.count(Comment.radio_id).desc()
    ).limit(10).all()
    
    # Radios by category
    radios_by_category = db.session.query(
        Category.name, func.count(Radio.id).label('count')
    ).outerjoin(Radio).group_by(Category.id).all()
    
    # Radios by status
    radios_by_status = db.session.query(
        Radio.status, func.count(Radio.id).label('count')
    ).group_by(Radio.status).all()
    
    return jsonify({
        'top_favorited': [
            {'id': r.id, 'title': r.title, 'favorites': r.favorite_count}
            for r in top_favorited
        ],
        'top_commented': [
            {'id': r.id, 'title': r.title, 'comments': r.comment_count}
            for r in top_commented
        ],
        'by_category': [
            {'category': c.name or 'Uncategorized', 'count': c.count}
            for c in radios_by_category
        ],
        'by_status': [
            {'status': s.status.value if s.status else 'Unknown', 'count': s.count}
            for s in radios_by_status
        ]
    }), 200


@bp.route('/analytics/trends', methods=['GET'])
@admin_required
def get_trends():
    """Get weekly/monthly trends"""
    now = datetime.now()
    
    # Radios created per day (last 7 days)
    daily_radios = []
    for i in range(7):
        day = now - timedelta(days=i)
        day_start = day.replace(hour=0, minute=0, second=0, microsecond=0)
        day_end = day_start + timedelta(days=1)
        count = Radio.query.filter(
            Radio.created_at >= day_start,
            Radio.created_at < day_end
        ).count()
        daily_radios.append({
            'date': day_start.strftime('%Y-%m-%d'),
            'count': count
        })
    
    # User signups per day (last 7 days)
    daily_users = []
    for i in range(7):
        day = now - timedelta(days=i)
        day_start = day.replace(hour=0, minute=0, second=0, microsecond=0)
        day_end = day_start + timedelta(days=1)
        count = User.query.filter(
            User.created_at >= day_start,
            User.created_at < day_end
        ).count()
        daily_users.append({
            'date': day_start.strftime('%Y-%m-%d'),
            'count': count
        })
    
    return jsonify({
        'daily_radios': list(reversed(daily_radios)),
        'daily_users': list(reversed(daily_users))
    }), 200
