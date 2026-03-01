# WebRTC Signaling Audit: Campus Wave / Brig Radio

This document analyzes the WebRTC signaling implementation and dependencies between the Android application and the backend signaling server.

---

## 1. Protocol Usage (`ws://` vs `wss://`)
The Android application exclusively uses the unencrypted **`ws://`** protocol for signaling. No references to `wss://` were found in the codebase.

*   **Evidence**: 
    *   [SignalingClient.kt:L19](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/data/network/SignalingClient.kt#L19): `private val signalingUrl = "ws://10.0.2.2:8765"`
    *   [RadioSignalingClient.kt:L186-L191](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/data/network/RadioSignalingClient.kt#L186-191): `return "ws://$host:8765"`

---

## 2. Port Reference (`8765`)
The port **`8765`** is the hardcoded signaling port used across all WebRTC-enabled components in the Android app.

*   **Evidence**:
    *   [SignalingClient.kt:L19](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/data/network/SignalingClient.kt#L19)
    *   [RadioSignalingClient.kt:L186](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/data/network/RadioSignalingClient.kt#L186)
    *   [signaling_server.py:L234](file:///c:/Campus_Wave(1)/backend/signaling_server.py#L234) (Backend confirms listening on 8765).

---

## 3. Client Instantiation
Signaling clients are instantiated in key ViewModels and lifecycle-aware components:

*   **SignalingClient**: Instantiated in `MainActivity.kt` ([L126](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/MainActivity.kt#L126)) and passed to `WebRTCAudioManager`.
*   **RadioSignalingClient**: Instantiated in `RadioViewModel.kt` and `RadioDetailsScreen.kt` to handle administrative broadcast events (Pause/Resume).

---

## 4. Fallback Mechanisms
There is **no fallback mechanism** for WebRTC `offer`/`answer` exchange (SDP handshake) or ICE candidate negotiation.

*   **Analysis**: If `signaling_server.py` is offline, the P2P connection cannot be established even if both peers are on the same network.
*   **Polling Backup**: `PodcastViewModel.kt` implements a polling mechanism ([L447-L450](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/ui/viewmodels/PodcastViewModel.kt#L447-L450)) that calls the REST API to **enforce pause states** as a safety measure. This only affects track enablement and does not help with the initial connection.

---

## 5. Functional Dependency
**Live hosting and joining CANNOT function without `signaling_server.py`.**

*   **Conclusion**: The signaling server is a critical runtime dependency for the live audio feature.
*   **Evidence**: [WebRTCAudioManager.kt:L61-L64](file:///c:/Campus_Wave(1)/android/app/src/main/java/com/campuswave/app/data/audio/WebRTCAudioManager.kt#L61-L64) shows that the audio manager's core logic is reactive to `signalingClient.signalingEvents`. Without these events, `createPeerConnection` is never triggered for remote peers.
