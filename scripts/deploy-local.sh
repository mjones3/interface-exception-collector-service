#!/bin/bash

# Interface Exception Collector - Local Development Deployment Script
# This script sets up local development environment with Tilt

set -e

# Configuration
TILT_FILE=${TILT_FILE:-"Tiltfile"}
LOCAL_REGISTRY_PORT=${LOCAL_REGISTRY_PORT:-5000}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
KAFKA_PORT=${KAFKA_PORT:-9092}
REDIS_PORT=${REDIS_PORT:-6379}

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
    log_info "Checking prerequisites for local development..."
    
    if ! command -v tilt &> /dev/null; then
        log_error "Tilt is not installed. Please install Tilt from https://tilt.dev/"
        exit 1
    fi
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        log_error "docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v helm &> /dev/null; then
        log_warning "helm is not installed. Some features may not work properly."
    fi
    
    # Check if Docker is running
    if ! docker info &> /dev/null; then
        log_error "Docker is not running. Please start Docker Desktop or Docker daemon."
        exit 1
    fi
    
    log_success "All prerequisites are available"
}

# Function to check Kubernetes context
check_kubernetes_context() {
    log_info "Checking Kubernetes context..."
    
    CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
    
    if [ "$CURRENT_CONTEXT" = "none" ]; then
        log_error "No Kubernetes context is set. Please configure kubectl."
        exit 1
    fi
    
    # Warn if not using local development context
    if [[ ! "$CURRENT_CONTEXT" =~ (docker-desktop|minikube|kind|k3d) ]]; then
        log_warning "Current context '$CURRENT_CONTEXT' doesn't appear to be a local development cluster."
        read -p "Do you want to continue? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Deployment cancelled by user"
            exit 0
        fi
    fi
    
    log_info "Using Kubernetes context: $CURRENT_CONTEXT"
}

# Function to setup local Docker registry
setup_local_registry() {
    log_info "Setting up local Docker registry..."
    
    # Check if registry is already running
    if docker ps --format "table {{.Names}}" | grep -q "^local-registry$"; then
        log_info "Local registry is already running"
        return 0
    fi
    
    # Start local registry
    docker run -d \
        --name local-registry \
        --restart=always \
        -p "${LOCAL_REGISTRY_PORT}:5000" \
        registry:2 || {
        log_warning "Failed to start local registry, it might already be running"
    }
    
    # Wait for registry to be ready
    sleep 2
    
    log_success "Local Docker registry is running on port $LOCAL_REGISTRY_PORT"
}

# Function to validate Tiltfile
validate_tiltfile() {
    log_info "Validating Tiltfile..."
    
    if [ ! -f "$TILT_FILE" ]; then
        log_error "Tiltfile not found at $TILT_FILE"
        log_info "Please ensure you're running this script from the project root directory"
        exit 1
    fi
    
    # Basic validation of Tiltfile content
    if ! grep -q "k8s_yaml" "$TILT_FILE"; then
        log_warning "Tiltfile doesn't contain k8s_yaml declarations. This might not be a valid Tilt configuration."
    fi
    
    log_success "Tiltfile validation completed"
}

# Function to check port availability
check_port_availability() {
    local port=$1
    local service=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warning "Port $port is already in use (required for $service)"
        log_info "You may need to stop other services or change port configuration"
    fi
}

# Function to check local development ports
check_local_ports() {
    log_info "Checking local development ports..."
    
    check_port_availability $LOCAL_REGISTRY_PORT "Docker Registry"
    check_port_availability $POSTGRES_PORT "PostgreSQL"
    check_port_availability $KAFKA_PORT "Kafka"
    check_port_availability $REDIS_PORT "Redis"
    check_port_availability 8080 "Application"
    check_port_availability 10350 "Tilt UI"
}

# Function to setup development environment
setup_dev_environment() {
    log_info "Setting up development environment..."
    
    # Create local development namespace
    kubectl create namespace interface-exception-collector-dev --dry-run=client -o yaml | kubectl apply -f -
    
    # Setup local registry
    setup_local_registry
    
    # Run infrastructure setup if needed
    if [ -f "scripts/setup-infrastructure.sh" ]; then
        log_info "Running infrastructure setup..."
        bash scripts/setup-infrastructure.sh --local
    fi
    
    log_success "Development environment setup completed"
}

