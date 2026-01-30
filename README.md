# Sea Cliff Smart Dining & WhatsApp Ordering System

A comprehensive hospitality management system with WhatsApp integration, POS functionality, and real-time order management for restaurants.

## Overview

This system provides:
- **WhatsApp Bot**: Guest ordering and notifications via WhatsApp Business API
- **Manager Dashboard**: Live order tracking, analytics, and staff management
- **Waiter POS**: Mobile and web-based point of sale system
- **Kitchen Display**: Real-time order queue for kitchen staff
- **Bar Display**: Drink order management for bartenders
- **Payment Integration**: Multiple payment methods including mobile money

## Technology Stack

### Backend & Web Frontend
- **Framework**: Laravel 11.x (PHP 8.2+)
- **Database**: MySQL 8.0+ or PostgreSQL 14+
- **Cache/Queue**: Redis 7.x
- **Real-time**: Laravel Echo + Pusher

### Mobile POS
- **Platform**: Native Android (Kotlin)
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room (SQLite)
- **DI**: Hilt

### Infrastructure
- **Containerization**: Docker + Docker Compose
- **Web Server**: Nginx
- **Process Manager**: Supervisor (for Laravel queues)

## Project Structure

```
HOSPITALITYSYSTEM/
├── laravel-app/          # Main Laravel application
├── android-pos/          # Native Android POS app
├── docs/                 # Documentation
├── infrastructure/       # Docker, K8s, deployment scripts
├── database/             # Database diagrams and documentation
├── scripts/              # Utility scripts
└── tests/                # E2E tests
```

See [docs/FOLDER_STRUCTURE.md](docs/FOLDER_STRUCTURE.md) for detailed structure.

## Quick Start

### Prerequisites

- PHP 8.2+ with extensions: BCMath, Ctype, JSON, Mbstring, OpenSSL, PDO, Tokenizer, XML
- Composer 2.x
- Node.js 18+ & NPM
- MySQL 8.0+ or PostgreSQL 14+
- Redis 7.x
- Android Studio (for mobile development)

### Installation

#### 1. Clone the repository

```bash
git clone <repository-url> HOSPITALITYSYSTEM
cd HOSPITALITYSYSTEM
```

#### 2. Setup Laravel Application

```bash
cd laravel-app

# Install PHP dependencies
composer install

# Copy environment file
cp .env.example .env

# Generate application key
php artisan key:generate

# Configure database in .env
# DB_CONNECTION=mysql
# DB_HOST=127.0.0.1
# DB_PORT=3306
# DB_DATABASE=seacliff_db
# DB_USERNAME=root
# DB_PASSWORD=

# Run migrations
php artisan migrate --seed

# Install NPM dependencies
npm install

# Build assets
npm run dev
```

#### 3. Setup Android POS

```bash
cd ../android-pos

# Open in Android Studio
# File -> Open -> select android-pos folder

# Configure API endpoint in local.properties
echo "api.base.url=http://10.0.2.2:8000/api/" >> local.properties

# Sync Gradle and build
```

#### 4. Start Development Servers

```bash
# Terminal 1: Laravel development server
cd laravel-app
php artisan serve

# Terminal 2: Queue worker
php artisan queue:work

# Terminal 3: Asset watcher (if using Vite)
npm run dev
```

### Docker Setup (Recommended)

```bash
# Build and start all services
docker-compose up -d

# Run migrations
docker-compose exec laravel php artisan migrate --seed

# View logs
docker-compose logs -f
```

Access the applications:
- **Manager Dashboard**: http://localhost:8000/manager
- **Waiter POS Web**: http://localhost:8000/waiter
- **Kitchen Display**: http://localhost:8000/kitchen
- **Bar Display**: http://localhost:8000/bar
- **API Documentation**: http://localhost:8000/api/docs

## Configuration

### WhatsApp Business API

1. Create a WhatsApp Business Account at https://business.facebook.com
2. Set up a phone number and get API credentials
3. Configure in `.env`:

