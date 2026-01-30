# Sea Cliff Hospitality System - Quick Start Guide

Complete guide to get the entire system running locally.

## Prerequisites

- **Mac** with macOS (your current setup)
- **PHP** 8.2+ with PostgreSQL extension
- **Composer** (PHP package manager)
- **Node.js** 18+ and npm
- **PostgreSQL** 14+ (installed and running)
- **Android Studio** Hedgehog or newer
- **JDK** 17

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Sea Cliff Hospitality System                │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   Laravel    │  │   Livewire   │  │   Android    │       │
│  │   Backend    │  │   Web Staff  │  │     POS      │       │
│  │   + API      │  │   Portals    │  │   (Mobile)   │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                 │                  │               │
│         └─────────────────┴──────────────────┘               │
│                           │                                  │
│                    ┌──────▼──────┐                           │
│                    │  PostgreSQL │                           │
│                    │   Database  │                           │
│                    └─────────────┘                           │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │         WhatsApp Business API (Future)              │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## Step 1: Database Setup

### 1.1 Start PostgreSQL

```bash
# Check if PostgreSQL is running
psql --version

# If not running, start it (macOS with Homebrew)
brew services start postgresql@14

# Or if using Postgres.app
# Just open the Postgres.app from Applications
```

### 1.2 Create Database

```bash
# Connect to PostgreSQL
psql postgres

# Create database and user
CREATE DATABASE seacliff_dining;
CREATE USER seacliff_user WITH PASSWORD 'seacliff_pass';
GRANT ALL PRIVILEGES ON DATABASE seacliff_dining TO seacliff_user;

# Exit psql
\q
```

---

## Step 2: Laravel Backend Setup

### 2.1 Navigate to Laravel Directory

```bash
cd /Volumes/DATA/PROJECTS/HOSPITALITYSYSTEM/laravel-app
```

### 2.2 Install Dependencies

```bash
# Install PHP dependencies
composer install

# Install Node dependencies
npm install
```

### 2.3 Configure Environment

```bash
# Copy environment file
cp .env.example .env

# Generate application key
php artisan key:generate
```

### 2.4 Update .env File

Edit `laravel-app/.env`:

```env
APP_NAME="Sea Cliff Dining"
APP_ENV=local
APP_DEBUG=true
APP_URL=http://localhost:8000

# Database Configuration
DB_CONNECTION=pgsql
DB_HOST=127.0.0.1
DB_PORT=5432
DB_DATABASE=seacliff_dining
DB_USERNAME=seacliff_user
DB_PASSWORD=seacliff_pass

# Queue Configuration
QUEUE_CONNECTION=database

# Cache Configuration
CACHE_STORE=database

# Session Configuration
SESSION_DRIVER=database

# WhatsApp (configure later when approved)
WHATSAPP_API_VERSION=v18.0
WHATSAPP_ACCESS_TOKEN=
WHATSAPP_PHONE_NUMBER_ID=
WHATSAPP_BUSINESS_ACCOUNT_ID=
WHATSAPP_WEBHOOK_VERIFY_TOKEN=seacliff_webhook_token_2024
```

### 2.5 Run Migrations and Seeders

```bash
# Run database migrations
php artisan migrate:fresh

# Seed database with test data
php artisan db:seed

# This creates:
# - 30 menu items
# - 8 staff members (admin, manager, waiters, chefs, bartender)
# - 23 tables
# - 20 sample guests
```

### 2.6 Start Laravel Server

```bash
# Start on all network interfaces (allows Android device connection)
php artisan serve --host=0.0.0.0 --port=8000
```

**Keep this terminal open!**

You should see:
```
INFO  Server running on [http://0.0.0.0:8000].
```

### 2.7 Test Backend (New Terminal)

```bash
# Test API endpoints
curl http://localhost:8000/api/menu

# Test authentication
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"waiter@seacliff.com","password":"password","device_name":"test"}'
```

---

## Step 3: Web Portal Setup (Optional)

### 3.1 Build Frontend Assets

```bash
cd /Volumes/DATA/PROJECTS/HOSPITALITYSYSTEM/laravel-app

# Build Vite assets
npm run build

# Or run in watch mode for development
npm run dev
```

### 3.2 Access Web Portals

Open browser and navigate to:

- **Manager Dashboard**: http://localhost:8000/manager/dashboard
- **Kitchen Display**: http://localhost:8000/kitchen/display
- **Bar Display**: http://localhost:8000/bar/display

**Login Credentials**:
- Manager: `manager@seacliff.com` / `password`
- Chef: `chef@seacliff.com` / `password`
- Bartender: `bartender@seacliff.com` / `password`

---

## Step 4: Android POS Setup

### 4.1 Open Android Project

```bash
# Open Android Studio
# File → Open → select android-pos folder
```

### 4.2 Configure API Connection

Create `android-pos/local.properties`:

```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
api.base.url="http://10.0.2.2:8000/api/"
api.timeout=30
sync.interval=300
```

**Note**: Use your actual Android SDK path!

### 4.3 Sync Gradle

In Android Studio:
```
File → Sync Project with Gradle Files
```

Wait for Gradle to download dependencies (~2-3 minutes first time).

### 4.4 Create Emulator (if needed)

```
Tools → Device Manager → Create Device
- Device: Pixel 5
- System Image: Android 13 (API 33) or higher
- Name: SeaCliff_Test
```

### 4.5 Run Android App

1. Select emulator from device dropdown
2. Click Run (▶️) button
3. Wait for app to install and launch

### 4.6 Login to POS

