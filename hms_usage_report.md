# HMS (100ms) Usage Analysis (Evidence Report)

I have scanned the Android and Backend codebases to determine the status of the HMS (100ms) integration.

## 1. Is HMS SDK imported in the Android app?
**NO.**
- **Evidence**: `android/app/build.gradle.kts` (Lines 90-93) does **not** include any 100ms dependencies (`live.hms...`).
- **Evidence**: A global search for `live.hms` and `HMSSDK` in the Android `java` source directory ([`com/campuswave/app`](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app)) returned **zero results**.

## 2. Usage of HMS Classes/Methods
**NONE.**
- The project uses a custom **WebRTC** implementation for live audio.
- **Reference**: [`PodcastViewModel.kt`](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/ui/viewmodels/PodcastViewModel.kt#L354-L360) contains **commented-out** logic referencing HMS role changes.
- **Reference**: [`ApiConfig.kt`](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/data/network/ApiConfig.kt#L17) contains a placeholder `HMS_DEFAULT_ROOM_ID`.

## 3. Backend Endpoints for HMS
**EXIST BUT UNUSED.**
- **Route**: `app/routes/hms.py` defines the `/api/hms/token` endpoint. ([`hms.py`](file:///c:/Campus_Wave(1)/backend/app/routes/hms.py))
- **Utility**: `app/utils/hms_token.py` contains the `generate_hms_token` function. ([`hms_token.py`](file:///c:/Campus_Wave(1)/backend/app/utils/hms_token.py))
- **Status**: The Android app does **not** make any requests to the HMS endpoint.

## 4. Decision: Can HMS be safely disabled?
**YES.**
HMS (100ms) is a legacy or alternative implementation that is currently **inactive** and can be safely removed from both backend and frontend.
