# Feature Usage Matrix: Campus Wave Android App

This report details the usage status of backend modules within the Campus Wave Android application based on code analysis of both the backend and Android source code.

## Summary Matrix

| Module | Status | Evidence / Usage |
| :--- | :--- | :--- |
| **Podcasts** | **ACTIVE** | `PodcastViewModel.kt`, `PodcastListScreen.kt`. Calls `/api/podcasts` endpoints. |
| **Suggestions** | **ACTIVE** | `SuggestionsViewModel.kt`, `RadioSuggestionsScreen.kt`, `SubmitSuggestionScreen.kt`. Calls `/api/suggestions`. |
| **Reviews** | **UNUSED** | `reviews.py` exists in backend, but **no references** found in Android code (UI or API). |
| **Categories** | **ACTIVE** | Used in `CreateRadioScreen.kt`. Fetched via `getCategories()` in `BRIG_RADIOApiService.kt`. |
| **Favorites** | **ACTIVE** | `toggleFavorite()` in `RadioViewModel.kt`. API calls to `/api/favorites`. |
| **Comments** | **ACTIVE** | `CommentsSection.kt` used for Radio and College Updates. Calls `/api/radios/{id}/comments`. |
| **Notifications** | **ACTIVE** | `NotificationsScreen.kt`. Logic in `RadioViewModel.kt` fetching from `/api/notifications`. |
| **College Updates** | **ACTIVE** | `CollegeUpdatesScreen.kt`. Calls `/api/college-updates`. |
| **Reports** | **ACTIVE** | `ReportViewModel.kt`, `ReportIssueScreen.kt`. Calls `/api/reports/`. |
| **Marquee** | **ACTIVE** | `MarqueeComponent.kt` displayed in Dashboards. Fetched from `/api/marquee`. |
| **Placements** | **ACTIVE** | `PlacementsScreen.kt`, `UploadPlacementScreen.kt`. Calls `/api/placements`. |
| **Issues** | **ACTIVE** | `IssueViewModel.kt`, `StudentIssuesScreen.kt`, `AdminIssuesScreen.kt`. Calls `/api/issues`. |
| **Banners** | **ACTIVE** | `AdminBannerScreen.kt`. Logic in `RadioViewModel.kt` fetching from `/api/banners`. |
| **Analytics** | **ACTIVE** | `AdminAnalyticsScreen.kt`. Calls `/api/analytics` endpoints (Admin only). |
| **Dashboard (Admin)** | **ACTIVE** | `AdminDashboardScreen.kt`. Calls `/api/dashboard/stats`. |
| **Agora** | **UNUSED** | No SDK included in `build.gradle.kts`. Backend endpoints `/api/agora/token` are never called. |
| **HMS** | **UNUSED** | No SDK included in `build.gradle.kts`. Backend endpoints `/api/hms/token` are never called. |

---

## Detailed Analysis of UNUSED Modules

### 1. Reviews
*   **Backend Status**: Registered in `app/__init__.py` and implemented in `app/routes/reviews.py`. Defines `POST /api/reviews/<radio_id>` and `GET /api/reviews/<radio_id>`.
*   **Android Context**: 
    *   No `Review` data model found in `com.campuswave.app.data.models`.
    *   No Retrofit endpoint defined in `BRIG_RADIOApiService.kt` for reviews.
    *   No UI element or screen (e.g., star rating) exists in the student or admin UI.
    *   **Finding**: Confirmed as **UNUSED** in the Android app. No UI or API references found.

### 2. Agora (RTC)
*   **Backend Status**: Registered and implemented in `app/routes/agora.py`. Returns tokens.
*   **Android Context**: 
    *   **NO Agora SDK** found in `android/app/build.gradle.kts`.
    *   Commented-out code found in `LivePodcastViewerScreen.kt` and `LivePodcastControlScreen.kt`.
    *   The app uses a **custom WebRTC solution** (`WebRTCAudioManager.kt`) for live audio instead.
    *   **Finding**: Confirmed as **UNUSED** in the Android app. No endpoint calls identified.

### 3. HMS (100ms)
*   **Backend Status**: Registered and implemented in `app/routes/hms.py`.
*   **Android Context**:
    *   **NO HMS SDK** found in `android/app/build.gradle.kts`.
    *   Commented-out HMS role change logic found in `PodcastViewModel.kt`.
    *   **Finding**: Confirmed as **UNUSED** in the Android app. No active UI or API usage identified.

## Verification of ACTIVE Modules
The following modules were verified by locating both the corresponding UI screens and their associated API calls in `BRIG_RADIOApiService.kt` and respective ViewModels:

*   **Marquee/Banners**: Actively used for announcements in the dashboards.
*   **Issues/Reports**: Fully implemented help desk and session reporting systems.
*   **Podcasts**: New audio-only live sessions using WebRTC.
*   **Placements**: Career portal for students with poster upload functionality.
