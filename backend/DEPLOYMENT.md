# Brig Radio — AWS EC2 Deployment Guide

Complete guide to deploy the Flask backend on AWS EC2 with Gunicorn + Nginx + RDS MySQL.

---

## 0. Pre-Deployment Cleanup (Already Done)

The following cleanup was performed to prepare for production:

| Action | Details |
|--------|---------|
| Removed `crash.txt` | 888KB debug crash dump — no production value |
| Removed `app/uploads/` | Stale duplicate of `backend/uploads/` |
| Removed podcasts stub | `podcasts.py` only returned "coming soon" |
| Scrubbed `.env` | Real credentials replaced with placeholders |
| Upgraded `upload.py` | Added subfolder support for organized file storage |

> [!NOTE]
> The `podcast.py` model file is kept to avoid database migration issues.

---

## 0.1 Mobile App Configuration

Before building the Android APK for production, update `ApiConfig.kt`:

```kotlin
// File: android/app/src/main/java/com/campuswave/app/data/network/ApiConfig.kt

object ApiConfig {
    // AWS EC2 Production Server
    const val BASE_URL = "http://<EC2-PUBLIC-IP>/api/"
    const val UPLOADS_URL = "http://<EC2-PUBLIC-IP>/uploads/"

    // Comment out local development URLs:
    // const val BASE_URL = "http://10.99.37.110:5000/api/"
    // const val UPLOADS_URL = "http://10.99.37.110:5000/uploads/"
}
```

> [!IMPORTANT]
> Use port 80 (no port in URL) for production since Nginx handles proxying to Gunicorn on port 8000.

---

## 1. EC2 Instance Setup

### Launch Instance
- **AMI**: Ubuntu 22.04 LTS
- **Instance Type**: `t2.micro` (free tier) or `t3.small` for production
- **Security Group** — open these ports:

| Port | Protocol | Source    | Purpose          |
|------|----------|-----------|------------------|
| 22   | TCP      | Your IP   | SSH              |
| 80   | TCP      | 0.0.0.0/0 | HTTP (Nginx)     |
| 443  | TCP      | 0.0.0.0/0 | HTTPS (optional) |

> [!IMPORTANT]
> Do NOT open port 8000 publicly. Nginx will proxy to Gunicorn internally.

### SSH into the Instance
```bash
ssh -i your-key.pem ubuntu@<EC2-PUBLIC-IP>
```

---

## 2. Server Preparation

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install required packages
sudo apt install -y python3 python3-pip python3-venv nginx mysql-client git

# Create project directory
sudo mkdir -p /home/ubuntu/brigradio
sudo chown ubuntu:ubuntu /home/ubuntu/brigradio
```

---

## 3. Deploy Application Code

```bash
cd /home/ubuntu/brigradio

# Clone your repository
git clone https://github.com/Rohith0713/Brig-radio.git .

# Or pull latest changes
# git pull origin main

cd backend
```

---

## 4. Python Environment Setup

```bash
# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install --upgrade pip
pip install -r requirements.txt
```

---

## 5. Environment Variables

```bash
# Copy the example and edit with your real values
cp .env.example .env
nano .env
```

Fill in these critical values:

```env
FLASK_ENV=production

# RDS endpoint from AWS console
DB_HOST=your-rds-endpoint.xxxx.ap-south-1.rds.amazonaws.com
DB_USER=admin
DB_PASSWORD=your_secure_password
DB_NAME=campuswave

# Generate these with: python -c "import secrets; print(secrets.token_hex(32))"
SECRET_KEY=<generated_key_1>
JWT_SECRET_KEY=<generated_key_2>

# Absolute path on the server
UPLOAD_FOLDER=/home/ubuntu/brigradio/backend/uploads

# Your Gmail app password
MAIL_USERNAME=brigradio@gmail.com
MAIL_PASSWORD=your_app_password
```

---

## 6. AWS RDS MySQL Setup

### Create RDS Instance (AWS Console)
1. Go to **RDS → Create database**
2. Choose **MySQL 8.0**
3. Template: **Free tier** (or Production)
4. DB instance identifier: `brigradio-db`
5. Master username: `admin`
6. Set a strong password
7. **Connectivity**: Same VPC as EC2, Security group allows port 3306 from EC2

### Connect and Create Database
```bash
mysql -h your-rds-endpoint.xxxx.ap-south-1.rds.amazonaws.com -u admin -p

