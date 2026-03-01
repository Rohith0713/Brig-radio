# Production Audit Report: Campus Wave / Brig Radio

This audit provides an evidence-based analysis of the Campus Wave project (Android + Backend) to determine the baseline configuration required for production.

---

## SECTION 1 — ANDROID ↔ BACKEND API USAGE

### 1. Active API Endpoints (Called by Android)
The following endpoint categories are actively used via `BRIG_RADIOApiService.kt`:
*   **Auth**: `/auth/login`, `/auth/register`, `/auth/verify-otp`, `/auth/resend-otp`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/me`, `/auth/profile`, `/auth/profile/picture`.
*   **Radios**: `GET /radios`, `GET /radios/live`, `GET /radios/upcoming`, `GET /radios/{id}`, `POST /radios`, `PUT /radios/{id}`, `DELETE /radios/{id}`, `POST /radios/{id}/upload-media`, `POST /radios/{id}/upload-banner`.
*   **Categories**: `GET /categories`, `GET /categories/{id}`, `POST /categories`.
*   **Favorites**: `GET /favorites`, `POST /radios/{id}/favorite/toggle`, `POST /radios/{id}/subscribe`.
*   **Comments**: `GET /radios/{id}/comments`, `GET /radios/{id}/comments/recent`, `POST /radios/{id}/comments`.
*   **Suggestions**: `GET /suggestions`, `POST /suggestions`, `PUT /suggestions/{id}/approve`, `PUT /suggestions/{id}/reject`, `GET /suggestions/my`, `DELETE /suggestions/{id}`.
*   **College Updates**: `GET /college-updates`, `POST /college-updates`, `DELETE /college-updates/{id}`, `POST /college-updates/{id}/like`, `POST /college-updates/{id}/view`, `GET /college-updates/{id}/comments`, `POST /college-updates/{id}/comment`.
*   **Notifications**: `GET /notifications`, `PUT /notifications/{id}/read`, `PUT /notifications/read-all`, `DELETE /notifications/clear-all`.
*   **Marquee**: `GET /marquee/active`, `GET /marquee`, `POST /marquee`, `PATCH /marquee/{id}/toggle`.
*   **Podcasts**: `POST /podcasts`, `GET /podcasts/active`, `GET /podcasts/live`, `GET /podcasts/{id}`, `POST /podcasts/{id}/go-live`, `POST /podcasts/{id}/join`, `POST /podcasts/{id}/leave`, `POST /podcasts/{id}/hand-raise`.
*   **Placements**: `GET /placements`, `POST /placements`, `DELETE /placements/{id}`, `GET /placements/posters`, `POST /placements/posters`.
*   **Issues**: `POST /issues`, `GET /issues/my`, `GET /issues/{id}`, `POST /issues/{id}/message`.
*   **Banners**: `GET /banners`, `POST /banners`.
*   **Reports**: `POST /reports/`.

### 2. Unused Endpoints (Exist but Never Called)
*   **Reviews Module**: Entire `reviews` blueprint (`POST /api/reviews/<radio_id>`, `GET /api/reviews/<radio_id>`). No Retrofit declaration exists.
*   **Agora Token**: `GET /agora/token`. Declared in `BRIG_RADIOApiService.kt` at L578, but **never invoked** in any ViewModel or screen (verified via grep).
*   **HMS Token**: `hms.py` blueprint exists in backend, but **no Retrofit declaration** exists in Android.

---

## SECTION 2 — THIRD-PARTY SERVICES VERIFICATION

| Service | Status | Evidence |
| :--- | :--- | :--- |
| **Firebase Admin** | **ACTIVE** | `campuswave-3e97f-firebase-adminsdk...json` in backend; `BRIG_RADIOMessagingService.kt` in Android. |
| **Gmail SMTP** | **ACTIVE** | `config.py` (L41) uses `smtp.gmail.com`; `email.py` utility used for OTP and Approvals. |
| **WebRTC Signaling** | **ACTIVE** | `signaling_server.py` handles room logic on **port 8765**; `RadioSignalingClient.kt` connects to it. |
| **Agora** | **UNUSED** | **NO SDK** in `build.gradle.kts`; `RadioSignalingClient.kt` uses custom WebRTC instead. |
| **HMS (100ms)** | **UNUSED** | **NO SDK** in `build.gradle.kts`; Implementation logic in `PodcastViewModel.kt` is commented out. |

---

## SECTION 3 — FEATURE MODULE USAGE MATRIX

| Module | Classification | Android UI Reference | API Usage | Production Safe to Disable? |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | **ACTIVE** | `LoginScreen.kt`, etc. | `/api/auth/*` | No |
| **Radios** | **ACTIVE** | `RadioDetailsScreen.kt` | `/api/radios/*` | No |
| **Podcasts** | **ACTIVE** | `PodcastListScreen.kt` | `/api/podcasts/*` | No |
| **Suggestions** | **ACTIVE** | `SubmitSuggestionScreen.kt`| `/api/suggestions/*` | No |
| **Reviews** | **UNUSED** | **None** | **None** | **YES** |
| **Categories** | **ACTIVE** | `CreateRadioScreen.kt` | `/api/categories/*` | No |
| **Favorites** | **ACTIVE** | `RadioViewModel.kt` | `/api/favorites/*` | No |
| **Comments** | **ACTIVE** | `CommentsSection.kt` | `/api/comments/*` | No |
| **Notifications** | **ACTIVE** | `NotificationsScreen.kt` | `/api/notifications/*` | No |
| **College Updates**| **ACTIVE** | `CollegeUpdatesScreen.kt` | `/api/college-updates/*`| No |
| **Reports** | **ACTIVE** | `ReportIssueScreen.kt` | `/api/reports/*` | No |
| **Marquee** | **ACTIVE** | `MarqueeComponent.kt` | `/api/marquee/*` | No |
| **Placements** | **ACTIVE** | `PlacementsScreen.kt` | `/api/placements/*` | No |
| **Issues** | **ACTIVE** | `StudentIssuesScreen.kt` | `/api/issues/*` | No |
| **Banners** | **ACTIVE** | `AdminBannerScreen.kt` | `/api/banners/*` | No |
| **Analytics** | **ADMIN-ONLY** | `AdminAnalyticsScreen.kt` | `/api/admin/analytics` | No |
| **Dashboard** | **ADMIN-ONLY** | `AdminDashboardScreen.kt` | `/api/dashboard/stats` | No |
| **Agora** | **UNUSED** | **None** | **None** | **YES** |
| **HMS** | **UNUSED** | **None** | **None** | **YES** |

---

## SECTION 4 — ANDROID SCREEN & VIEWMODEL AUDIT

### 1. Activities & Services
*   **Activities**: `MainActivity` (Host for all Composables).
*   **Services**: `BRIG_RADIOMessagingService` (FCM), `AudioPlaybackService` (Background audio).

### 2. Composable Screen Usage
*   **Active Screens**: `Login`, `Register`, `StudentDashboard`, `AdminDashboard`, `RadioDetails`, `PodcastList`, `Placements`, `CollegeUpdates`, `StudentIssues`, `AdminIssues`, `ManageMarquee`.
*   **Unused / Placeholder Screens**:
    *   `PodcastComingSoonScreen.kt`: Defined in `AppNavigation.kt` (L534) but only used as a fallback. Classify as **Future feature placeholder**.
    *   `NewPasswordScreen.kt`: Used only in `profile_reset` flow. **ACTIVE**.

### 3. ViewModel Reference Audit
All 9 ViewModels are actively instantiated and used in `AppNavigation.kt`:
*   `AuthViewModel`, `AdminViewModel`, `RadioViewModel`, `SuggestionsViewModel`, `PodcastViewModel`, `PlacementViewModel`, `IssueViewModel`, `ProfileViewModel`, `ReportViewModel`.

---

## SECTION 5 — FINAL MINIMAL RUNTIME CONFIGURATION

### 1. Services required on AWS EC2
*   **Gunicorn / Flask**: Running the main API (`app:create_app()`).
*   **WebRTC Signaling Server**: Running `signaling_server.py` via `asyncio`.
*   **MySQL Database**: (Based on `config.py` L13).
*   **Firebase Admin SDK**: Initialized at runtime via JSON credentials.

### 2. Required Environment Variables
*   `SECRET_KEY`, `JWT_SECRET_KEY`
*   `DB_USER`, `DB_PASSWORD`, `DB_HOST`, `DB_NAME`
*   `MAIL_USERNAME`, `MAIL_PASSWORD` (Gmail SMTP)
*   `FIREBASE_CONFIG_PATH`

### 3. Required Open Ports
*   **80 / 443**: Standard API traffic.
*   **8765**: WebRTC Signaling (WebSockets).
*   **3306**: MySQL (internal or restricted access).

### 4. Optional / Disablable Components
*   **Agora/HMS**: All routes in `app/routes/agora.py` and `app/routes/hms.py` can be removed.
*   **Reviews**: Entire `reviews.py` route and `Review` model can be disabled.
