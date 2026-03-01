# Agora SDK Usage Analysis (Evidence Report)

I have scanned the Android and Backend codebases to determine the status of the Agora SDK.

## 1. Is Agora SDK imported in the Android app?
**NO.**
- **Evidence**: `android/app/build.gradle.kts` (Lines 90-93) lists `google.webrtc` and `okhttp`, but does **not** include any Agora dependencies (`io.agora...`).
- **Evidence**: A global search for `io.agora` and `RtcEngine` in the Android `java` source directory ([`com/campuswave/app`](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app)) returned **zero results**.

## 2. Usage of Agora Classes/Methods
**NONE.**
- The live audio functionality is implemented using **WebRTC**, not Agora.
- **Reference**: [`PodcastViewModel.kt`](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/ui/viewmodels/PodcastViewModel.kt#L17-L21) uses `WebRTCAudioManager`.
- **Reference**: [`WebRTCAudioManager.kt`](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/data/audio/WebRTCAudioManager.kt#L13) uses `org.webrtc.*`.
- **Reference**: [`LivePodcastViewerScreen.kt`](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/ui/screens/student/LivePodcastViewerScreen.kt#L127) and [`LivePodcastControlScreen.kt`](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/ui/screens/admin/LivePodcastControlScreen.kt#L143) contain only **comments** about Agora.

## 3. Usage of Backend `/api/agora/token`
**UNUSED.**
- **Evidence**: [`BRIG_RADIOApiService.kt`](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/data/network/BRIG_RADIOApiService.kt#L578-L584) defines `getAgoraToken`, but it is **never called** by any ViewModel or Repository.

## 4. Decision: Can Agora be safely disabled?
**YES.**
Agora can be safely removed from both backend and frontend as the active codebase uses a custom WebRTC signaling server (running on port 8765) for all live audio features.