# In MySQL shell:
CREATE DATABASE campuswave CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### Run Migrations
```bash
cd /home/ubuntu/brigradio/backend
source venv/bin/activate

# Initialize migrations (first time only, if migrations/ doesn't exist)
# flask db init

# Run migrations
flask db upgrade
```

> [!NOTE]
> If `flask db upgrade` fails because the migration files reference deleted models, 
> run `flask db stamp head` first, then make a new migration with `flask db migrate`.

---

## 7. Create Upload Directory

```bash
mkdir -p /home/ubuntu/brigradio/backend/uploads/banners
mkdir -p /home/ubuntu/brigradio/backend/uploads/placements
mkdir -p /home/ubuntu/brigradio/backend/uploads/profile_pics
mkdir -p /home/ubuntu/brigradio/backend/uploads/radios

# Set permissions
chmod -R 755 /home/ubuntu/brigradio/backend/uploads
```

---

## 8. Test Gunicorn

```bash
cd /home/ubuntu/brigradio/backend
source venv/bin/activate

# Quick test — should start without errors
gunicorn -c gunicorn.conf.py wsgi:app

# Test it works
curl http://localhost:8000/api/categories
# Should return JSON, not an error

# Stop with Ctrl+C
```

---

## 9. Gunicorn Systemd Service

Create a systemd service so Gunicorn starts on boot and auto-restarts:

```bash
sudo nano /etc/systemd/system/brigradio.service
```

Paste this:

```ini
[Unit]
Description=Brig Radio Gunicorn Application
After=network.target

[Service]
User=ubuntu
Group=ubuntu
WorkingDirectory=/home/ubuntu/brigradio/backend
Environment="PATH=/home/ubuntu/brigradio/backend/venv/bin"
ExecStart=/home/ubuntu/brigradio/backend/venv/bin/gunicorn -c gunicorn.conf.py wsgi:app
Restart=always
RestartSec=5

# Logging
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable brigradio
sudo systemctl start brigradio

# Check status
sudo systemctl status brigradio

# View logs
sudo journalctl -u brigradio -f
```

---

## 10. Nginx Configuration

```bash
sudo nano /etc/nginx/sites-available/brigradio
```

Paste this:

```nginx
server {
    listen 80;
    server_name your-domain.com;  # Or EC2 public IP

    # Max upload size (match Flask MAX_CONTENT_LENGTH)
    client_max_body_size 500M;

    # Proxy API requests to Gunicorn
    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Timeout settings for large uploads
        proxy_read_timeout 120s;
        proxy_connect_timeout 120s;
        proxy_send_timeout 120s;
    }

    # Serve uploaded files directly via Nginx (faster than Flask)
    location /uploads/ {
        alias /home/ubuntu/brigradio/backend/uploads/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

Enable and restart Nginx:

```bash
# Enable site
sudo ln -sf /etc/nginx/sites-available/brigradio /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Test config
sudo nginx -t

# Restart
sudo systemctl restart nginx
```

---

## 11. SSL with Certbot (Optional but Recommended)

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

---

## 12. Verify Deployment

```bash
# From your local machine or browser:
curl http://<EC2-PUBLIC-IP>/api/categories
curl http://<EC2-PUBLIC-IP>/api/radios
curl -X POST http://<EC2-PUBLIC-IP>/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test"}'
```

---

## Quick Reference — Common Commands

| Action | Command |
|--------|---------|
| Start app | `sudo systemctl start brigradio` |
| Stop app | `sudo systemctl stop brigradio` |
| Restart app | `sudo systemctl restart brigradio` |
| View logs | `sudo journalctl -u brigradio -f` |
| Restart Nginx | `sudo systemctl restart nginx` |
| Run migrations | `cd backend && source venv/bin/activate && flask db upgrade` |
| Update code | `git pull origin main && sudo systemctl restart brigradio` |

---

## Troubleshooting

### App won't start
```bash
# Check logs
sudo journalctl -u brigradio --no-pager -n 50

# Test manually
cd /home/ubuntu/brigradio/backend
source venv/bin/activate
python -c "from app import create_app; app = create_app('production'); print('OK')"
```

### Database connection fails
```bash
# Test connectivity
mysql -h $DB_HOST -u $DB_USER -p

# Check .env has correct RDS endpoint
cat .env | grep DB_
```

### 502 Bad Gateway from Nginx
```bash
# Gunicorn probably isn't running
sudo systemctl status brigradio
sudo systemctl restart brigradio
```

### Uploads not working
```bash
# Check permissions
ls -la /home/ubuntu/brigradio/backend/uploads/
chmod -R 755 /home/ubuntu/brigradio/backend/uploads/
```
