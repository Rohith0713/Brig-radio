# BRIG RADIO - Complete Setup Guide

## 📋 Overview
This guide will help you set up the complete BRIG RADIO project on a new system. The project consists of a Flask backend, Android mobile app, and Firebase integration for notifications and media storage.

---

## 🛠️ Prerequisites

### Required Software
1. **Python 3.10+** - Backend API
2. **MySQL 8.0+** - Database
3. **Android Studio (Latest)** - Mobile app development
4. **Git** - Version control
5. **Firebase Account** - Push notifications & storage

### System Requirements
- **OS:** Windows 10/11, macOS, or Linux
- **RAM:** Minimum 8GB (16GB recommended for Android Studio)
- **Storage:** 10GB free space

---

## 📂 Project Structure
```
Campus_Wave(1)/
├── CampusWave/backend/          # Flask Backend API
├── AndroidStudioProjects/
│   └── CampusWave/              # Android Mobile App
├── App_Summary.md               # Project overview
└── Setup_Guide.md              # This file
```

---

## 1️⃣ Backend Setup

### Step 1: Navigate to Backend Directory
```bash
cd CampusWave/backend
```

### Step 2: Create Virtual Environment
```bash
# Windows
python -m venv venv
venv\Scripts\activate

# macOS/Linux
python3 -m venv venv
source venv/bin/activate
```

### Step 3: Install Python Dependencies
```bash
pip install -r requirements.txt
```

### Step 4: Setup MySQL Database
```sql
-- Login to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE campuswave CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (optional)
CREATE USER 'campuswave_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON campuswave.* TO 'campuswave_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 5: Configure Environment Variables
Create `.env` file in `backend/` directory:

```ini
# Database
DATABASE_URL=mysql+pymysql://root:your_password@localhost/campuswave

# Security
JWT_SECRET_KEY=your_super_secret_jwt_key_here_change_in_production
SECRET_KEY=your_flask_secret_key_here

# Flask
FLASK_APP=app.py
FLASK_ENV=development

# File Upload
MAX_CONTENT_LENGTH=104857600  # 100MB in bytes
UPLOAD_FOLDER=uploads

# Firebase (Get from Firebase Console)
FIREBASE_CREDENTIALS_PATH=path/to/firebase-credentials.json
```

### Step 6: Initialize Database Tables
```bash
# Run table creation scripts
python create_tables.py
python create_issues_tables.py

# (Optional) Seed initial data
python seed.py
```

### Step 7: Start Backend Server
```bash
python app.py
```

✅ **Backend should be running at:** `http://localhost:5000`

---

## 2️⃣ Firebase Setup

### Step 1: Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add Project"
3. Enter project name: `CampusWave`
4. Follow wizard to complete setup

### Step 2: Enable Required Services
**In Firebase Console:**
1. **Authentication** → Enable Email/Password
2. **Cloud Messaging (FCM)** → Note your Server Key
3. **Realtime Database** or **Firestore** (if using)
4. **Storage** → Create default bucket

### Step 3: Download Service Account Key
1. Go to Project Settings → Service Accounts
2. Click "Generate New Private Key"
3. Save as `firebase-credentials.json` in `backend/` folder
4. Update path in `.env` file

### Step 4: Get FCM Server Key
1. Project Settings → Cloud Messaging
2. Copy "Server Key"
3. Store securely for mobile app configuration

---

## 3️⃣ Android App Setup

### Step 1: Open Project in Android Studio
1. Launch **Android Studio**
2. File → Open → Select `AndroidStudioProjects/CampusWave`
3. Wait for Gradle sync to complete (may take 5-10 minutes first time)

### Step 2: Add Firebase to Android App
1. Download `google-services.json` from Firebase Console:
   - Project Settings → Your Apps → Android App
   - Click "google-services.json" download button
   
2. Place file in: `app/google-services.json`

### Step 3: Add Splash Video
1. Create folder: `app/src/main/res/raw/`
2. Place your 3-second splash video: `app/src/main/res/raw/splash_video.mp4`

### Step 4: Configure API Endpoint
Edit `app/src/main/java/com/campuswave/app/data/network/ApiConfig.kt`:

```kotlin
object ApiConfig {
    // For Android Emulator (backend on same machine)
    const val BASE_URL = "http://10.0.2.2:5000/api/"
    
    // For Physical Device (replace with your computer's local IP)
    // const val BASE_URL = "http://192.168.1.100:5000/api/"
    
    // For Production
    // const val BASE_URL = "https://your-domain.com/api/"
}
```