Use these credentials:

- **Waiter**: `waiter@seacliff.com` / `password`
- **Manager**: `manager@seacliff.com` / `password`
- **Admin**: `admin@seacliff.com` / `password`

---

## Complete Test Flow

### Test 1: Create Order via POS

1. **Login** to Android POS as waiter
2. **Select Table** from table grid
3. **Browse Menu** and add items to cart
4. **Place Order**
5. **Verify** order appears in database

### Test 2: Kitchen Display

1. Open **Kitchen Display** in browser (http://localhost:8000/kitchen/display)
2. Login as chef
3. See order from POS appear in "Pending" column
4. Click **"Start Preparing"**
5. Click **"Mark as Ready"**

### Test 3: View Orders in POS

1. In Android POS, navigate to **Orders**
2. See order with updated status
3. Filter by status (Pending, Preparing, Ready, etc.)

---

## Default Credentials

All passwords are: `password`

### Staff Accounts

| Role | Email | Access |
|------|-------|--------|
| Admin | admin@seacliff.com | Full access |
| Manager | manager@seacliff.com | Dashboard, reports, all portals |
| Waiter 1 | waiter@seacliff.com | POS, tables, orders |
| Waiter 2 | waiter2@seacliff.com | POS, tables, orders |
| Waiter 3 | waiter3@seacliff.com | POS, tables, orders |
| Chef 1 | chef@seacliff.com | Kitchen display |
| Chef 2 | chef2@seacliff.com | Kitchen display |
| Bartender | bartender@seacliff.com | Bar display |

---

## Troubleshooting

### Issue: "Connection refused" in Android

**Solution**:
```bash
# Ensure Laravel is running on 0.0.0.0
php artisan serve --host=0.0.0.0 --port=8000

# Test from emulator
adb shell
curl http://10.0.2.2:8000/api/menu
```

### Issue: "SQLSTATE[08006] connection failed"

**Solution**:
```bash
# Check PostgreSQL is running
brew services list

# Restart PostgreSQL
brew services restart postgresql@14

# Verify connection
psql -U seacliff_user -d seacliff_dining
```

### Issue: Gradle sync failed

**Solution**:
```bash
# In Android Studio:
File → Invalidate Caches → Invalidate and Restart

# Or from terminal:
cd android-pos
./gradlew clean
./gradlew build
```

### Issue: White screen in Android app

**Solution**:
```bash
# Clear app data
adb shell pm clear com.seacliff.pos

# Reinstall
cd android-pos
./gradlew installDebug
```

---

## System Verification Checklist

- [ ] PostgreSQL running and database created
- [ ] Laravel dependencies installed (`composer install`)
- [ ] Laravel migrations run (`php artisan migrate:fresh --seed`)
- [ ] Laravel server running on port 8000
- [ ] Can access http://localhost:8000/api/menu
- [ ] Android Studio opened with android-pos project
- [ ] Gradle sync completed successfully
- [ ] Android emulator created and running
- [ ] Can login to Android POS app
- [ ] Can create orders from Android POS
- [ ] Orders appear in database

---

## Architecture Components

### Backend (Laravel)
- **API**: RESTful API with Sanctum authentication
- **Database**: PostgreSQL with 9 tables
- **Services**: Order management, payment processing, menu service
- **Events**: Real-time order status updates
- **Jobs**: Background sync, notifications

### Web Portals (Livewire)
- **Manager Dashboard**: Analytics, order overview
- **Kitchen Display**: Order queue for chefs
- **Bar Display**: Drink order queue

### Mobile (Android)
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room (SQLite) for offline support
- **Network**: Retrofit with OkHttp
- **DI**: Hilt dependency injection
- **Sync**: WorkManager background sync

---

## Development Workflow

### Daily Development

1. **Start Database**:
   ```bash
   brew services start postgresql@14
   ```

2. **Start Laravel**:
   ```bash
   cd laravel-app
   php artisan serve --host=0.0.0.0
   ```

3. **Start Vite** (if working on web UI):
   ```bash
   npm run dev
   ```

4. **Run Android App**:
   - Open Android Studio
   - Click Run

### Making Changes

**Backend Changes**:
```bash
cd laravel-app

# After database changes
php artisan migrate:fresh --seed

# Clear cache
php artisan cache:clear
php artisan config:clear
```

**Android Changes**:
```bash
cd android-pos

# Rebuild project
./gradlew clean assembleDebug

# Or in Android Studio: Build → Rebuild Project
```

---

## Next Steps

1. ✅ Complete WhatsApp Business API setup (see `docs/WHATSAPP_TEMPLATES.md`)
2. ✅ Integrate payment gateways (Pesapal, M-Pesa)
3. ✅ Add reporting and analytics
4. ✅ Implement loyalty program
5. ✅ Deploy to production server

---

## Project Documentation

- **Implementation Strategy**: `docs/IMPLEMENTATION_STRATEGY.md`
- **WhatsApp Setup**: `docs/WHATSAPP_TEMPLATES.md`
- **Android Configuration**: `android-pos/API_CONFIGURATION.md`
- **Android README**: `android-pos/README.md`

---

## Support

If you encounter issues:

1. Check logs:
   ```bash
   # Laravel logs
   tail -f laravel-app/storage/logs/laravel.log

   # Android logs
   adb logcat | grep "SeaCliff"
   ```

2. GitHub Issues: https://github.com/Andrew-Mashamba/HOSPITALITYSYSTEM/issues

---

**System is ready! Start Laravel backend and launch Android POS to begin testing.** 🚀
