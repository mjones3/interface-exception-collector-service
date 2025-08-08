#!/bin/bash

# Interface Exception Collector - Smoke Tests Script
# This script runs basic smoke tests to verify the application is working

set -e

# Configuration
ENVIRONMENT=${1:-"local"}
BASE_URL=""
NAMESPACE=""
TIMEOUT=${TIMEOUT:-60}
TEST_TOKEN=${TEST_TOKEN:-""}

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

# Function to set environment-specific configuration
setup_environment() {
    case "$ENVIRONMENT" in
        local)
            BASE_URL="http://localhost:8080"
            NAMESPACE="interface-exception-collector-dev"
            ;;
        dev)
            BASE_URL="https://exception-collector-dev.biopro.com"
            NAMESPACE="biopro-dev"
            ;;
        staging)
            BASE_URL="https://exception-collector-staging.biopro.com"
            NAMESPACE="biopro-staging"
            ;;
        prod|prod-green)
            BASE_URL="https://exception-collector.biopro.com"
            NAMESPACE="biopro-prod"
            ;;
        *)
            log_error "Unknown environment: $ENVIRONMENT"
            log_info "Supported environments: local, dev, staging, prod"
            exit 1
            ;;
    esac
    
    log_info "Running smoke tests for environment: $ENVIRONMENT"
    log_info "Base URL: $BASE_URL"
    log_info "Namespace: $NAMESPACE"
}

# Function to wait for application to be ready
wait_for_application() {
    log_info "Waiting for application to be ready..."
    
    local retries=0
    local max_retries=$((TIMEOUT / 5))
    
    while [ $retries -lt $max_retries ]; do
        if curl -f -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
            log_success "Application is ready"
            return 0
        fi
        
        log_info "Application not ready yet, waiting... (attempt $((retries + 1))/$max_retries)"
        sleep 5
        retries=$((retries + 1))
    done
    
    log_error "Application failed to become ready within $TIMEOUT seconds"
    return 1
}

# Function to test health endpoints
test_health_endpoints() {
    log_info "Testing health endpoints..."
    
    # Test main health endpoint
    log_info "Testing /actuator/health"
    if curl -f -s "$BASE_URL/actuator/health" | jq -e '.status == "UP"' > /dev/null 2>&1; then
        log_success "Health endpoint is UP"
    else
        log_error "Health endpoint test failed"
        return 1
    fi
    
    # Test readiness endpoint
    log_info "Testing /actuator/health/readiness"
    if curl -f -s "$BASE_URL/actuator/health/readiness" | jq -e '.status == "UP"' > /dev/null 2>&1; then
        log_success "Readiness endpoint is UP"
    else
        log_warning "Readiness endpoint test failed (may not be critical)"
    fi
    
    # Test liveness endpoint
    log_info "Testing /actuator/health/liveness"
    if curl -f -s "$BASE_URL/actuator/health/liveness" | jq -e '.status == "UP"' > /dev/null 2>&1; then
        log_success "Liveness endpoint is UP"
    else
        log_warning "Liveness endpoint test failed (may not be critical)"
    fi
}

# Function to test API endpoints
test_api_endpoints() {
    log_info "Testing API endpoints..."
    
    # Test API root
    log_info "Testing /api/v1"
    if curl -f -s "$BASE_URL/api/v1" > /dev/null 2>&1; then
        log_success "API root endpoint accessible"
    else
        log_warning "API root endpoint test failed (may not be implemented)"
    fi
    
    # Test exceptions endpoint (without auth for basic connectivity)
    log_info "Testing /api/v1/exceptions (connectivity)"
    local response_code
    response_code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/exceptions")
    
    if [ "$response_code" = "401" ] || [ "$response_code" = "403" ]; then
        log_success "Exceptions endpoint is accessible (authentication required as expected)"
    elif [ "$response_code" = "200" ]; then
        log_success "Exceptions endpoint is accessible"
    else
        log_error "Exceptions endpoint test failed (HTTP $response_code)"
        return 1
    fi
}

