@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1
title CampusWave Native Setup
color 0A

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║            CampusWave - Reorganized Project Setup              ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

:: ========================================
:: Check Prerequisites
:: ========================================
echo [CHECKING PREREQUISITES]
echo ─────────────────────────────────────────

python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Python is NOT installed or not in PATH
    pause
    exit /b 1
)

echo [✓] Python found.
echo.

:: ========================================
:: Database Configuration
:: ========================================
echo [DATABASE CONFIGURATION]
echo ─────────────────────────────────────────
set /p DB_USER="   Database Username (default: root): "
if "!DB_USER!"=="" set DB_USER=root
set /p DB_PASSWORD="   Database Password: "
set /p DB_NAME="   Database Name (default: campuswave): "
if "!DB_NAME!"=="" set DB_NAME=campuswave
echo.

:: ========================================
:: Email Configuration
:: ========================================
echo [EMAIL/OTP CONFIGURATION]
echo ─────────────────────────────────────────
set /p MAIL_EMAIL="   Gmail Address for OTP: "
set /p MAIL_PASSWORD="   Gmail App Password (16 chars): "
echo.

:: ========================================
:: Network Configuration
:: ========================================
echo [NETWORK CONFIGURATION]
echo ─────────────────────────────────────────
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    for /f "tokens=1" %%b in ("%%a") do (
        set LOCAL_IP=%%b
        goto :found_ip
    )
)
:found_ip
echo    Detected IP: !LOCAL_IP!
set /p CONFIRM_IP="   Use this IP? (Y/n) or enter custom: "
if /i "!CONFIRM_IP!"=="n" (
    set /p LOCAL_IP="   Enter IP: "
) else if not "!CONFIRM_IP!"=="" if /i not "!CONFIRM_IP!"=="y" (
    set LOCAL_IP=!CONFIRM_IP!
)
echo.

:: ========================================
:: Create Configs
:: ========================================
echo [1/2] Creating Backend .env...
cd backend
(
echo FLASK_APP=app.py
echo FLASK_ENV=development
echo DB_HOST=localhost
echo DB_USER=!DB_USER!
echo DB_PASSWORD=!DB_PASSWORD!
echo DB_NAME=!DB_NAME!
echo JWT_SECRET_KEY=campuswave-secret-key-!RANDOM!
echo MAIL_USERNAME=!MAIL_EMAIL!
echo MAIL_PASSWORD=!MAIL_PASSWORD!
) > .env
echo       [✓] Created backend/.env

echo [2/2] Updating Android ApiConfig.kt...
cd ..
set API_CONFIG=android\app\src\main\java\com\campuswave\app\data\network\ApiConfig.kt
(
echo package com.campuswave.app.data.network
echo.
echo object ApiConfig {
echo     const val BASE_URL = "http://!LOCAL_IP!:5000/api/"
echo     const val UPLOADS_URL = "http://!LOCAL_IP!:5000/uploads/"
echo     const val AGORA_APP_ID = "5c987d3664cf418ea548a92bc73dff0b"
echo }
) > "!API_CONFIG!"
echo       [✓] Updated Android ApiConfig.kt

echo.
echo ═══════════════════════════════════════════════════════════════════
echo                    SETUP COMPLETE!
echo ═══════════════════════════════════════════════════════════════════
echo.
echo    1. Start MySQL and create database '!DB_NAME!'
echo    2. Start Backend: cd backend ^& python app.py
echo    3. Open 'android' folder in Android Studio
echo.
pause
