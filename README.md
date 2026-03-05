# Brig Radio 📻

A full-stack campus radio platform that lets college communities create, host, and listen to live radio sessions. Built with a **Flask** backend and **Android (Jetpack Compose)** mobile app.

---

## Features

- **Live Radio Sessions** — Admins create and host live radio broadcasts
- **Session Proposals** — Students suggest radio topics for admin review
- **College Updates** — Post campus news with images and media
- **Placements Board** — Share placement/internship announcements
- **Banners & Marquee** — Scrolling announcements and promotional banners
- **User Profiles** — Registration, OTP verification, profile pictures
- **Notifications** — Firebase Cloud Messaging push notifications
- **Admin Dashboard** — Full admin panel for managing content
- **Issues & Reports** — Student feedback and content reporting system

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Mobile App** | Android · Kotlin · Jetpack Compose |
| **Backend API** | Python · Flask · SQLAlchemy |
| **Database** | MySQL (AWS RDS in production) |
| **Auth** | JWT (flask-jwt-extended) |
| **Email** | Flask-Mail (Gmail SMTP) |
| **Push Notifications** | Firebase Cloud Messaging |
| **Production Server** | Gunicorn + Nginx on AWS EC2 |

---

## Project Structure

```
Brig-radio/
├── android/                  # Android mobile app (Jetpack Compose)
│   └── app/src/main/java/com/campuswave/app/
│       ├── data/             # Models, network, repositories
│       ├── navigation/       # App navigation graph
│       ├── services/         # Background services
│       ├── ui/               # Screens, components, viewmodels, theme
│       └── utils/            # Utilities (date, URL, notifications)
│
├── backend/                  # Flask REST API
│   ├── app/
│   │   ├── models/           # SQLAlchemy database models
│   │   ├── routes/           # API route blueprints
│   │   ├── utils/            # Email, upload, analytics helpers
│   │   ├── middleware/       # Auth decorators
│   │   └── errors/           # Error handlers
│   ├── migrations/           # Alembic database migrations
│   ├── config.py             # App configuration (dev/prod/test)
│   ├── wsgi.py               # Production WSGI entry point
│   ├── gunicorn.conf.py      # Gunicorn worker configuration
│   ├── requirements.txt      # Python dependencies
│   ├── .env.example          # Environment variables template
│   └── DEPLOYMENT.md         # AWS deployment guide
│
└── docs/                     # Project documentation
```

---

## Quick Start (Local Development)

### Backend

```bash
cd backend

# Create virtual environment
python -m venv venv
source venv/bin/activate   # Linux/Mac
venv\Scripts\activate      # Windows

# Install dependencies
pip install -r requirements.txt

# Configure environment
cp .env.example .env
# Edit .env with your database credentials and mail settings

# Run migrations
flask db upgrade

# Start development server
python app.py
```

The API runs at `http://localhost:5000/api/`

### Android App

1. Open the `android/` directory in **Android Studio**
2. Update `ApiConfig.kt` with your backend URL
3. Build and run on device/emulator

---

## API Endpoints

| Module | Prefix | Description |
|--------|--------|-------------|
| Auth | `/api/auth/` | Register, login, OTP, profile |
| Radios | `/api/radios/` | CRUD, hosting, subscriptions |
| Suggestions | `/api/suggestions/` | Session proposals |
| Categories | `/api/categories/` | Radio categories |
| College Updates | `/api/college-updates/` | Campus news posts |
| Placements | `/api/placements/` | Job/internship board |
| Banners | `/api/banners/` | Promotional banners |
| Marquee | `/api/marquee/` | Scrolling announcements |
| Favorites | `/api/favorites/` | User favorites |
| Comments | `/api/comments/` | Radio comments |
| Issues | `/api/issues/` | Student feedback |
| Reports | `/api/reports/` | Content reports |
| Analytics | `/api/analytics/` | Usage analytics |
| Notifications | `/api/notifications/` | Push notifications |
| Dashboard | `/api/dashboard/` | Admin statistics |

---

## Deployment

See [backend/DEPLOYMENT.md](backend/DEPLOYMENT.md) for the complete AWS EC2 deployment guide covering:

- EC2 instance setup
- Gunicorn + Nginx configuration
- RDS MySQL database setup
- Environment variables
- SSL/HTTPS (optional)

---

## Environment Variables

Copy `backend/.env.example` to `backend/.env` and configure:

| Variable | Purpose |
|----------|---------|
| `DB_HOST` | MySQL database host |
| `DB_USER` | Database username |
| `DB_PASSWORD` | Database password |
| `DB_NAME` | Database name |
| `SECRET_KEY` | Flask secret key |
| `JWT_SECRET_KEY` | JWT signing key |
| `UPLOAD_FOLDER` | File upload directory |
| `MAIL_USERNAME` | Gmail address for OTP |
| `MAIL_PASSWORD` | Gmail app password |

---

## License

This project is proprietary. All rights reserved.

---

**Developed by Rohith Kumar** · [GitHub](https://github.com/Rohith0713)