```env
WHATSAPP_API_URL=https://graph.facebook.com/v18.0
WHATSAPP_ACCESS_TOKEN=your_access_token
WHATSAPP_PHONE_NUMBER_ID=your_phone_number_id
WHATSAPP_BUSINESS_ACCOUNT_ID=your_business_account_id
WHATSAPP_WEBHOOK_SECRET=your_webhook_secret
```

4. Register webhook URL: `https://yourdomain.com/webhooks/whatsapp`

### Payment Gateway

Configure your payment provider in `.env`:

```env
PAYMENT_GATEWAY=pesapal
PESAPAL_CONSUMER_KEY=your_key
PESAPAL_CONSUMER_SECRET=your_secret
PESAPAL_ENVIRONMENT=sandbox  # or 'live'
```

### Real-time Updates

Configure Pusher for real-time order updates:

```env
BROADCAST_DRIVER=pusher
PUSHER_APP_ID=your_app_id
PUSHER_APP_KEY=your_app_key
PUSHER_APP_SECRET=your_app_secret
PUSHER_APP_CLUSTER=mt1
```

## Development

### Running Tests

```bash
# Laravel tests
cd laravel-app
php artisan test

# Android tests
cd android-pos
./gradlew test
./gradlew connectedAndroidTest
```

### Code Style

```bash
# Laravel (Pint)
cd laravel-app
./vendor/bin/pint

# Android (ktlint)
cd android-pos
./gradlew ktlintFormat
```

### Database Migrations

```bash
# Create new migration
php artisan make:migration create_table_name

# Run migrations
php artisan migrate

# Rollback last migration
php artisan migrate:rollback

# Reset and re-run all migrations
php artisan migrate:fresh --seed
```

## Deployment

### Production Deployment

See [docs/deployment/production-deployment.md](docs/deployment/production-deployment.md) for detailed instructions.

#### Quick Production Setup

```bash
# 1. Clone repository on server
git clone <repo> /var/www/seacliff

# 2. Install dependencies
composer install --no-dev --optimize-autoloader

# 3. Configure environment
cp .env.example .env
# Edit .env with production values

# 4. Run migrations
php artisan migrate --force

# 5. Cache configuration
php artisan config:cache
php artisan route:cache
php artisan view:cache

# 6. Set permissions
chown -R www-data:www-data storage bootstrap/cache
chmod -R 775 storage bootstrap/cache

# 7. Setup supervisor for queues
# 8. Configure Nginx
# 9. Setup SSL with Let's Encrypt
```

### Android APK Release

```bash
cd android-pos
./gradlew assembleRelease

# APK located at: app/build/outputs/apk/release/app-release.apk
```

## API Documentation

API documentation is available at `/api/docs` when running the application.

Key endpoints:

- **Authentication**: `POST /api/auth/login`
- **Orders**: `GET|POST /api/orders`
- **Menu**: `GET /api/menu`
- **Tables**: `GET /api/tables`
- **Payments**: `POST /api/payments`

See [docs/api/](docs/api/) for complete API documentation.

## Features

### Guest Journey
- QR code scanning for table access
- WhatsApp-based ordering
- Real-time order status updates
- Multiple payment options
- Digital receipts and feedback

### Staff Operations
- **Waiters**: Mobile POS with offline support
- **Kitchen**: Order queue with priority management
- **Bar**: Drink preparation tracking
- **Manager**: Live dashboard with analytics

### Analytics & Reporting
- Real-time order tracking
- Sales reports (daily, weekly, monthly)
- Staff performance metrics
- Menu item popularity
- Average order value and wait times

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## License

This project is proprietary software. All rights reserved.

## Support

For support and questions:
- Email: support@seacliff.com
- Documentation: [docs/](docs/)
- Issue Tracker: GitHub Issues

## Team

- **Project Lead**: [Name]
- **Backend Developer**: [Name]
- **Android Developer**: [Name]
- **DevOps Engineer**: [Name]

## Acknowledgments

- Laravel Framework
- WhatsApp Business API
- Android Jetpack Components
- Open source community
