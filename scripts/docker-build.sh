#!/bin/bash

# Docker build script for Interface Exception Collector Service
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
IMAGE_NAME="interface-exception-collector"
TAG="latest"
BUILD_TYPE="production"
PUSH_IMAGE=false
REGISTRY=""

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  -n, --name NAME       Image name (default: interface-exception-collector)"
    echo "  -t, --tag TAG         Image tag (default: latest)"
    echo "  -d, --dev             Build development image"
    echo "  -p, --push            Push image to registry"
    echo "  -r, --registry REG    Registry URL for pushing"
    echo "  -h, --help            Display this help message"
    exit 1
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--name)
            IMAGE_NAME="$2"
            shift 2
            ;;
        -t|--tag)
            TAG="$2"
            shift 2
            ;;
        -d|--dev)
            BUILD_TYPE="development"
            shift
            ;;
        -p|--push)
            PUSH_IMAGE=true
            shift
            ;;
        -r|--registry)
            REGISTRY="$2"
            shift 2
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

# Set full image name
if [[ -n "$REGISTRY" ]]; then
    FULL_IMAGE_NAME="$REGISTRY/$IMAGE_NAME:$TAG"
else
    FULL_IMAGE_NAME="$IMAGE_NAME:$TAG"
fi

echo -e "${GREEN}Building Docker image...${NC}"
echo -e "${YELLOW}Image name: $FULL_IMAGE_NAME${NC}"
echo -e "${YELLOW}Build type: $BUILD_TYPE${NC}"

# Build the image
if [[ "$BUILD_TYPE" == "development" ]]; then
    echo -e "${GREEN}Building development image...${NC}"
    docker build -f Dockerfile.dev -t "$FULL_IMAGE_NAME" .
else
    echo -e "${GREEN}Building production image...${NC}"
    docker build -f Dockerfile -t "$FULL_IMAGE_NAME" .
fi

# Check if build was successful
if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✓ Docker image built successfully: $FULL_IMAGE_NAME${NC}"
    
    # Display image size
    IMAGE_SIZE=$(docker images "$FULL_IMAGE_NAME" --format "table {{.Size}}" | tail -n 1)
    echo -e "${YELLOW}Image size: $IMAGE_SIZE${NC}"
    
    # Push image if requested
    if [[ "$PUSH_IMAGE" == true ]]; then
        if [[ -z "$REGISTRY" ]]; then
            echo -e "${RED}Error: Registry URL required for pushing${NC}"
            exit 1
        fi
        
        echo -e "${GREEN}Pushing image to registry...${NC}"
        docker push "$FULL_IMAGE_NAME"
        
        if [[ $? -eq 0 ]]; then
            echo -e "${GREEN}✓ Image pushed successfully${NC}"
        else
            echo -e "${RED}✗ Failed to push image${NC}"
            exit 1
        fi
    fi
else
    echo -e "${RED}✗ Docker build failed${NC}"
    exit 1
fi

echo -e "${GREEN}Build completed successfully!${NC}"