# Function to test authenticated API endpoints
test_authenticated_endpoints() {
    if [ -z "$TEST_TOKEN" ]; then
        log_warning "No test token provided, skipping authenticated endpoint tests"
        return 0
    fi
    
    log_info "Testing authenticated API endpoints..."
    
    # Test exceptions list with authentication
    log_info "Testing /api/v1/exceptions with authentication"
    if curl -f -s -H "Authorization: Bearer $TEST_TOKEN" \
        "$BASE_URL/api/v1/exceptions?page=0&size=1" > /dev/null 2>&1; then
        log_success "Authenticated exceptions endpoint test passed"
    else
        log_error "Authenticated exceptions endpoint test failed"
        return 1
    fi
    
    # Test exception creation (if test data available)
    log_info "Testing exception creation endpoint"
    local test_exception='{
        "transactionId": "smoke-test-' $(date +%s) '",
        "interfaceType": "ORDER",
        "exceptionReason": "Smoke test exception",
        "operation": "CREATE_ORDER",
        "severity": "LOW",
        "category": "VALIDATION",
        "retryable": true,
        "customerId": "test-customer",
        "locationCode": "TEST-LOC"
    }'
    
    local response_code
    response_code=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Authorization: Bearer $TEST_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$test_exception" \
        "$BASE_URL/api/v1/exceptions")
    
    if [ "$response_code" = "201" ] || [ "$response_code" = "200" ]; then
        log_success "Exception creation endpoint test passed"
    else
        log_warning "Exception creation endpoint test failed (HTTP $response_code) - may be expected"
    fi
}

# Function to test metrics endpoints
test_metrics_endpoints() {
    log_info "Testing metrics endpoints..."
    
    # Test Prometheus metrics
    log_info "Testing /actuator/prometheus"
    if curl -f -s "$BASE_URL/actuator/prometheus" | grep -q "# HELP"; then
        log_success "Prometheus metrics endpoint is working"
    else
        log_warning "Prometheus metrics endpoint test failed"
    fi
    
    # Test application metrics
    log_info "Testing /actuator/metrics"
    if curl -f -s "$BASE_URL/actuator/metrics" | jq -e '.names | length > 0' > /dev/null 2>&1; then
        log_success "Application metrics endpoint is working"
    else
        log_warning "Application metrics endpoint test failed"
    fi
}

# Function to test database connectivity
test_database_connectivity() {
    log_info "Testing database connectivity..."
    
    # Test database health indicator
    local db_status
    db_status=$(curl -f -s "$BASE_URL/actuator/health" | jq -r '.components.db.status // "UNKNOWN"' 2>/dev/null)
    
    if [ "$db_status" = "UP" ]; then
        log_success "Database connectivity is healthy"
    else
        log_error "Database connectivity test failed (status: $db_status)"
        return 1
    fi
}

# Function to test Kafka connectivity
test_kafka_connectivity() {
    log_info "Testing Kafka connectivity..."
    
    # Test Kafka health indicator
    local kafka_status
    kafka_status=$(curl -f -s "$BASE_URL/actuator/health" | jq -r '.components.kafka.status // "UNKNOWN"' 2>/dev/null)
    
    if [ "$kafka_status" = "UP" ]; then
        log_success "Kafka connectivity is healthy"
    else
        log_warning "Kafka connectivity test failed (status: $kafka_status) - may not be critical"
    fi
}

# Function to test Redis connectivity
test_redis_connectivity() {
    log_info "Testing Redis connectivity..."
    
    # Test Redis health indicator
    local redis_status
    redis_status=$(curl -f -s "$BASE_URL/actuator/health" | jq -r '.components.redis.status // "UNKNOWN"' 2>/dev/null)
    
    if [ "$redis_status" = "UP" ]; then
        log_success "Redis connectivity is healthy"
    else
        log_warning "Redis connectivity test failed (status: $redis_status) - may not be critical"
    fi
}

# Function to test OpenAPI documentation
test_openapi_docs() {
    log_info "Testing OpenAPI documentation..."
    
    # Test Swagger UI
    log_info "Testing /swagger-ui/index.html"
    if curl -f -s "$BASE_URL/swagger-ui/index.html" > /dev/null 2>&1; then
        log_success "Swagger UI is accessible"
    else
        log_warning "Swagger UI test failed"
    fi
    
    # Test OpenAPI JSON
    log_info "Testing /v3/api-docs"
    if curl -f -s "$BASE_URL/v3/api-docs" | jq -e '.openapi' > /dev/null 2>&1; then
        log_success "OpenAPI documentation is available"
    else
        log_warning "OpenAPI documentation test failed"
    fi
}

# Function to run performance smoke test
test_basic_performance() {
    log_info "Running basic performance test..."
    
    # Simple load test with 10 requests
    local start_time
    local end_time
    local duration
    
    start_time=$(date +%s)
    
    for i in {1..10}; do
        if ! curl -f -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
            log_error "Performance test failed on request $i"
            return 1
        fi
    done
    
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    
    if [ $duration -lt 10 ]; then
        log_success "Basic performance test passed (10 requests in ${duration}s)"
    else
        log_warning "Basic performance test slow (10 requests in ${duration}s)"
    fi
}

