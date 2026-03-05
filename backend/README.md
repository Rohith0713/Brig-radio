# Brig Radio — Backend API

Flask REST API powering the Brig Radio mobile application.

---

## Setup

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate   # Linux/Mac
venv\Scripts\activate      # Windows

# Install dependencies
pip install -r requirements.txt

# Configure environment
cp .env.example .env
# Edit .env with your credentials

# Run database migrations
flask db upgrade

# Start development server
python app.py
```

API available at `http://localhost:5000/api/`

---

## Project Structure

```
backend/
├── app/
│   ├── __init__.py           # App factory (create_app)
│   ├── extensions.py         # Flask extensions (db, jwt, cors, mail)
│   ├── models/               # SQLAlchemy models (22 files)
│   │   ├── user.py           # User, UserRole
│   │   ├── radio.py          # Radio, RadioStatus, HostStatus
│   │   ├── category.py       # Radio categories
│   │   ├── college_update.py # Campus news posts
│   │   ├── placement.py      # Placement announcements
│   │   ├── issue.py          # Student issues/feedback
│   │   └── ...               # +16 more models
│   ├── routes/               # API blueprints (16 files)
│   │   ├── auth.py           # Authentication & profiles
│   │   ├── radios.py         # Radio CRUD & live hosting
│   │   ├── suggestions.py    # Session proposals
│   │   ├── college_updates.py # Campus news
│   │   └── ...               # +12 more routes
│   ├── utils/                # Helpers
│   │   ├── upload.py         # File upload with subfolder support
│   │   ├── email.py          # Async email via Flask-Mail
│   │   ├── notifications.py  # Firebase push notifications
│   │   └── ...
│   ├── middleware/auth.py    # JWT decorators (admin_required, etc.)
│   └── errors/handlers.py   # Global error handlers
├── migrations/               # Alembic DB migrations
├── uploads/                  # User-uploaded files (gitignored)
├── config.py                 # Dev/Prod/Test configuration
├── app.py                    # Development entry point
├── wsgi.py                   # Production WSGI entry point
├── gunicorn.conf.py          # Gunicorn configuration
├── requirements.txt          # Python dependencies
├── .env.example              # Environment template
└── DEPLOYMENT.md             # AWS EC2 deployment guide
```

---

## Configuration

The app uses a config class hierarchy in `config.py`:

| Config | Use |
|--------|-----|
| `DevelopmentConfig` | Local dev with debug mode |
| `ProductionConfig` | AWS EC2 with Gunicorn |
| `TestingConfig` | SQLite in-memory for tests |

Set `FLASK_ENV=development` or `FLASK_ENV=production` in `.env`.

---

## API Reference

### Authentication (`/api/auth/`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/register` | — | Register new user |
| POST | `/login` | — | Login, returns JWT |
| GET | `/me` | JWT | Get current user |
| POST | `/verify-otp` | — | Verify email OTP |
| POST | `/resend-otp` | — | Resend OTP |
| PUT | `/profile` | JWT | Update profile |
| POST | `/profile/picture` | JWT | Upload profile picture |
| POST | `/forgot-password` | — | Request password reset |
| POST | `/reset-password` | — | Reset password with OTP |

### Radios (`/api/radios/`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | — | List all radios (filterable) |
| GET | `/live` | — | Get live radios |
| GET | `/upcoming` | — | Get upcoming radios |
| GET | `/missed` | — | Get ended radios |
| GET | `/<id>` | — | Get radio details |
| POST | `/` | Admin | Create radio |
| PUT | `/<id>` | Admin | Update radio |
| DELETE | `/<id>` | Admin | Delete radio |
| POST | `/<id>/banner` | Admin | Upload banner image |
| POST | `/<id>/media` | Admin | Upload audio/media |
| POST | `/<id>/subscribe` | JWT | Toggle subscription |
| POST | `/<id>/start` | Admin | Start live hosting |
| POST | `/<id>/pause` | Admin | Pause hosting |
| POST | `/<id>/resume` | Admin | Resume hosting |
| POST | `/<id>/end` | Admin | End hosting |

### Other Modules

| Module | Prefix | Key Operations |
|--------|--------|---------------|
| Suggestions | `/api/suggestions/` | CRUD session proposals |
| Categories | `/api/categories/` | List/create categories |
| College Updates | `/api/college-updates/` | CRUD campus posts |
| Placements | `/api/placements/` | Placement board |
| Banners | `/api/banners/` | Promotional banners |
| Marquee | `/api/marquee/` | Scrolling text |
| Favorites | `/api/favorites/` | Toggle favorites |
| Comments | `/api/comments/` | Radio comments |
| Issues | `/api/issues/` | Student feedback |
| Reports | `/api/reports/` | Content reporting |
| Dashboard | `/api/dashboard/` | Admin statistics |

---

## Production Deployment

```bash
# Run with Gunicorn
gunicorn -c gunicorn.conf.py wsgi:app
```

See [DEPLOYMENT.md](DEPLOYMENT.md) for the full AWS EC2 guide.

---

## Dependencies

| Package | Purpose |
|---------|---------|
| Flask 3.1 | Web framework |
| Flask-SQLAlchemy | ORM |
| Flask-Migrate | Database migrations |
| Flask-JWT-Extended | JWT authentication |
| Flask-CORS | Cross-origin requests |
| Flask-Mail | Email (OTP, notifications) |
| PyMySQL | MySQL driver |
| Gunicorn | Production WSGI server |
| firebase-admin | Push notifications |
| python-dotenv | Environment variables |