# Function to start Tilt
start_tilt() {
    log_info "Starting Tilt for local development..."
    
    # Check if Tilt is already running
    if pgrep -f "tilt up" > /dev/null; then
        log_warning "Tilt appears to be already running"
        read -p "Do you want to stop the existing Tilt process and start a new one? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            pkill -f "tilt up" || true
            sleep 2
        else
            log_info "Keeping existing Tilt process running"
            return 0
        fi
    fi
    
    # Start Tilt
    log_info "Starting Tilt UI at http://localhost:10350"
    log_info "Press Ctrl+C to stop Tilt and clean up resources"
    
    tilt up --file="$TILT_FILE"
}

# Function to show local development information
show_dev_info() {
    log_info "Local Development Environment Information:"
    echo "=========================================="
    echo "Tilt UI: http://localhost:10350"
    echo "Application: http://localhost:8080"
    echo "Local Registry: localhost:$LOCAL_REGISTRY_PORT"
    echo "PostgreSQL: localhost:$POSTGRES_PORT"
    echo "Kafka: localhost:$KAFKA_PORT"
    echo "Redis: localhost:$REDIS_PORT"
    echo "=========================================="
    echo ""
    echo "Useful commands:"
    echo "  tilt down                    # Stop all services"
    echo "  kubectl get pods -n interface-exception-collector-dev  # Check pod status"
    echo "  kubectl logs -f deployment/interface-exception-collector -n interface-exception-collector-dev  # View logs"
    echo "  docker logs local-registry   # Check registry logs"
}

# Function to cleanup on exit
cleanup() {
    log_info "Cleaning up local development environment..."
    
    # Stop Tilt if running
    if pgrep -f "tilt up" > /dev/null; then
        log_info "Stopping Tilt..."
        pkill -f "tilt up" || true
    fi
    
    # Run Tilt down to clean up resources
    if [ -f "$TILT_FILE" ]; then
        tilt down --file="$TILT_FILE" 2>/dev/null || true
    fi
    
    log_success "Cleanup completed"
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --tiltfile FILE              Use specific Tiltfile (default: Tiltfile)"
    echo "  --registry-port PORT         Local registry port (default: 5000)"
    echo "  --postgres-port PORT         PostgreSQL port (default: 5432)"
    echo "  --kafka-port PORT            Kafka port (default: 9092)"
    echo "  --redis-port PORT            Redis port (default: 6379)"
    echo "  --setup-only                 Only setup environment, don't start Tilt"
    echo "  --info                       Show development environment information"
    echo "  --cleanup                    Cleanup local development environment"
    echo "  -h, --help                   Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  TILT_FILE                    Path to Tiltfile"
    echo "  LOCAL_REGISTRY_PORT          Local Docker registry port"
    echo "  POSTGRES_PORT                PostgreSQL port"
    echo "  KAFKA_PORT                   Kafka port"
    echo "  REDIS_PORT                   Redis port"
}

# Parse command line arguments
SETUP_ONLY=false
SHOW_INFO=false
CLEANUP_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --tiltfile)
            TILT_FILE="$2"
            shift 2
            ;;
        --registry-port)
            LOCAL_REGISTRY_PORT="$2"
            shift 2
            ;;
        --postgres-port)
            POSTGRES_PORT="$2"
            shift 2
            ;;
        --kafka-port)
            KAFKA_PORT="$2"
            shift 2
            ;;
        --redis-port)
            REDIS_PORT="$2"
            shift 2
            ;;
        --setup-only)
            SETUP_ONLY=true
            shift
            ;;
        --info)
            SHOW_INFO=true
            shift
            ;;
        --cleanup)
            CLEANUP_ONLY=true
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

# Set up signal handlers for cleanup
trap cleanup EXIT INT TERM

# Main function
main() {
    if [ "$CLEANUP_ONLY" = true ]; then
        cleanup
        exit 0
    fi
    
    if [ "$SHOW_INFO" = true ]; then
        show_dev_info
        exit 0
    fi
    
    log_info "Starting local development environment setup..."
    
    check_prerequisites
    check_kubernetes_context
    validate_tiltfile
    check_local_ports
    setup_dev_environment
    
    if [ "$SETUP_ONLY" = true ]; then
        log_success "Development environment setup completed"
        show_dev_info
        exit 0
    fi
    
    show_dev_info
    start_tilt
}

# Run main function
main "$@"