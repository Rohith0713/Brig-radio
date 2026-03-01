from datetime import datetime, timedelta
from dotenv import load_dotenv
from app import create_app
from app.extensions import db
from app.models.user import User, UserRole
from app.models.radio import Radio, RadioStatus
from app.models.radio_suggestion import RadioSuggestion, SuggestionStatus

from app.models.category import Category

# Load environment variables BEFORE creating app
load_dotenv()

def seed_database():
    """Seed the database with initial data"""
    app = create_app()
    
    with app.app_context():
        # Clear specific existing data instead of dropping everything
        # This preserves Placements and other tables not mentioned here
        print("Clearing specific existing data for re-seeding...")
        
        from app.models.radio_subscription import RadioSubscription
        from app.models.favorite import Favorite
        from app.models.comment import Comment
        from app.models.notification import Notification
        from app.models.student import Student
        from app.models.admin import Admin
        from sqlalchemy import text

        try:
            # Disable FK checks to allow broad deletion
            db.session.execute(text("SET FOREIGN_KEY_CHECKS = 0"))
            
            db.session.query(RadioSubscription).delete()
            db.session.query(Favorite).delete()
            db.session.query(Comment).delete()
            db.session.query(Notification).delete()
            db.session.query(RadioSuggestion).delete()
            
            # Clear many-to-many radio_participants
            from app.models.radio import radio_participants
            db.session.execute(radio_participants.delete())
            
            db.session.query(Radio).delete()
            db.session.query(Category).delete()
            
            # We also clear users to ensure the default admin/students are recreated correctly
            # But we KEEP Placements by not touching that table
            db.session.query(Student).delete()
            db.session.query(Admin).delete()
            db.session.query(User).delete()
            
            db.session.commit()
        finally:
            # Re-enable FK checks
            db.session.execute(text("SET FOREIGN_KEY_CHECKS = 1"))
            db.session.commit()
        
        # Ensure all tables exist (in case any were missing)
        db.create_all()
        
        # All, Academic, Co-Curricular, Extra-Curricular, Music, Talk Show, Guest Lecture, Branch Sponsored
        category_data = [
            {'name': 'All', 'color': '#8898AA', 'icon': 'apps'},
            {'name': 'Academic', 'color': '#5E72E4', 'icon': 'school'},
            {'name': 'Co-Curricular', 'color': '#11CDEF', 'icon': 'groups'},
            {'name': 'Extra-Curricular', 'color': '#2DCE89', 'icon': 'sports_soccer'},
            {'name': 'Music', 'color': '#F5365C', 'icon': 'music_note'},
            {'name': 'Talk Show', 'color': '#FB6340', 'icon': 'mic'},
            {'name': 'Guest Lecture', 'color': '#8965E0', 'icon': 'person'},
            {'name': 'Branch Sponsored', 'color': '#32325D', 'icon': 'stars'}
        ]
        
        categories = {}
        for cat_info in category_data:
            cat = Category(name=cat_info['name'], color=cat_info['color'], icon=cat_info['icon'])
            db.session.add(cat)
            categories[cat_info['name']] = cat
        
        db.session.commit()
        print(f"Created {len(category_data)} categories")
        
        print("Creating users...")
        # Create admin user
        admin = User(
            email='admin@campuswave.com',
            role=UserRole.ADMIN,
            is_verified=True
        )
        admin.set_password('admin123')
        db.session.add(admin)
        db.session.flush() # Get admin.id

        admin_profile = Admin(
            id=admin.id,
            name='Admin User',
            admin_type='ADMIN'
        )
        db.session.add(admin_profile)
        
        # Create student users
        students = []
        for i in range(1, 6):
            student = User(
                email=f'student{i}@campuswave.com',
                role=UserRole.STUDENT,
                is_verified=True
            )
            student.set_password('student123')
            db.session.add(student)
            db.session.flush()

            student_profile = Student(
                id=student.id,
                name=f'Student {i}',
                college_pin=f'PIN00{i}'
            )
            db.session.add(student_profile)
            students.append(student)
        
        db.session.commit()
        print(f"Created {len(students) + 1} users with profiles")
        
        print("Creating radios...")
        # Create live radios
        live_radio1 = Radio(
            title='Tech Talk: AI in Education',
            description='Join us for an exciting discussion about artificial intelligence in modern education',
            location='Main Auditorium',
            start_time=datetime.utcnow() - timedelta(hours=1),
            end_time=datetime.utcnow() + timedelta(hours=2),
            status=RadioStatus.LIVE,
            category_id=categories['Academic'].id,
            created_by=admin.id,
            banner_image='tech_talk.jpg'
        )
        
        live_radio2 = Radio(
            title='Campus Radio Live Session',
            description='Live music and entertainment broadcast',
            location='Radio Studio',
            start_time=datetime.utcnow() - timedelta(minutes=30),
            end_time=datetime.utcnow() + timedelta(hours=1),
            status=RadioStatus.LIVE,
            category_id=categories['Music'].id,
            created_by=admin.id,
            banner_image='radio_session.jpg'
        )
        
        
        # Create upcoming radios (COMMENTED OUT - No sample data)
        # upcoming_radios = [
        #     Radio(
        #         title='Annual Sports Day',
        #         description='Join us for a day of sports, fun, and competition',
        #         location='Sports Complex',
        #         start_time=datetime.utcnow() + timedelta(days=7),
        #         end_time=datetime.utcnow() + timedelta(days=7, hours=8),
        #         status=RadioStatus.UPCOMING,
        #         created_by=admin.id,
        #         banner_image='sports_day.jpg'
        #     ),
        #     Radio(
        #         title='Cultural Fest 2025',
        #         description='Celebrate diversity with music, dance, and food',
        #         location='Central Ground',
        #         start_time=datetime.utcnow() + timedelta(days=14),
        #         end_time=datetime.utcnow() + timedelta(days=14, hours=10),
        #         status=RadioStatus.UPCOMING,
        #         created_by=admin.id,
        #         banner_image='cultural_fest.jpg'
        #     ),
        #     Radio(
        #         title='Coding Hackathon',
        #         description='24-hour coding challenge with amazing prizes',
        #         location='Computer Lab',
        #         start_time=datetime.utcnow() + timedelta(days=21),
        #         end_time=datetime.utcnow() + timedelta(days=22),
        #         status=RadioStatus.UPCOMING,
        #         created_by=admin.id,
        #         banner_image='hackathon.jpg'
        #     )
        # ]
        
        db.session.add(live_radio1)
        db.session.add(live_radio2)
        # for radio in upcoming_radios:
        #     db.session.add(radio)
        
        db.session.commit()
        print(f"Created {2} radios (live only, no sample upcoming)")
        
        # Add participants to live radios
        live_radio1.participants.append(students[0])
        live_radio1.participants.append(students[1])
        live_radio1.participants.append(students[2])
        
        live_radio2.participants.append(students[1])
        live_radio2.participants.append(students[3])
        
        db.session.commit()
        
        print("Creating radio suggestions...")
        # Create suggestions
        suggestions = [
            RadioSuggestion(
                radio_title='Open Mic Night',
                description='A platform for students to showcase their talents',
                category='Talk Show',
                suggested_by=students[0].id,
                status=SuggestionStatus.PENDING
            ),
            RadioSuggestion(
                radio_title='Photography Workshop',
                description='Learn professional photography techniques',
                category='Academic',
                suggested_by=students[1].id,
                status=SuggestionStatus.PENDING
            ),
            RadioSuggestion(
                radio_title='Career Fair',
                description='Meet recruiters from top companies',
                category='Brand / Sponsored',
                suggested_by=students[2].id,
                status=SuggestionStatus.PENDING
            )
        ]
        
        for suggestion in suggestions:
            db.session.add(suggestion)
        
        db.session.commit()
        print(f"Created {len(suggestions)} suggestions")
        
        print("\n✅ Database seeded successfully!")
        print("\n📧 Admin credentials:")
        print("   Email: admin@campuswave.com")
        print("   Password: admin123")
        print("\n📧 Student credentials:")
        print("   Email: student1@campuswave.com (or student2, student3, etc.)")
        print("   Password: student123")

if __name__ == '__main__':
    seed_database()
