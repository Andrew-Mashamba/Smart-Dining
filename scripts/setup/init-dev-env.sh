#!/bin/bash

# Sea Cliff Development Environment Setup Script

set -e

echo "======================================"
echo "Sea Cliff Development Setup"
echo "======================================"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Docker Compose is not installed. Please install Docker Compose first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker and Docker Compose are installed${NC}"

# Navigate to project root
cd "$(dirname "$0")/../.."

# Create Laravel .env file if it doesn't exist
if [ ! -f laravel-app/.env ]; then
    echo -e "${YELLOW}Creating Laravel .env file...${NC}"
    cp laravel-app/.env.example laravel-app/.env
    echo -e "${GREEN}✓ Laravel .env file created${NC}"
else
    echo -e "${YELLOW}Laravel .env file already exists${NC}"
fi

# Build and start Docker containers
echo -e "${YELLOW}Building Docker containers...${NC}"
docker-compose build

echo -e "${YELLOW}Starting Docker containers...${NC}"
docker-compose up -d

# Wait for MySQL to be ready
echo -e "${YELLOW}Waiting for MySQL to be ready...${NC}"
sleep 10

# Install Laravel dependencies
echo -e "${YELLOW}Installing Laravel dependencies...${NC}"
docker-compose exec app composer install

# Generate Laravel application key
echo -e "${YELLOW}Generating application key...${NC}"
docker-compose exec app php artisan key:generate

# Run database migrations
echo -e "${YELLOW}Running database migrations...${NC}"
docker-compose exec app php artisan migrate --seed

# Install NPM dependencies
echo -e "${YELLOW}Installing NPM dependencies...${NC}"
docker-compose exec app npm install

# Build assets
echo -e "${YELLOW}Building assets...${NC}"
docker-compose exec app npm run build

echo ""
echo -e "${GREEN}======================================"
echo "✓ Setup Complete!"
echo "======================================${NC}"
echo ""
echo "Your application is now running at:"
echo -e "${GREEN}http://localhost:8000${NC}"
echo ""
echo "Useful commands:"
echo "  docker-compose up -d      - Start containers"
echo "  docker-compose down       - Stop containers"
echo "  docker-compose logs -f    - View logs"
echo "  docker-compose exec app bash - Access app container"
echo ""
echo "Access points:"
echo "  Manager Dashboard: http://localhost:8000/manager"
echo "  Waiter POS:        http://localhost:8000/waiter"
echo "  Kitchen Display:   http://localhost:8000/kitchen"
echo "  Bar Display:       http://localhost:8000/bar"
echo ""
