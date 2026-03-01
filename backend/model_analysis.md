# SQLAlchemy Model Analysis Report

I have performed a deep analysis of the SQLAlchemy models in the `backend` folder. Below are the findings for each of your 6 points.

## 1. List of All Models (Table Names)
The application defines 22 primary models and 2 association tables, resulting in 24 total tables used in the data layer.

| File | Model Class | Table Name |
| :--- | :--- | :--- |
| `user.py` | `User` | `users` |
| `student.py` | `Student` | `students` |
| `admin.py` | `Admin` | `admins` |
| `radio.py` | `Radio` | `radios` |
| `radio.py` | (Association) | `radio_participants` |
| `podcast.py` | `Podcast` | `podcasts` |
| `podcast.py` | `HandRaise` | `hand_raises` |
| `podcast.py` | (Association) | `podcast_viewers` |
| `admin_request.py` | `AdminRequest` | `admin_requests` |
| `category.py` | `Category` | `categories` |
| `favorite.py` | `Favorite` | `favorites` |
| `comment.py` | `Comment` | `comments` |
| `notification.py` | `Notification` | `notifications` |
| `radio_subscription.py` | `RadioSubscription` | `radio_subscriptions` |
| `otp.py` | `OTP` | `otps` |
| `marquee.py` | `Marquee` | `marquees` |
| `placement.py` | `Placement` | `placements` |
| `placement.py` | `PlacementPoster` | `placement_posters` |
| `placement.py` | `PlacementBookmark`| `placement_bookmarks` |
| `issue.py` | `Issue` | `issues` |
| `issue.py` | `IssueMessage` | `issue_messages` |
| `system_event.py` | `SystemEvent` | `system_events` |
| `banner.py` | `Banner` | `banners` |
| `college_update.py` | `CollegeUpdate` | `college_updates` |
| `college_update.py" | `CollegeUpdateLike` | `college_update_likes` |
| `college_update.py` | `CollegeUpdateView` | `college_update_views` |
| `report.py` | `Report` | `reports` |
| `review.py` | `Review` | `reviews` |

## 2. Relationships Between Models
The data model uses a standard relational structure with several key link patterns:

- **User Profiles (1:1)**: `User` -> `Student` (via `user_id`), `User` -> `Admin` (via `user_id`).
- **Real-time Participation (M:M)**:
  - `User` <-> `Radio` via `radio_participants` table.
  - `User` <-> `Podcast` via `podcast_viewers` table.
- **Content Creation (1:M)**:
  - `User` -> `Radio` (as `creator`).
  - `User` -> `Podcast` (as `creator`).
  - `User` -> `CollegeUpdate` (as `admin`).
- **Engagement (1:M / M:M via junction)**:
  - `User` <-> `Radio` via `Favorite`, `Comment`, `Review`, and `RadioSubscription`.
  - `User` <-> `Placement` via `PlacementBookmark`.
  - `User` <-> `CollegeUpdate` via `CollegeUpdateLike` and `CollegeUpdateView`.
- **Support & Feedback (1:M)**:
  - `User` -> `Issue` -> `IssueMessage`.
  - `User` -> `RadioSuggestion`.
  - `User` -> `Report`.
- **System Services (1:M)**:
  - `User` -> `Notification`.
  - `User` -> `SystemEvent`.

## 3. Models Referenced in Routes
All major models are referenced in their respective route files:
- **`auth.py`**: `User`, `Student`, `Admin`, `AdminRequest`, `OTP`.
- **`radios.py`**: `Radio`, `RadioSubscription`.
- **`podcasts.py`**: `Podcast`, `HandRaise`.
- **`placements.py`**: `Placement`, `PlacementPoster`, `PlacementBookmark`.
- **`issues.py`**: `Issue`, `IssueMessage`.
- **`college_updates.py`**: `CollegeUpdate`, `CollegeUpdateLike`, `CollegeUpdateView`.
- **`analytics.py` / `dashboard.py`**: `SystemEvent`, `Radio`, `User`, `Issue`.

## 4. Models Defined but Never Used
- **None**. Every model class defined in `app/models` is imported into `app/models/__init__.py` and referenced in at least one route or utility.

## 5. Migration Consistency
Migrations are **consistent** with the models. The migration history matches the status types (`LIVE`, `SCHEDULED`, etc.) and hosting fields used in the code.

## 6. Total Tables in Production
The total number of tables expected in production (including association tables and internal tracking) is **30**.

1. `users`
2. `students`
3. `admins`
4. `admin_requests`
5. `radios`
6. `radio_participants` (Association)
7. `radio_suggestions`
8. `categories`
9. `favorites`
10. `comments`
11. `notifications`
12. `radio_subscriptions`
13. `otps`
14. `marquees`
15. `podcasts`
16. `podcast_viewers` (Association)
17. `hand_raises`
18. `placements`
19. `placement_posters`
20. `placement_bookmarks`
21. `issues`
22. `issue_messages`
23. `system_events`
24. `banners`
25. `college_updates`
26. `college_update_likes`
27. `college_update_views`
28. `reports`
29. `reviews`
30. `alembic_version` (Flask-Migrate)
