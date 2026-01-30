#!/bin/bash

# Laravel Deployment Script for Production

set -e

echo "======================================"
echo "Sea Cliff Laravel Deployment"
echo "======================================"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Configuration
DEPLOY_PATH="/var/www/seacliff"
REPO_URL="<your-git-repo-url>"
BRANCH="main"

# Check if running as root or with sudo
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}Please run with sudo${NC}"
    exit 1
fi

echo -e "${YELLOW}Deploying to: $DEPLOY_PATH${NC}"

# Pull latest changes
echo -e "${YELLOW}Pulling latest changes...${NC}"
cd $DEPLOY_PATH
git pull origin $BRANCH

# Install/Update dependencies
echo -e "${YELLOW}Installing dependencies...${NC}"
composer install --no-dev --optimize-autoloader --no-interaction

# Run database migrations
echo -e "${YELLOW}Running migrations...${NC}"
php artisan migrate --force --no-interaction

# Clear and cache config
echo -e "${YELLOW}Optimizing application...${NC}"
php artisan config:cache
php artisan route:cache
php artisan view:cache
php artisan event:cache

# Clear old cache
php artisan cache:clear

# Install NPM dependencies and build assets
echo -e "${YELLOW}Building assets...${NC}"
npm ci --production
npm run build

# Set permissions
echo -e "${YELLOW}Setting permissions...${NC}"
chown -R www-data:www-data $DEPLOY_PATH
chmod -R 755 $DEPLOY_PATH
chmod -R 775 $DEPLOY_PATH/storage
chmod -R 775 $DEPLOY_PATH/bootstrap/cache

# Restart services
echo -e "${YELLOW}Restarting services...${NC}"
systemctl restart php8.2-fpm
systemctl restart nginx

# Restart queue workers
echo -e "${YELLOW}Restarting queue workers...${NC}"
php artisan queue:restart

echo ""
echo -e "${GREEN}======================================"
echo "✓ Deployment Complete!"
echo "======================================${NC}"
echo ""
echo "Don't forget to:"
echo "  1. Test the application"
echo "  2. Check logs for errors"
echo "  3. Monitor performance"
echo ""
