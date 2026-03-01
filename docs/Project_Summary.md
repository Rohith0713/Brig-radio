# CampusWave (Brig Radio) — Project Summary

> **Version:** 2.0 | **Date:** February 2026 | **Status:** Production Ready

## What It Is
A **campus radio & content management platform** — Flask backend + native Android app (Kotlin/Jetpack Compose).

---

## Core Features

| Feature | Description |
|---------|-------------|
| 🎙️ **Live Radio** | Admins create, schedule, and host live audio sessions; students tune in |
| 📰 **College Updates** | Instagram-style posts with likes/views |
| 📢 **Marquee** | Customizable scrolling announcements (color, gradient, speed, font) |
| 🖼️ **Banners** | Auto-sliding image carousel on home screen |
| 💡 **Suggestions** | Students propose radio topics → Admins approve/reject → Auto-creates radio |
| 🐛 **Issues** | Students report campus issues with threaded admin-student chat |
| 🎧 **Podcasts** | Live podcast sessions with hand-raise interaction |
| 🎓 **Placements** | Job/internship listings with bookmarks and posters |
| 📊 **Analytics** | Admin dashboard with engagement metrics and trends |
| 🔔 **Notifications** | In-app + Firebase push notifications |

---

## User Roles

| Role | Purpose |
|------|---------|
| **MAIN_ADMIN** | Super admin — approves other admin registrations |
| **ADMIN** | Creates content, hosts radio, manages all modules |
| **STUDENT** | Listens, browses, submits suggestions, reports issues |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Flask 3.0 + SQLAlchemy + MySQL 8.0 |
| **Auth** | JWT + Email OTP verification |
| **Android** | Kotlin + Jetpack Compose + Material Design 3 |
| **Audio** | ExoPlayer Media3 + Foreground Service |
| **Push** | Firebase Cloud Messaging |
| **Signaling** | WebSocket server (Python asyncio) |
| **HTTP Client** | Retrofit + OkHttp |
| **Image Loading** | Coil |

---

## Project Scale

- **22** database models
- **20** API route blueprints
- **49** Android screens (7 auth + 16 admin + 13 student + 6 common + others)

---

## Live Radio Flow

```
Create Radio → Upload Media → Start Hosting → Students Tune In → Pause/Resume → End Session
```

Statuses: `DRAFT` → `UPCOMING` → `LIVE` → `COMPLETED` (auto-synced by time)

---

## Current Status

| Category | Status |
|----------|--------|
| Auth + Roles | ✅ Complete |
| Radio CRUD + Live Hosting | ✅ Complete |
| Audio Playback (Foreground Service) | ✅ Complete |
| College Updates, Marquee, Banners | ✅ Complete |
| Suggestions, Issues, Placements | ✅ Complete |
| Analytics + Notifications | ✅ Complete |
| Podcast WebRTC Audio | 🔄 In Progress |
| Community/Societies Module | 🔄 Planned |
| Session Recording & Replay | 🔄 Partial |
| Offline Mode | ❌ Not Started |

---

## Key Pending Improvements

1. Replace HTTP polling with WebSockets for real-time updates
2. Migrate file storage to cloud (S3/GCS) with CDN
3. Add offline caching (Room DB on Android)
4. Complete podcast WebRTC audio pipeline
5. CI/CD pipeline + automated testing
6. API versioning and Swagger documentation
