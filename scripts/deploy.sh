#!/bin/bash

# Interface Exception Collector - Kubernetes Deployment Script
# This script deploys the application to Kubernetes without Tilt

set -e

# Configuration
NAMESPACE=${NAMESPACE:-"interface-exception-collector"}
ENVIRONMENT=${ENVIRONMENT:-"dev"}
HELM_RELEASE_NAME=${HELM_RELEASE_NAME:-"interface-exception-collector"}
DOCKER_IMAGE_TAG=${DOCKER_IMAGE_TAG:-"latest"}
DOCKER_REGISTRY=${DOCKER_REGISTRY:-"localhost:5000"}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if required tools are installed
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v helm &> /dev/null; then
        log_error "helm is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        log_error "docker is not installed or not in PATH"
        exit 1
    fi
    
    log_success "All prerequisites are installed"
}

# Function to build and push Docker image
build_and_push_image() {
    log_info "Building Docker image..."
    
    # Build the application
    ./mvnw clean package -DskipTests
    
    # Build Docker image
    docker build -t "${DOCKER_REGISTRY}/interface-exception-collector:${DOCKER_IMAGE_TAG}" .
    
    # Push to registry
    log_info "Pushing Docker image to registry..."
    docker push "${DOCKER_REGISTRY}/interface-exception-collector:${DOCKER_IMAGE_TAG}"
    
    log_success "Docker image built and pushed successfully"
}

# Function to create namespace if it doesn't exist
create_namespace() {
    log_info "Creating namespace if it doesn't exist..."
    
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        kubectl create namespace "$NAMESPACE"
        log_success "Namespace '$NAMESPACE' created"
    else
        log_info "Namespace '$NAMESPACE' already exists"
    fi
}

# Function to deploy with Helm
deploy_with_helm() {
    log_info "Deploying application with Helm..."
    
    # Check if Helm chart exists
    if [ ! -d "helm" ]; then
        log_error "Helm chart directory not found. Please ensure helm/ directory exists."
        exit 1
    fi
    
    # Determine values file based on environment
    VALUES_FILE="helm/values-${ENVIRONMENT}.yaml"
    if [ ! -f "$VALUES_FILE" ]; then
        log_warning "Environment-specific values file not found: $VALUES_FILE"
        log_info "Using default values.yaml"
        VALUES_FILE="helm/values.yaml"
    fi
    
    # Deploy or upgrade the Helm release
    helm upgrade --install "$HELM_RELEASE_NAME" ./helm \
        --namespace "$NAMESPACE" \
        --values "$VALUES_FILE" \
        --set image.tag="$DOCKER_IMAGE_TAG" \
        --set image.repository="${DOCKER_REGISTRY}/interface-exception-collector" \
        --wait \
        --timeout=10m
    
    log_success "Application deployed successfully"
}

# Function to verify deployment
verify_deployment() {
    log_info "Verifying deployment..."
    
    # Wait for pods to be ready
    kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=interface-exception-collector \
        --namespace="$NAMESPACE" \
        --timeout=300s
    
    # Check deployment status
    kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=interface-exception-collector
    
    # Get service information
    kubectl get services -n "$NAMESPACE" -l app.kubernetes.io/name=interface-exception-collector
    
    log_success "Deployment verification completed"
}

# Function to show deployment information
show_deployment_info() {
    log_info "Deployment Information:"
    echo "=========================="
    echo "Namespace: $NAMESPACE"
    echo "Environment: $ENVIRONMENT"
    echo "Helm Release: $HELM_RELEASE_NAME"
    echo "Docker Image: ${DOCKER_REGISTRY}/interface-exception-collector:${DOCKER_IMAGE_TAG}"
    echo "=========================="
    
    # Show application endpoints
    log_info "Application endpoints:"
    kubectl get ingress -n "$NAMESPACE" 2>/dev/null || log_info "No ingress configured"
    
    # Show service endpoints
    kubectl get services -n "$NAMESPACE" -l app.kubernetes.io/name=interface-exception-collector
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -e, --environment ENVIRONMENT    Set deployment environment (default: dev)"
    echo "  -n, --namespace NAMESPACE        Set Kubernetes namespace (default: interface-exception-collector)"
    echo "  -t, --tag TAG                   Set Docker image tag (default: latest)"
    echo "  -r, --registry REGISTRY         Set Docker registry (default: localhost:5000)"
    echo "  --skip-build                    Skip Docker image build and push"
    echo "  --dry-run                       Show what would be deployed without actually deploying"
    echo "  -h, --help                      Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  NAMESPACE                       Kubernetes namespace"
    echo "  ENVIRONMENT                     Deployment environment"
    echo "  DOCKER_IMAGE_TAG               Docker image tag"
    echo "  DOCKER_REGISTRY                Docker registry URL"
    echo "  HELM_RELEASE_NAME              Helm release name"
}

# Parse command line arguments
SKIP_BUILD=false
DRY_RUN=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -t|--tag)
            DOCKER_IMAGE_TAG="$2"
            shift 2
            ;;
        -r|--registry)
            DOCKER_REGISTRY="$2"
            shift 2
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Main deployment process
main() {
    log_info "Starting deployment process..."
    
    check_prerequisites
    
    if [ "$DRY_RUN" = true ]; then
        log_info "DRY RUN MODE - No actual deployment will be performed"
        show_deployment_info
        exit 0
    fi
    
    if [ "$SKIP_BUILD" = false ]; then
        build_and_push_image
    else
        log_info "Skipping Docker image build and push"
    fi
    
    create_namespace
    deploy_with_helm
    verify_deployment
    show_deployment_info
    
    log_success "Deployment completed successfully!"
    log_info "You can check the application status with: kubectl get pods -n $NAMESPACE"
}

# Run main function
main "$@"