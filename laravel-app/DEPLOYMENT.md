# Production Deployment Guide

This guide covers the complete setup and deployment process for the SeaCliff Dining Laravel application in a production environment.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Server Requirements](#server-requirements)
3. [Initial Server Setup](#initial-server-setup)
4. [Application Deployment](#application-deployment)
5. [Environment Configuration](#environment-configuration)
6. [Queue Workers Setup](#queue-workers-setup)
7. [Task Scheduling Setup](#task-scheduling-setup)
8. [Database Backup](#database-backup)
9. [WebSocket Server (Laravel Reverb)](#websocket-server-laravel-reverb)
10. [SSL/HTTPS Configuration](#sslhttps-configuration)
11. [File Permissions](#file-permissions)
12. [Security Hardening](#security-hardening)
13. [Monitoring & Logging](#monitoring--logging)
14. [Post-Deployment Checklist](#post-deployment-checklist)
15. [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Domain name configured and pointing to your server
- SSH access to your production server
- Root or sudo access
- Basic knowledge of Linux command line

## Server Requirements

### Minimum Specifications
- **OS**: Ubuntu 22.04 LTS or later (recommended)
- **CPU**: 2 cores minimum
- **RAM**: 4GB minimum (8GB recommended)
- **Storage**: 40GB SSD minimum
- **Network**: Stable internet connection

### Required Software
- **PHP**: 8.2 or higher
- **Web Server**: Nginx or Apache
- **Database**: PostgreSQL 14+ (recommended) or MySQL 8.0+
- **Cache/Queue**: Redis 6.0+
- **Process Manager**: Supervisor
- **Node.js**: 18.x or later (for asset compilation)
- **Composer**: 2.x

---

## Initial Server Setup

### 1. Update System Packages

```bash
sudo apt update && sudo apt upgrade -y
```

### 2. Install Required Packages

```bash
# Install PHP and extensions
sudo apt install -y php8.2-fpm php8.2-cli php8.2-pgsql php8.2-mysql \
    php8.2-redis php8.2-mbstring php8.2-xml php8.2-bcmath \
    php8.2-curl php8.2-zip php8.2-gd php8.2-intl php8.2-soap

# Install PostgreSQL
sudo apt install -y postgresql postgresql-contrib

# Install Redis
sudo apt install -y redis-server

# Install Nginx
sudo apt install -y nginx

# Install Supervisor
sudo apt install -y supervisor

# Install Node.js and npm
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Install Composer
curl -sS https://getcomposer.org/installer | php
sudo mv composer.phar /usr/local/bin/composer
```

### 3. Configure PostgreSQL

```bash
# Switch to postgres user
sudo -u postgres psql

# Create database and user
CREATE DATABASE seacliff_dining_production;
CREATE USER seacliff_user WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE seacliff_dining_production TO seacliff_user;
\q
```

### 4. Configure Redis

```bash
# Edit Redis configuration
sudo nano /etc/redis/redis.conf

# Set password (uncomment and modify)
requirepass your_redis_password

# Restart Redis
sudo systemctl restart redis-server
sudo systemctl enable redis-server
```

---

## Application Deployment

### 1. Create Application Directory

```bash
sudo mkdir -p /var/www/html/laravel-app
sudo chown -R $USER:www-data /var/www/html/laravel-app
```

### 2. Clone or Upload Application

**Option A: Using Git**
```bash
cd /var/www/html
git clone https://github.com/yourusername/laravel-app.git
cd laravel-app
```

**Option B: Upload via SFTP**
- Use an SFTP client to upload files to `/var/www/html/laravel-app`

### 3. Install Dependencies

```bash
cd /var/www/html/laravel-app

# Install PHP dependencies
composer install --optimize-autoloader --no-dev

# Install Node dependencies and build assets
npm install
npm run build
```

---

## Environment Configuration

### 1. Create Production Environment File

```bash
cd /var/www/html/laravel-app
cp .env.production .env
```

### 2. Generate Application Key

```bash
php artisan key:generate
```

### 3. Configure Environment Variables

Edit `.env` and update the following:

```bash
nano .env
```

**Critical Settings to Update:**

```env
APP_NAME="SeaCliff Dining"
APP_ENV=production
APP_DEBUG=false
APP_URL=https://yourdomain.com

# Database
DB_CONNECTION=pgsql
DB_HOST=127.0.0.1
DB_PORT=5432
DB_DATABASE=seacliff_dining_production
DB_USERNAME=seacliff_user
DB_PASSWORD=your_secure_password

# Redis
REDIS_PASSWORD=your_redis_password

# Session & Sanctum
SESSION_DOMAIN=.yourdomain.com
SANCTUM_STATEFUL_DOMAINS=yourdomain.com,www.yourdomain.com

# Mail Configuration
MAIL_MAILER=smtp
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=your_mailgun_username
MAIL_PASSWORD=your_mailgun_password
MAIL_FROM_ADDRESS=noreply@yourdomain.com
MAIL_ERROR_TO=admin@yourdomain.com

# Reverb WebSocket
REVERB_APP_ID=your_app_id
REVERB_APP_KEY=your_app_key
REVERB_APP_SECRET=your_app_secret
REVERB_HOST=yourdomain.com
REVERB_SCHEME=wss

# API Keys (add your production keys)
WHATSAPP_API_TOKEN=your_production_token
STRIPE_PUBLIC_KEY=your_production_key
STRIPE_SECRET_KEY=your_production_secret

# Logging
LOG_SLACK_WEBHOOK_URL=your_slack_webhook_url
```

### 4. Run Migrations

```bash
php artisan migrate --force
```

### 5. Seed Database (if needed)

```bash
php artisan db:seed --force
```

### 6. Create Storage Link

```bash
php artisan storage:link
```

### 7. Cache Configuration

```bash
php artisan config:cache
php artisan route:cache
php artisan view:cache
```

---

## Queue Workers Setup

### 1. Copy Supervisor Configuration

```bash
sudo cp supervisor-queue-worker.conf /etc/supervisor/conf.d/laravel-queue-worker.conf
```

### 2. Update Paths in Supervisor Config

```bash
sudo nano /etc/supervisor/conf.d/laravel-worker.conf
```

Ensure the path matches your installation directory.

### 3. Start Queue Workers

```bash
sudo supervisorctl reread
sudo supervisorctl update
sudo supervisorctl start laravel-worker:*
```

### 4. Verify Queue Workers

```bash
sudo supervisorctl status
```

You should see 4 worker processes running.

### 5. Managing Queue Workers

```bash
# Restart workers
sudo supervisorctl restart laravel-worker:*

# Stop workers
sudo supervisorctl stop laravel-worker:*

# View logs
tail -f /var/www/html/laravel-app/storage/logs/worker.log
```

---

## Task Scheduling Setup

### 1. Edit Crontab

```bash
sudo crontab -e
```

### 2. Add Laravel Scheduler

Add this line to the crontab:

```cron
* * * * * cd /var/www/html/laravel-app && php artisan schedule:run >> /dev/null 2>&1
```

### 3. Verify Scheduler is Working

```bash
# Check logs
tail -f /var/www/html/laravel-app/storage/logs/laravel.log

# Test scheduler manually
php artisan schedule:run
```

### 4. Scheduled Tasks Overview

The following tasks are automatically scheduled (defined in `routes/console.php`):

- **Daily at 01:00**: Clean old backups
- **Daily at 02:00**: Run database backup
- **Daily at 03:00**: Clear expired sessions
- **Daily at 04:00**: Optimize application (cache configs)
- **Daily at 08:00**: Send daily sales summary email
- **Weekly on Sunday at 01:00**: Clear old log files
- **Every 5 minutes**: Update order statuses, monitor queue size
- **Hourly (9 AM - 10 PM)**: Send reminder notifications

---

## Database Backup

### 1. Install Laravel Backup Package (Optional but Recommended)

```bash
composer require spatie/laravel-backup
```

### 2. Publish Configuration

```bash
php artisan vendor:publish --provider="Spatie\Backup\BackupServiceProvider"
```

### 3. Configure Backup

Edit `config/backup.php`:

```php
'destination' => [
    'disks' => [
        'local',
        's3', // Optional: backup to S3
    ],
],
```

### 4. Manual Backup

```bash
php artisan backup:run
```

### 5. Backup to S3 (Optional)

If using S3, configure in `.env`:

```env
AWS_ACCESS_KEY_ID=your_key
AWS_SECRET_ACCESS_KEY=your_secret
AWS_DEFAULT_REGION=us-east-1
AWS_BUCKET=your-backup-bucket
```

---

## WebSocket Server (Laravel Reverb)

### 1. Start Reverb Server

```bash
php artisan reverb:start --host=0.0.0.0 --port=8080
```

### 2. Create Supervisor Config for Reverb

```bash
sudo nano /etc/supervisor/conf.d/laravel-reverb.conf
```

Add:

```ini
[program:laravel-reverb]
process_name=%(program_name)s
command=php /var/www/html/laravel-app/artisan reverb:start --host=0.0.0.0 --port=8080
autostart=true
autorestart=true
stopasgroup=true
killasgroup=true
user=www-data
numprocs=1
redirect_stderr=true
stdout_logfile=/var/www/html/laravel-app/storage/logs/reverb.log
stopwaitsecs=60
```

### 3. Start Reverb with Supervisor

```bash
sudo supervisorctl reread
sudo supervisorctl update
sudo supervisorctl start laravel-reverb
```

### 4. Configure Nginx Proxy for WebSocket

Add to your Nginx server block:

```nginx
# WebSocket proxy for Laravel Reverb
location /app {
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "Upgrade";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_pass http://127.0.0.1:8080;
}
```

---

## SSL/HTTPS Configuration

### 1. Install Certbot

```bash
sudo apt install -y certbot python3-certbot-nginx
```

### 2. Obtain SSL Certificate

```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

### 3. Verify Auto-Renewal

```bash
sudo certbot renew --dry-run
```

### 4. Configure Nginx for Laravel

```bash
sudo nano /etc/nginx/sites-available/laravel-app
```

Add:

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    root /var/www/html/laravel-app/public;
    index index.php index.html;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers HIGH:!aNULL:!MD5;

    add_header X-Frame-Options "SAMEORIGIN";
    add_header X-Content-Type-Options "nosniff";
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    location / {
        try_files $uri $uri/ /index.php?$query_string;
    }

    location ~ \.php$ {
        fastcgi_pass unix:/var/run/php/php8.2-fpm.sock;
        fastcgi_index index.php;
        fastcgi_param SCRIPT_FILENAME $realpath_root$fastcgi_script_name;
        include fastcgi_params;
    }

    location ~ /\.(?!well-known).* {
        deny all;
    }

    # WebSocket proxy
    location /app {
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
        proxy_pass http://127.0.0.1:8080;
    }
}
```

### 5. Enable Site and Restart Nginx

```bash
sudo ln -s /etc/nginx/sites-available/laravel-app /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

---

## File Permissions

### 1. Set Correct Ownership

```bash
cd /var/www/html/laravel-app
sudo chown -R www-data:www-data .
```

### 2. Set Directory Permissions

```bash
sudo find . -type d -exec chmod 755 {} \;
sudo find . -type f -exec chmod 644 {} \;
```

### 3. Set Writable Directories

```bash
sudo chmod -R 775 storage bootstrap/cache
sudo chown -R www-data:www-data storage bootstrap/cache
```

### 4. Verify Permissions

```bash
ls -la storage/
ls -la bootstrap/cache/
```

Both directories should be owned by `www-data:www-data` and have `775` permissions.

---

## Security Hardening

### 1. Disable Directory Listing

Already configured in Nginx config above.

### 2. Hide Laravel Version

Edit `public/index.php` - already secure by default in Laravel 11.

### 3. Configure Firewall

```bash
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

### 4. Secure PHP Configuration

```bash
sudo nano /etc/php/8.2/fpm/php.ini
```

Update:
```ini
expose_php = Off
display_errors = Off
log_errors = On
```

### 5. Restart PHP-FPM

```bash
sudo systemctl restart php8.2-fpm
```

### 6. Enable Security Headers

Already configured in Nginx above:
- X-Frame-Options
- X-Content-Type-Options
- X-XSS-Protection
- Strict-Transport-Security (HSTS)

---

## Monitoring & Logging

### 1. Log Locations

```bash
# Application logs
/var/www/html/laravel-app/storage/logs/

# Nginx logs
/var/log/nginx/access.log
/var/log/nginx/error.log

# PHP-FPM logs
/var/log/php8.2-fpm.log

# Queue worker logs
/var/www/html/laravel-app/storage/logs/worker.log

# Reverb logs
/var/www/html/laravel-app/storage/logs/reverb.log
```

### 2. Configure Slack Notifications

In `.env`:
```env
LOG_SLACK_WEBHOOK_URL=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
LOG_SLACK_CHANNEL=#production-errors
LOG_SLACK_USERNAME=Laravel-Production
LOG_SLACK_LEVEL=error
```

### 3. Configure Email Alerts

Critical errors will be emailed to:
```env
MAIL_ERROR_TO=admin@yourdomain.com
```

### 4. Setup Sentry (Optional)

```bash
composer require sentry/sentry-laravel
```

In `.env`:
```env
SENTRY_LARAVEL_DSN=your_sentry_dsn
SENTRY_TRACES_SAMPLE_RATE=0.1
```

### 5. Monitor Queue Size

The scheduler automatically monitors queue size every 5 minutes and logs if it exceeds 100 jobs.

---

## Post-Deployment Checklist

- [ ] `.env` file configured with production values
- [ ] `APP_KEY` generated and set
- [ ] `APP_DEBUG=false` in production
- [ ] Database migrated successfully
- [ ] Storage link created (`php artisan storage:link`)
- [ ] File permissions set correctly (storage and bootstrap/cache writable)
- [ ] Queue workers running (verify with `supervisorctl status`)
- [ ] Cron job configured for scheduler
- [ ] SSL certificate installed and HTTPS working
- [ ] HTTPS forced in AppServiceProvider
- [ ] WebSocket server (Reverb) running
- [ ] Redis configured and running
- [ ] Backup scheduled and tested
- [ ] Logging configured (Slack/email for critical errors)
- [ ] Configuration cached (`php artisan config:cache`)
- [ ] Routes cached (`php artisan route:cache`)
- [ ] Views cached (`php artisan view:cache`)
- [ ] Firewall configured
- [ ] Security headers configured
- [ ] API keys updated (Stripe, WhatsApp, etc.)
- [ ] Domain DNS pointing to server
- [ ] Test all critical functionality
- [ ] Monitor logs for errors

---

## Troubleshooting

### Queue Workers Not Processing Jobs

```bash
# Check supervisor status
sudo supervisorctl status

# Restart workers
sudo supervisorctl restart laravel-worker:*

# Check worker logs
tail -f storage/logs/worker.log

# Verify Redis connection
redis-cli ping
```

### Scheduler Not Running

```bash
# Check crontab
sudo crontab -l

# Manually run scheduler
php artisan schedule:run

# Check logs
tail -f storage/logs/laravel.log
```

### 500 Internal Server Error

```bash
# Check Laravel logs
tail -f storage/logs/laravel.log

# Check Nginx error logs
sudo tail -f /var/log/nginx/error.log

# Check PHP-FPM logs
sudo tail -f /var/log/php8.2-fpm.log

# Verify permissions
ls -la storage/
ls -la bootstrap/cache/

# Clear and recache
php artisan config:clear
php artisan cache:clear
php artisan config:cache
```

### WebSocket Connection Failed

```bash
# Check Reverb is running
sudo supervisorctl status laravel-reverb

# Check Reverb logs
tail -f storage/logs/reverb.log

# Verify Nginx WebSocket proxy configuration
sudo nginx -t

# Test WebSocket connection
wscat -c wss://yourdomain.com/app/your_app_key
```

### Permission Denied Errors

```bash
# Fix storage permissions
sudo chmod -R 775 storage bootstrap/cache
sudo chown -R www-data:www-data storage bootstrap/cache

# Clear cache
php artisan cache:clear
php artisan config:clear
```

### Database Connection Failed

```bash
# Test database connection
php artisan tinker
>>> DB::connection()->getPdo();

# Check PostgreSQL is running
sudo systemctl status postgresql

# Verify credentials in .env match database
```

---

## Updating the Application

### 1. Put Application in Maintenance Mode

```bash
php artisan down
```

### 2. Pull Latest Changes

```bash
git pull origin main
```

### 3. Update Dependencies

```bash
composer install --optimize-autoloader --no-dev
npm install
npm run build
```

### 4. Run Migrations

```bash
php artisan migrate --force
```

### 5. Clear and Rebuild Cache

```bash
php artisan config:clear
php artisan cache:clear
php artisan config:cache
php artisan route:cache
php artisan view:cache
```

### 6. Restart Services

```bash
sudo supervisorctl restart laravel-worker:*
sudo supervisorctl restart laravel-reverb
sudo systemctl reload php8.2-fpm
```

### 7. Bring Application Back Online

```bash
php artisan up
```

---

## Support and Resources

- **Laravel Documentation**: https://laravel.com/docs
- **Laravel Deployment**: https://laravel.com/docs/deployment
- **Forge (Automated Deployment)**: https://forge.laravel.com
- **Envoyer (Zero-Downtime Deployment)**: https://envoyer.io

---

**Last Updated**: February 2026
**Version**: 1.0.0
