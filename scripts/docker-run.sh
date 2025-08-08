#!/bin/bash

# Docker run script for Interface Exception Collector Service
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
COMPOSE_FILE="docker-compose.yml"
SERVICE_NAME="exception-collector"
ENVIRONMENT="development"
DETACHED=false
BUILD=false
LOGS=false

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS] [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  up          Start services"
    echo "  down        Stop services"
    echo "  restart     Restart services"
    echo "  logs        Show logs"
    echo "  build       Build and start services"
    echo "  clean       Stop services and remove volumes"
    echo ""
    echo "Options:"
    echo "  -f, --file FILE       Docker compose file (default: docker-compose.yml)"
    echo "  -s, --service NAME    Service name (default: exception-collector)"
    echo "  -e, --env ENV         Environment (development|production)"
    echo "  -d, --detach          Run in detached mode"
    echo "  -b, --build           Build images before starting"
    echo "  -l, --logs            Follow logs after starting"
    echo "  --monitoring          Include monitoring services"
    echo "  -h, --help            Display this help message"
    exit 1
}

# Parse command line arguments
COMMAND=""
MONITORING=false

while [[ $# -gt 0 ]]; do
    case $1 in
        up|down|restart|logs|build|clean)
            COMMAND="$1"
            shift
            ;;
        -f|--file)
            COMPOSE_FILE="$2"
            shift 2
            ;;
        -s|--service)
            SERVICE_NAME="$2"
            shift 2
            ;;
        -e|--env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -d|--detach)
            DETACHED=true
            shift
            ;;
        -b|--build)
            BUILD=true
            shift
            ;;
        -l|--logs)
            LOGS=true
            shift
            ;;
        --monitoring)
            MONITORING=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            usage
            ;;
    esac
done

# Set default command if not provided
if [[ -z "$COMMAND" ]]; then
    COMMAND="up"
fi

# Build compose command
COMPOSE_CMD="docker-compose -f $COMPOSE_FILE"

# Add monitoring profile if requested
if [[ "$MONITORING" == true ]]; then
    COMPOSE_CMD="$COMPOSE_CMD --profile monitoring"
fi

# Set environment variables
export COMPOSE_PROJECT_NAME="exception-collector"

echo -e "${GREEN}Interface Exception Collector Service - Docker Management${NC}"
echo -e "${YELLOW}Command: $COMMAND${NC}"
echo -e "${YELLOW}Environment: $ENVIRONMENT${NC}"
echo -e "${YELLOW}Compose file: $COMPOSE_FILE${NC}"

case $COMMAND in
    up)
        echo -e "${GREEN}Starting services...${NC}"
        
        if [[ "$BUILD" == true ]]; then
            echo -e "${YELLOW}Building images...${NC}"
            $COMPOSE_CMD build
        fi
        
        if [[ "$DETACHED" == true ]]; then
            $COMPOSE_CMD up -d
        else
            $COMPOSE_CMD up
        fi
        
        if [[ "$LOGS" == true && "$DETACHED" == true ]]; then
            echo -e "${GREEN}Following logs...${NC}"
            $COMPOSE_CMD logs -f $SERVICE_NAME
        fi
        ;;
        
    down)
        echo -e "${GREEN}Stopping services...${NC}"
        $COMPOSE_CMD down
        ;;
        
    restart)
        echo -e "${GREEN}Restarting services...${NC}"
        $COMPOSE_CMD restart $SERVICE_NAME
        ;;
        
    logs)
        echo -e "${GREEN}Showing logs...${NC}"
        $COMPOSE_CMD logs -f $SERVICE_NAME
        ;;
        
    build)
        echo -e "${GREEN}Building and starting services...${NC}"
        $COMPOSE_CMD up --build
        ;;
        
    clean)
        echo -e "${GREEN}Cleaning up services and volumes...${NC}"
        $COMPOSE_CMD down -v --remove-orphans
        docker system prune -f
        ;;
        
    *)
        echo -e "${RED}Unknown command: $COMMAND${NC}"
        usage
        ;;
esac

echo -e "${GREEN}Operation completed!${NC}"