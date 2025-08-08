#!/bin/bash

# Interface Exception Collector - Infrastructure Validation Script
# This script validates that all required infrastructure services are ready

set -e

# Configuration
NAMESPACE=${NAMESPACE:-"interface-exception-collector"}
TIMEOUT=${TIMEOUT:-300}

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

# Function to check if running in Docker Compose mode
is_docker_compose() {
    if docker-compose ps > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Function to validate Docker Compose services
validate_docker_compose() {
    log_info "Validating Docker Compose services..."
    
    # Check if docker-compose.yml exists
    if [ ! -f "docker-compose.yml" ]; then
        log_error "docker-compose.yml not found"
        return 1
    fi
    
    # Check PostgreSQL
    log_info "Checking PostgreSQL..."
    if docker-compose exec -T postgres pg_isready -U exception_user -d exception_collector_db > /dev/null 2>&1; then
        log_success "PostgreSQL is ready"
    else
        log_error "PostgreSQL is not ready"
        return 1
    fi
    
    # Check Kafka
    log_info "Checking Kafka..."
    if docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
        log_success "Kafka is ready"
    else
        log_error "Kafka is not ready"
        return 1
    fi
    
    # Check Redis
    log_info "Checking Redis..."
    if docker-compose exec -T redis redis-cli ping | grep -q PONG; then
        log_success "Redis is ready"
    else
        log_error "Redis is not ready"
        return 1
    fi
    
    log_success "All Docker Compose services are ready"
}

# Function to validate Kubernetes services
validate_kubernetes() {
    log_info "Validating Kubernetes services in namespace: $NAMESPACE"
    
    # Check if namespace exists
    if ! kubectl get namespace "$NAMESPACE" > /dev/null 2>&1; then
        log_error "Namespace '$NAMESPACE' does not exist"
        return 1
    fi
    
    # Check PostgreSQL
    log_info "Checking PostgreSQL..."
    if kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=postgresql \
        --namespace="$NAMESPACE" \
        --timeout="${TIMEOUT}s" > /dev/null 2>&1; then
        log_success "PostgreSQL is ready"
    else
        log_error "PostgreSQL is not ready"
        return 1
    fi
    
    # Check Redis
    log_info "Checking Redis..."
    if kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=redis \
        --namespace="$NAMESPACE" \
        --timeout="${TIMEOUT}s" > /dev/null 2>&1; then
        log_success "Redis is ready"
    else
        log_error "Redis is not ready"
        return 1
    fi
    
    # Check Kafka
    log_info "Checking Kafka..."
    if kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=kafka \
        --namespace="$NAMESPACE" \
        --timeout="${TIMEOUT}s" > /dev/null 2>&1; then
        log_success "Kafka is ready"
    else
        log_error "Kafka is not ready"
        return 1
    fi
    
    # Check Zookeeper (if exists)
    log_info "Checking Zookeeper..."
    if kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=zookeeper > /dev/null 2>&1; then
        if kubectl wait --for=condition=ready pod \
            --selector=app.kubernetes.io/name=zookeeper \
            --namespace="$NAMESPACE" \
            --timeout="${TIMEOUT}s" > /dev/null 2>&1; then
            log_success "Zookeeper is ready"
        else
            log_error "Zookeeper is not ready"
            return 1
        fi
    else
        log_info "Zookeeper not found (may not be required)"
    fi
    
    log_success "All Kubernetes services are ready"
}

# Function to test database connectivity
test_database_connectivity() {
    log_info "Testing database connectivity..."
    
    if is_docker_compose; then
        # Test Docker Compose PostgreSQL
        if docker-compose exec -T postgres psql -U exception_user -d exception_collector_db -c "SELECT 1;" > /dev/null 2>&1; then
            log_success "Database connectivity test passed"
        else
            log_error "Database connectivity test failed"
            return 1
        fi
    else
        # Test Kubernetes PostgreSQL
        local postgres_pod
        postgres_pod=$(kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=postgresql -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -n "$postgres_pod" ]; then
            if kubectl exec -n "$NAMESPACE" "$postgres_pod" -- psql -U postgres -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
                log_success "Database connectivity test passed"
            else
                log_error "Database connectivity test failed"
                return 1
            fi
        else
            log_error "No PostgreSQL pods found"
            return 1
        fi
    fi
}

# Function to test Kafka connectivity
test_kafka_connectivity() {
    log_info "Testing Kafka connectivity..."
    
    if is_docker_compose; then
        # Test Docker Compose Kafka
        if docker-compose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
            log_success "Kafka connectivity test passed"
        else
            log_error "Kafka connectivity test failed"
            return 1
        fi
    else
        # Test Kubernetes Kafka
        local kafka_pod
        kafka_pod=$(kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=kafka -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -n "$kafka_pod" ]; then
            if kubectl exec -n "$NAMESPACE" "$kafka_pod" -- kafka-broker-api-versions.sh --bootstrap-server localhost:9092 > /dev/null 2>&1; then
                log_success "Kafka connectivity test passed"
            else
                log_error "Kafka connectivity test failed"
                return 1
            fi
        else
            log_error "No Kafka pods found"
            return 1
        fi
    fi
}

# Function to test Redis connectivity
test_redis_connectivity() {
    log_info "Testing Redis connectivity..."
    
    if is_docker_compose; then
        # Test Docker Compose Redis
        if docker-compose exec -T redis redis-cli ping | grep -q PONG; then
            log_success "Redis connectivity test passed"
        else
            log_error "Redis connectivity test failed"
            return 1
        fi
    else
        # Test Kubernetes Redis
        local redis_pod
        redis_pod=$(kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=redis -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -n "$redis_pod" ]; then
            if kubectl exec -n "$NAMESPACE" "$redis_pod" -- redis-cli ping | grep -q PONG; then
                log_success "Redis connectivity test passed"
            else
                log_error "Redis connectivity test failed"
                return 1
            fi
        else
            log_error "No Redis pods found"
            return 1
        fi
    fi
}

# Function to show infrastructure status
show_infrastructure_status() {
    log_info "Infrastructure Status:"
    echo "====================="
    
    if is_docker_compose; then
        echo "Mode: Docker Compose"
        echo ""
        docker-compose ps
    else
        echo "Mode: Kubernetes"
        echo "Namespace: $NAMESPACE"
        echo ""
        kubectl get pods -n "$NAMESPACE"
        echo ""
        kubectl get services -n "$NAMESPACE"
    fi
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -n, --namespace NAMESPACE    Kubernetes namespace (default: interface-exception-collector)"
    echo "  -t, --timeout TIMEOUT       Timeout in seconds (default: 300)"
    echo "  --connectivity-only          Only test connectivity, skip readiness checks"
    echo "  --status-only               Only show status, skip validation"
    echo "  -h, --help                  Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  NAMESPACE                   Kubernetes namespace"
    echo "  TIMEOUT                     Timeout in seconds"
}

# Parse command line arguments
CONNECTIVITY_ONLY=false
STATUS_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -t|--timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        --connectivity-only)
            CONNECTIVITY_ONLY=true
            shift
            ;;
        --status-only)
            STATUS_ONLY=true
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

# Main function
main() {
    log_info "Starting infrastructure validation..."
    
    if [ "$STATUS_ONLY" = true ]; then
        show_infrastructure_status
        exit 0
    fi
    
    # Determine environment and validate accordingly
    if is_docker_compose; then
        log_info "Detected Docker Compose environment"
        if [ "$CONNECTIVITY_ONLY" != true ]; then
            validate_docker_compose
        fi
    else
        log_info "Detected Kubernetes environment"
        if [ "$CONNECTIVITY_ONLY" != true ]; then
            validate_kubernetes
        fi
    fi
    
    # Test connectivity
    test_database_connectivity
    test_kafka_connectivity
    test_redis_connectivity
    
    # Show final status
    show_infrastructure_status
    
    log_success "Infrastructure validation completed successfully!"
}

# Run main function
main "$@"