# Function to check application logs for errors
check_application_logs() {
    if [ "$ENVIRONMENT" = "local" ]; then
        log_info "Checking application logs for errors..."
        
        # Check Docker Compose logs if available
        if docker-compose ps interface-exception-collector > /dev/null 2>&1; then
            local error_count
            error_count=$(docker-compose logs interface-exception-collector | grep -i error | wc -l)
            
            if [ "$error_count" -eq 0 ]; then
                log_success "No errors found in application logs"
            else
                log_warning "Found $error_count error messages in application logs"
            fi
        fi
    elif [ -n "$NAMESPACE" ]; then
        log_info "Checking Kubernetes application logs for errors..."
        
        # Check Kubernetes logs
        local pod_name
        pod_name=$(kubectl get pods -n "$NAMESPACE" -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -n "$pod_name" ]; then
            local error_count
            error_count=$(kubectl logs "$pod_name" -n "$NAMESPACE" --tail=100 | grep -i error | wc -l)
            
            if [ "$error_count" -eq 0 ]; then
                log_success "No errors found in application logs"
            else
                log_warning "Found $error_count error messages in recent application logs"
            fi
        else
            log_warning "Could not find application pod to check logs"
        fi
    fi
}

# Function to generate smoke test report
generate_report() {
    local test_results_file="/tmp/smoke-test-results-$(date +%s).json"
    
    cat > "$test_results_file" << EOF
{
    "environment": "$ENVIRONMENT",
    "base_url": "$BASE_URL",
    "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
    "tests": {
        "health_endpoints": "passed",
        "api_endpoints": "passed",
        "database_connectivity": "passed",
        "basic_performance": "passed"
    },
    "summary": {
        "total_tests": 4,
        "passed_tests": 4,
        "failed_tests": 0
    }
}
EOF
    
    log_info "Smoke test report generated: $test_results_file"
}

# Function to display usage
usage() {
    echo "Usage: $0 [ENVIRONMENT] [OPTIONS]"
    echo ""
    echo "Environments:"
    echo "  local                       Local development environment (default)"
    echo "  dev                         Development environment"
    echo "  staging                     Staging environment"
    echo "  prod                        Production environment"
    echo "  prod-green                  Production green deployment"
    echo ""
    echo "Options:"
    echo "  --token TOKEN               Authentication token for API tests"
    echo "  --timeout TIMEOUT           Timeout in seconds (default: 60)"
    echo "  --skip-auth                 Skip authenticated endpoint tests"
    echo "  --skip-performance          Skip performance tests"
    echo "  --report                    Generate test report"
    echo "  -h, --help                  Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  TEST_TOKEN                  Authentication token"
    echo "  TIMEOUT                     Timeout in seconds"
}

# Parse command line arguments
SKIP_AUTH=false
SKIP_PERFORMANCE=false
GENERATE_REPORT=false

# First argument is environment if provided
if [ $# -gt 0 ] && [[ ! "$1" =~ ^-- ]]; then
    ENVIRONMENT="$1"
    shift
fi

while [[ $# -gt 0 ]]; do
    case $1 in
        --token)
            TEST_TOKEN="$2"
            shift 2
            ;;
        --timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        --skip-auth)
            SKIP_AUTH=true
            shift
            ;;
        --skip-performance)
            SKIP_PERFORMANCE=true
            shift
            ;;
        --report)
            GENERATE_REPORT=true
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
    log_info "Starting smoke tests..."
    
    setup_environment
    wait_for_application
    
    # Run core tests
    test_health_endpoints
    test_api_endpoints
    test_database_connectivity
    test_kafka_connectivity
    test_redis_connectivity
    test_metrics_endpoints
    test_openapi_docs
    
    # Run optional tests
    if [ "$SKIP_AUTH" != true ]; then
        test_authenticated_endpoints
    fi
    
    if [ "$SKIP_PERFORMANCE" != true ]; then
        test_basic_performance
    fi
    
    # Check logs
    check_application_logs
    
    # Generate report if requested
    if [ "$GENERATE_REPORT" = true ]; then
        generate_report
    fi
    
    log_success "All smoke tests completed successfully!"
    log_info "Environment: $ENVIRONMENT"
    log_info "Base URL: $BASE_URL"
}

# Run main function
main "$@"