**To find your local IP:**
- Windows: `ipconfig` → Look for IPv4 Address
- macOS/Linux: `ifconfig` or `ip addr` → Look for inet address

### Step 5: Build and Run
1. Connect Android device via USB **OR** start an AVD emulator
2. Click **Run** button (▶️) or press `Shift + F10`
3. Select your device/emulator
4. Wait for build and installation

✅ **App should launch with splash screen!**

---

## 4️⃣ Database Migrations (Optional)

If you need to update database schema:

```bash
cd backend

# Create migration
flask db migrate -m "Description of changes"

# Apply migration
flask db upgrade

# Rollback if needed
flask db downgrade
```

---

## 5️⃣ Testing the Complete System

### Backend Health Check
```bash
curl http://localhost:5000/api/health
```
Expected response: `{"status": "healthy"}`

### Test User Registration
1. Open mobile app
2. Click "Register"
3. Fill in details
4. Check email for OTP
5. Verify account

### Test Live Radio
1. Login as admin
2. Create a radio session
3. Start hosting
4. Login as student on another device
5. Tune in to live radio

---

## 6️⃣ Common Issues & Solutions

### Backend Issues

**Error:** `ModuleNotFoundError: No module named 'flask'`
- **Fix:** Ensure virtual environment is activated and run `pip install -r requirements.txt`

**Error:** `Can't connect to MySQL server`
- **Fix:** 
  - Check if MySQL is running: `sudo systemctl status mysql` (Linux) or check Windows Services
  - Verify database credentials in `.env`

**Error:** `Request Entity Too Large`
- **Fix:** Already configured in `config.py` with `MAX_CONTENT_LENGTH = 100MB`

### Android Issues

**Error:** `Cleartext HTTP traffic not permitted`
- **Fix:** Already configured in `AndroidManifest.xml` with `android:usesCleartextTraffic="true"`

**Error:** `Unable to load video`
- **Fix:** Ensure `splash_video.mp4` is in `app/src/main/res/raw/` folder

**Error:** `Failed to connect to /10.0.2.2:5000`
- **Fix:**
  - If using emulator: Use `10.0.2.2` (already configured)
  - If using physical device: Use your computer's local IP address
  - Ensure firewall allows connections on port 5000

### Firebase Issues

**Error:** Push notifications not working
- **Fix:**
  - Verify `google-services.json` is in `app/` folder
  - Check FCM server key in Firebase Console
  - Ensure app has notification permissions

---

## 7️⃣ Production Deployment

### Backend (Heroku/AWS/DigitalOcean)
1. Set environment variables (no `.env` file)
2. Use production database (not local MySQL)
3. Update `FLASK_ENV=production`
4. Configure WSGI server (Gunicorn/uWSGI)
5. Setup SSL certificate for HTTPS

### Mobile App (Google Play Store)
1. Update `BASE_URL` to production API
2. Change `android:usesCleartextTraffic` to `false`
3. Generate signed APK/AAB
4. Test thoroughly 
5. Create Play Store listing
6. Submit for review

---

## 8️⃣ Development Workflow

### Daily Development
```bash
# Terminal 1: Backend
cd backend
source venv/bin/activate  # or venv\Scripts\activate on Windows
python app.py

# Terminal 2: Android Studio
# Just click Run button
```

### Git Workflow
``` bash
git checkout -b feature/your-feature-name
# Make changes
git add .
git commit -m "Description of changes"
git push origin feature/your-feature-name
# Create Pull Request
```

---

## 📞 Support & Resources

- **Firebase Docs:** https://firebase.google.com/docs
- **Flask Docs:** https://flask.palletsprojects.com/
- **Android Docs:** https://developer.android.com/

---

## ✅ Verification Checklist

After setup, verify:
- [ ] Backend API running without errors
- [ ] MySQL database created and tables initialized
- [ ] Firebase project configured with FCM
- [ ] Android app builds successfully
- [ ] Splash video plays on app launch
- [ ] User registration/login works
- [ ] Push notifications delivered
- [ ] Live radio streaming functional
- [ ] File uploads working (images, audio, video)
- [ ] All admin features accessible

**Setup Complete! 🎉**
