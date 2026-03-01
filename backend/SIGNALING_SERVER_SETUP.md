# WebRTC Signaling Server Setup Guide

## Purpose
This signaling server handles real-time WebRTC signaling for the Campus Wave podcast/radio system, including **critical admin pause/resume events** that enforce audio control.

## Installation

### 1. Install Dependencies
```bash
cd backend
pip install -r signaling_requirements.txt
```

### 2. Start the Signaling Server
```bash
python signaling_server.py
```

The server will start on `ws://0.0.0.0:8765` and listen for WebSocket connections.

### 3. Configure Android App

The app is already configured to connect to:
- **Emulator**: `ws://10.0.2.2:8765` (localhost on your PC)
- **Real Device**: Change `SignalingClient.kt` line 19 to your PC's local IP:
  ```kotlin
  private val signalingUrl = "ws://192.168.1.X:8765" // Replace X with your IP
  ```

## How It Works

### Admin Pause Flow
1. Admin clicks "Pause Audio" → `toggleMute()` called
2. Backend updates `podcast.is_muted = true`
3. Admin's `WebRTCAudioManager.mute(true, isAdmin=true)` broadcasts:
   ```json
   {"type": "admin_pause", "room_id": "123"}
   ```
4. Signaling server receives and broadcasts to all students:
   ```json
   {"type": "admin_paused", "room_id": "123"}
   ```
5. Each student's `WebRTCAudioManager.handleSignalingEvent()`:
   - Disables all remote audio tracks: `receiver.track()?.setEnabled(false)`
   - Updates UI: Shows "Paused by Admin" overlay
6. **Result**: Students hear COMPLETE SILENCE ✅

### Admin Resume Flow
Same process but with `admin_resume` → `admin_resumed` → Enable tracks

## Testing

### Manual Test (Required)
1. Start signaling server: `python signaling_server.py`
2. Admin: Go live with a podcast
3. Students (2+): Join the live podcast
4. Admin: Speak → Verify students hear audio
5. Admin: Click "Pause Audio"
   - Students should see "Paused by Admin" overlay
   - Students should hear SILENCE
6. Admin: Speak (even though paused)
   - Students should hear NOTHING
7. Admin: Click "Resume Audio"
   - Students should hear audio again

### Verify Server Logs
You should see:
```
🔴 ADMIN PAUSE - Broadcasting to room 123
🟢 ADMIN RESUME - Broadcasting to room 123
```

### Verify Android Logs
Filter by `WebRTCAudioManager`:
```
🔴 Admin pausing podcast - broadcasting to students
🔴 ADMIN PAUSED - Disabling all remote audio tracks
Disabling track: ARDAMSa0
```

## Troubleshooting

### Students still hear audio after pause
- Check signaling server is running
- Check Android logs for "ADMIN PAUSED" message
- Verify student polling is calling `enforcePauseState()`

### Signaling server not connecting
- Check firewall allows port 8765
- For real device: Ensure PC and device on same WiFi
- Check `SignalingClient.kt` has correct IP address

### Audio quality issues
- This is unrelated to pause functionality
- Check WebRTC ICE connection state
- Verify STUN servers are reachable

## Running Both Servers

You need TWO servers running:

**Terminal 1 - Flask Backend**:
```bash
cd backend
python app.py
```

**Terminal 2 - Signaling Server**:
```bash
cd backend
python signaling_server.py
```

## Production Deployment

For production, use a process manager like `supervisor` or `systemd` to run both servers:

```ini
[program:flask_backend]
command=/usr/bin/python3 /path/to/backend/app.py
directory=/path/to/backend
autostart=true
autorestart=true

[program:signaling_server]
command=/usr/bin/python3 /path/to/backend/signaling_server.py
directory=/path/to/backend
autostart=true
autorestart=true
```

## Architecture Overview

```
Admin Device
    ↓ Pause clicked
Flask API (update DB: is_muted=true)
    ↓ Response
Admin WebRTCAudioManager.mute(true, isAdmin=true)
    ↓ sendAdminPause()
Signaling Server (broadcast "admin_paused")
    ↓ WebSocket
Student Devices
    ↓ handleSignalingEvent("admin_paused")
Disable remote tracks → SILENCE ✅
```

**Backup**: Student polling (every 3s) calls `enforcePauseState()` to sync with backend.
