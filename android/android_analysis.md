# Android Project Analysis Report

I have completed a comprehensive scan of the `android` project. Below are the details for each of your 9 points.

## 1. Base API URL
The base URL is configured in `ApiConfig.kt`:
- **Physical Device**: `http://10.99.37.110:5000/api/`
- **Emulator**: `http://10.0.2.2:5000/api/`
- **Uploads URL**: Same host with `/uploads/` path.

## 2. All Retrofit Endpoints
Found in `BRIG_RADIOApiService.kt`. The API surface is divided into modules:
- **Auth**: `/auth/login`, `/auth/register`, `/auth/verify-otp`, `/auth/me`, `/auth/profile`, etc.
- **Radios**: `/radios`, `/radios/live`, `/radios/upcoming`, `/radios/{id}/start-hosting`, etc.
- **Podcasts**: `/podcasts`, `/podcasts/active`, `/podcasts/{id}/join`, `/podcasts/{id}/hand-raise`.
- **Placements**: `/placements`, `/placements/saved`, `/placements/posters`.
- **Issues**: `/issues`, `/issues/{id}/message`, `/issues/{id}/resolve`.
- **Others**: `/categories`, `/favorites`, `/notifications`, `/marquee`, `/banners`, `/college-updates`.

## 3. All WebSocket Connections
The app uses WebSockets for real-time signaling and control:
- **General Signaling (SignalingClient)**: `ws://10.0.2.2:8765` (used for WebRTC/Podcasts).
- **Radio Control (RadioSignalingClient)**: `ws://[host]:8765` (used for admin pause/resume of radio sessions).

## 4. Any WebRTC Server URLs
- **STUN Server**: `stun:stun.l.google.com:19302` (used in `WebRTCAudioManager.kt`).
- **Agora**: The app is also configured for Agora with `AGORA_APP_ID = "5c987d3664cf418ea548a92bc73dff0b"`.

## 5. All Background Workers
- **`RadioReminderWorker`**: Registered to show notifications for radio reminders (30m, 5m, 2m before, and Live).
- **Scheduler**: Managed by `NotificationScheduler.kt` utilizing `WorkManager.enqueueUniqueWork`.

## 6. All ViewModels Used in Navigation
The following ViewModels are passed to `AppNavigation`:
- `AuthViewModel`
- `AdminViewModel`
- `RadioViewModel`
- `SuggestionsViewModel`
- `PlacementViewModel`
- `PodcastViewModel`

Additionally, `IssueViewModel`, `ProfileViewModel`, and `ReportViewModel` are instantiated locally in their specific screens.

## 7. Any Screen or ViewModel Not Referenced
### Unreferenced Screens (Defined in `Screen.kt` but not in `AppNavigation`):
- `Screen.Search`
- `Screen.Favorites`
- `Screen.Calendar`
- `Screen.ReportIssue` (The UI uses `StudentIssues` flow instead)
### ViewModels:
- All ViewModels are internally referenced in at least one screen.

## 8. All Services and Triggers
- **`AudioPlaybackService`**: Handles background audio for radio.
  - **Triggered by**: `AudioServiceManager.startPlayback()` when a user starts a radio session.
- **`BRIG_RADIOMessagingService`** (Firebase): Handles push notifications.
  - **Triggered by**: FCM (Firebase Cloud Messaging) events.
- **`AudioServiceManager`**: A singleton utility that manages binding/unbinding to the audio service.

## 9. All Firebase Usage
- **FCM**: Implemented in `BRIG_RADIOMessagingService.kt` for navigation-aware notifications.
- **Topic Subscription**: App automatically subscribes to the `"students"` topic in `MainActivity.kt`.
- **Deep Linking**: FCM data payload (type, id, auto_start) is used to trigger navigation in `MainActivity.kt`.
