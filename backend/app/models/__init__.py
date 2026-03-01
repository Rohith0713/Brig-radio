# Import all models here for Flask-Migrate to detect them
from app.models.user import User, UserRole
from app.models.student import Student
from app.models.admin import Admin
from app.models.admin_request import AdminRequest, RequestStatus
from app.models.radio import Radio, RadioStatus, radio_participants, MediaType, HostStatus
from app.models.radio_suggestion import RadioSuggestion, SuggestionStatus
from app.models.category import Category
from app.models.favorite import Favorite
from app.models.comment import Comment
from app.models.notification import Notification
from app.models.radio_subscription import RadioSubscription
from app.models.otp import OTP
from app.models.marquee import Marquee
from app.models.podcast import Podcast, PodcastStatus, HandRaise, HandRaiseStatus, podcast_viewers
from app.models.placement import Placement, PlacementPoster
from app.models.issue import Issue, IssueStatus, IssueMessage
from app.models.system_event import SystemEvent
from app.models.banner import Banner
from app.models.college_update import CollegeUpdate, CollegeUpdateLike, CollegeUpdateView
from app.models.report import Report
from app.models.review import Review

__all__ = [
    'User', 'UserRole', 'Student', 'Admin', 'AdminRequest', 'RequestStatus',
    'Radio', 'RadioStatus', 'radio_participants', 'MediaType', 'HostStatus',
    'RadioSuggestion', 'SuggestionStatus',
    'Category', 'Favorite', 'Comment',
    'Notification', 'RadioSubscription', 'OTP', 'Marquee',
    'Podcast', 'PodcastStatus', 'HandRaise', 'HandRaiseStatus', 'podcast_viewers',
    'Placement', 'PlacementPoster',
    'Issue', 'IssueStatus', 'IssueMessage',
    'SystemEvent', 'Banner', 'CollegeUpdate', 'CollegeUpdateLike', 'CollegeUpdateView',
    'Report', 'Review'
]


