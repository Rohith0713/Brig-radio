# Firebase Backend Setup Instructions

## You Need the Backend Service Account Key

To enable push notifications from your backend, you need to add `serviceAccountKey.json` to your backend folder.

### Where to Get It:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **campuswave-3e97f**
3. Click the gear icon (⚙️) → **Project settings**
4. Go to the **Service accounts** tab
5. Click **Generate new private key**
6. Save the downloaded JSON file as `serviceAccountKey.json`
7. Place it in: `c:\Users\Rohith Kumar\Campus_Wave(1)\CampusWave\backend\serviceAccountKey.json`

### ⚠️ Important:
- **Do NOT commit this file to Git** (already in .gitignore)
- Keep it secure - it has admin access to your Firebase project

---

## What Happens Without It:

If you don't add the backend key:
- ✅ App still works normally
- ✅ Students can view and like updates
- ✅ Admins can create posts
- ❌ Push notifications will be **mocked** (logged to console instead of sent)

Once you add it, notifications will be delivered in real-time! 🔔
