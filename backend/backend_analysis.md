# Backend Analysis Report

I have completed a comprehensive scan of the `backend` folder. Below are the details for each of your 10 points.

## 1. Entry Point File
The entry point file used to start the Flask app is:
- **`app.py`** (located at the root level).
It uses the application factory pattern by calling `create_app()` from `app/__init__.py`.

## 2. All Registered Blueprints
The application registers 19 blueprints in `app/__init__.py`:
1. `auth`
2. `radios`
3. `suggestions`
4. `dashboard`
5. `reviews`
6. `categories`
7. `favorites`
8. `comments`
9. `analytics`
10. `notifications`
11. `college_updates`
12. `reports`
13. `marquee`
14. `podcasts`
15. `placements`
16. `agora`
17. `hms`
18. `issues`
19. `banners`

## 3. API Routes Map
Full list of routes extracted from `app/routes/`:

| Blueprint | Method | URL Path | Function Name |
| :--- | :--- | :--- | :--- |
| **Auth** | POST | `/api/auth/register` | `register` |
| | POST | `/api/auth/login` | `login` |
| | GET | `/api/auth/me` | `get_current_user` |
| | POST | `/api/auth/logout` | `logout` |
| | POST | `/api/auth/verify-otp` | `verify_otp` |
| | POST | `/api/auth/resend-otp` | `resend_otp` |
| | POST | `/api/auth/forgot-password` | `forgot_password` |
| | POST | `/api/auth/verify-reset-otp` | `verify_reset_otp` |
| | POST | `/api/auth/reset-password` | `reset_password` |
| | PATCH | `/api/auth/profile` | `update_profile` |
| | POST | `/api/auth/profile/picture` | `upload_profile_picture` |
| | GET | `/api/auth/admin-requests` | `get_admin_requests` |
| | POST | `/api/auth/approve-admin/<int:request_id>` | `approve_admin` |
| | POST | `/api/auth/request-password-reset` | `request_password_reset` |
| | POST | `/api/auth/verify-profile-reset-otp` | `verify_profile_reset_otp` |
| | POST | `/api/auth/complete-password-reset` | `complete_password_reset` |
| **Radios** | GET | `/api/radios` | `get_radios` |
| | GET | `/api/radios/live` | `get_live_radios` |
| | GET | `/api/radios/upcoming` | `get_upcoming_radios` |
| | GET | `/api/radios/missed` | `get_missed_radios` |
| | GET | `/api/radios/<int:radio_id>` | `get_radio` |
| | POST | `/api/radios` | `create_radio` |
| | PUT | `/api/radios/<int:radio_id>` | `update_radio` |
| | DELETE | `/api/radios/<int:radio_id>` | `delete_radio` |
| | POST | `/api/radios/<int:radio_id>/upload-banner` | `upload_banner` |
| | POST | `/api/radios/<int:radio_id>/upload-media` | `upload_media` |
| | POST | `/api/radios/<int:radio_id>/subscribe` | `toggle_subscription` |
| | POST | `/api/radios/<int:radio_id>/start-hosting` | `start_hosting` |
| | PUT | `/api/radios/<int:radio_id>/pause-hosting` | `pause_hosting` |
| | PUT | `/api/radios/<int:radio_id>/resume-hosting` | `resume_hosting` |
| | PUT | `/api/radios/<int:radio_id>/end-hosting` | `end_hosting` |
| | GET | `/api/radios/<int:radio_id>/stream-info` | `get_stream_info` |
| **Suggestions** | GET | `/api/suggestions` | `get_suggestions` |
| | GET | `/api/suggestions/pending` | `get_pending_suggestions` |
| | POST | `/api/suggestions` | `create_suggestion` |
| | PUT | `/api/suggestions/<int:id>/approve` | `approve_suggestion` |
| | PUT | `/api/suggestions/<int:id>/reject` | `reject_suggestion` |
| | GET | `/api/suggestions/my` | `get_my_suggestions` |
| | DELETE | `/api/suggestions/<int:id>` | `delete_suggestion` |
| **Dashboard** | GET | `/api/dashboard/stats` | `get_stats` |
| | GET | `/api/dashboard/analytics/radios` | `get_radio_analytics` |
| | GET | `/api/dashboard/analytics/participation` | `get_participation_analytics` |
| **Reviews** | POST | `/api/reviews/<int:radio_id>` | `create_review` |
| | GET | `/api/reviews/<int:radio_id>` | `get_radio_reviews` |
| **Categories** | GET | `/api/categories` | `get_categories` |
| | GET | `/api/categories/<int:id>` | `get_category` |
| | POST | `/api/categories` | `create_category` |
| | PUT | `/api/categories/<int:id>` | `update_category` |
| | DELETE | `/api/categories/<int:id>` | `delete_category` |
| | POST | `/api/categories/seed` | `seed_categories` |
| **Favorites** | GET | `/api/favorites` | `get_favorites` |
| | POST | `/api/radios/<id>/favorite` | `add_favorite` |
| | DELETE | `/api/radios/<id>/favorite` | `remove_favorite` |
| | POST | `/api/radios/<id>/favorite/toggle` | `toggle_favorite` |
| **Comments** | GET | `/api/radios/<id>/comments` | `get_comments` |
| | POST | `/api/radios/<id>/comments` | `add_comment` |
| | DELETE | `/api/comments/<id>` | `delete_comment` |
| | GET | `/api/radios/<id>/comments/recent` | `get_recent_comments` |
| **Analytics** | GET | `/api/admin/analytics` | `get_admin_analytics` |
| | GET | `/api/analytics/overview` | `get_overview` |
| | GET | `/api/analytics/radios` | `get_radio_analytics` |
| | GET | `/api/analytics/trends` | `get_trends` |
| **Notifications** | GET | `/api/notifications` | `get_notifications` |
| | PUT | `/api/notifications/<id>/read` | `mark_as_read` |
| | PUT | `/api/notifications/read-all` | `mark_all_as_read` |
| | DELETE | `/api/notifications/clear-all` | `clear_all_notifications` |
| **College Updates**| POST | `/api/college-updates` | `create_update` |
| | GET | `/api/college-updates` | `get_updates` |
| | DELETE | `/api/college-updates/<id>` | `delete_update` |
| | POST | `/api/college-updates/<id>/like` | `toggle_like` |
| | POST | `/api/college-updates/<id>/view` | `record_view` |
| | GET | `/api/college-updates/<id>/analytics`| `get_update_analytics`|
| **Reports** | POST | `/api/reports/` | `create_report` |
| | GET | `/api/reports/` | `get_reports` |
| **Marquee** | GET | `/api/marquee/active` | `get_active_marquee` |
| | GET | `/api/marquee` | `get_all_marquees` |
| | POST | `/api/marquee` | `create_or_update_marquee` |
| | PATCH | `/api/marquee/<id>/toggle` | `toggle_marquee` |
| | DELETE | `/api/marquee/<id>` | `delete_marquee` |
| **Podcasts** | POST | `/api/podcasts` | `create_podcast` |
| | GET | `/api/podcasts/active` | `get_active_podcasts` |
| | GET | `/api/podcasts/live` | `get_live_podcast` |
| | GET | `/api/podcasts/<id>` | `get_podcast` |
| | DELETE | `/api/podcasts/<id>` | `delete_podcast` |
| | POST | `/api/podcasts/<id>/go-live` | `go_live` |
| | POST | `/api/podcasts/<id>/end` | `end_podcast` |
| | POST | `/api/podcasts/<id>/toggle-mute` | `toggle_mute` |
| | POST | `/api/podcasts/<id>/join` | `join_podcast` |
| | POST | `/api/podcasts/<id>/leave` | `leave_podcast` |
| | GET | `/api/podcasts/<id>/viewers` | `get_viewer_count` |
| | POST | `/api/podcasts/<id>/hand-raise` | `raise_hand` |
| | DELETE | `/api/podcasts/<id>/hand-raise` | `cancel_hand_raise` |
| | GET | `/api/podcasts/<id>/hand-raise/status`| `get_hand_raise_status`|
| | GET | `/api/podcasts/<id>/hand-raises` | `get_hand_raises` |
| | POST | `/api/podcasts/<id>/hand-raises/<ui>/accept`| `accept_hand_raise`|
| | POST | `/api/podcasts/<id>/hand-raises/<ui>/ignore`|`ignore_hand_raise`|
| **Placements** | GET | `/api/placements` | `get_placements` |
| | GET | `/api/placements/saved` | `get_saved_placements` |
| | POST | `/api/placements/<id>/bookmark` | `toggle_bookmark` |
| | POST | `/api/placements` | `create_placement` |
| | DELETE | `/api/placements/<id>` | `delete_placement` |
| | POST | `/api/placements/<id>/apply` | `apply_placement` |
| | POST | `/api/placements/posters` | `upload_poster` |
| | GET | `/api/placements/posters` | `get_posters` |
| | PATCH | `/api/placements/posters/<id>` | `update_poster` |
| | DELETE | `/api/placements/posters/<id>` | `delete_poster` |
| **Agora** | GET | `/api/agora/token` | `get_token` |
| **HMS** | GET | `/api/hms/token" | `get_hms_token` |
| **Issues** | POST | `/api/issues` | `create_issue` |
| | GET | `/api/issues` | `get_all_issues` |
| | GET | `/api/issues/resolved` | `get_resolved_issues` |
| | GET | `/api/issues/my` | `get_my_issues` |
| | GET | `/api/issues/<id>` | `get_issue_details` |
| | POST | `/api/issues/<id>/message` | `send_message` |
| | PUT | `/api/issues/<id>/resolve` | `resolve_issue` |
| | GET | `/api/issues/stats` | `get_issue_stats` |
| | DELETE | `/api/issues/<id>` | `delete_issue` |
| **Banners** | POST | `/api/banners` | `upload_banner` |
| | GET | `/api/banners` | `get_banners` |
| | DELETE | `/api/banners/<int:id>` | `delete_banner` |

## 4. WebSocket or Socket.IO Events
The Flask application **does not** handle any WebSocket or Socket.IO events. Real-time communication is handled by the **standalone signaling server** (see point 5).

## 5. Signaling Server Reference
- **`signaling_server.py`** is located in the backend root.
- It is **actively used** as a standalone process (running on port 8765) for WebRTC signaling (admin pause/resume events).
- It is **not imported** into the Flask app but operates alongside it.

## 6. Background Scheduler or Cron Job
- **None**. There are no background schedulers (like APScheduler or cron) configured within the Flask application.

## 7. Celery or Async Worker
- **None**. No Celery, Redis, or other async worker configurations were found in the `backend` folder.

## 8. Third-Party Services
The following third-party services are integrated:
- **Firebase**: Used via `firebase-admin` (SDK JSON keys found).
- **Agora**: Used for real-time audio/video token generation (`agora_token.py`).
- **HMS (100ms)**: Used for live audio room token generation (`hms_token.py`).
- **Email**: Uses Flask-Mail via Gmail SMTP server for OTPs and notifications.

## 9. Unimported Files
The following files in the `backend` folder are not imported into the Flask application:
- **`signaling_server.py`**: Runs as a standalone process.
- **`seed.py`**: A one-time script used to populate the database with initial data.
- **`SIGNALING_SERVER_SETUP.md`**, **`FIREBASE_SETUP.md`**: Documentation files.

## 10. Required Environment Variables for Production
Based on the analysis of `config.py` and service utilities:

### Core App
- `FLASK_APP=app.py`
- `FLASK_ENV=production`
- `SECRET_KEY`: Long random string for session security.
- `JWT_SECRET_KEY`: Long random string for JWT security.

### Database
- `DB_USER`
- `DB_PASSWORD`
- `DB_HOST`
- `DB_NAME`

### Email (SMTP)
- `MAIL_SERVER` (Default: `smtp.gmail.com`)
- `MAIL_PORT` (Default: `587`)
- `MAIL_USERNAME`: Your email address.
- `MAIL_PASSWORD`: Your email app password.
- `MAIL_DEFAULT_SENDER`: Your email address.

### Third-Party APIs
- `AGORA_APP_ID`
- `AGORA_APP_CERTIFICATE`
- `HMS_ACCESS_KEY`
- `HMS_SECRET`

### Storage
- `UPLOAD_FOLDER`: Directory for file uploads (Default: `CWD/uploads`).
- `MAX_CONTENT_LENGTH`: Maximum file upload size (Default: 200MB).
