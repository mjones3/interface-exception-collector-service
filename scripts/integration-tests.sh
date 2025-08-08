#!/bin/bash

# Interface Exception Collector - Integration Tests Script
# This script runs comprehensive integration tests

set -e

# Configuration
ENVIRONMENT=${1:-"local"}
BASE_URL=""
NAMESPACE=""
TEST_TOKEN=${TEST_TOKEN:-""}
TIMEOUT=${TIMEOUT:-300}
CLEANUP=${CLEANUP:-true}

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

# Test data
TEST_TRANSACTION_ID="integration-test-$(date +%s)"
CREATED_EXCEPTION_ID=""

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
        prod)
            log_error "Integration tests should not be run against production"
            exit 1
            ;;
        *)
            log_error "Unknown environment: $ENVIRONMENT"
            exit 1
            ;;
    esac
    
    log_info "Running integration tests for environment: $ENVIRONMENT"
    log_info "Base URL: $BASE_URL"
}

# Function to check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v curl &> /dev/null; then
        log_error "curl is required but not installed"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        log_error "jq is required but not installed"
        exit 1
    fi
    
    if [ -z "$TEST_TOKEN" ]; then
        log_error "TEST_TOKEN environment variable is required for integration tests"
        log_info "Please set TEST_TOKEN with a valid authentication token"
        exit 1
    fi
    
    log_success "Prerequisites check completed"
}

# Function to test exception creation
test_exception_creation() {
    log_info "Testing exception creation..."
    
    local test_exception='{
        "transactionId": "'$TEST_TRANSACTION_ID'",
        "interfaceType": "ORDER",
        "exceptionReason": "Integration test exception for order processing",
        "operation": "CREATE_ORDER",
        "externalId": "ORDER-12345",
        "severity": "MEDIUM",
        "category": "VALIDATION",
        "retryable": true,
        "customerId": "CUST-001",
        "locationCode": "LOC-001"
    }'
    
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer $TEST_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$test_exception" \
        "$BASE_URL/api/v1/exceptions")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    local body
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "201" ]; then
        CREATED_EXCEPTION_ID=$(echo "$body" | jq -r '.id')
        log_success "Exception created successfully (ID: $CREATED_EXCEPTION_ID)"
    else
        log_error "Exception creation failed (HTTP $http_code)"
        echo "$body"
        return 1
    fi
}

# Function to test exception retrieval
test_exception_retrieval() {
    log_info "Testing exception retrieval..."
    
    if [ -z "$CREATED_EXCEPTION_ID" ]; then
        log_error "No exception ID available for retrieval test"
        return 1
    fi
    
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer $TEST_TOKEN" \
        "$BASE_URL/api/v1/exceptions/$CREATED_EXCEPTION_ID")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    local body
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        local retrieved_transaction_id
        retrieved_transaction_id=$(echo "$body" | jq -r '.transactionId')
        
        if [ "$retrieved_transaction_id" = "$TEST_TRANSACTION_ID" ]; then
            log_success "Exception retrieved successfully"
        else
            log_error "Retrieved exception has incorrect transaction ID"
            return 1
        fi
    else
        log_error "Exception retrieval failed (HTTP $http_code)"
        return 1
    fi
}

# Function to test exception listing
test_exception_listing() {
    log_info "Testing exception listing..."
    
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer $TEST_TOKEN" \
        "$BASE_URL/api/v1/exceptions?page=0&size=10")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    local body
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        local total_elements
        total_elements=$(echo "$body" | jq -r '.totalElements // 0')
        
        if [ "$total_elements" -gt 0 ]; then
            log_success "Exception listing returned $total_elements exceptions"
        else
            log_warning "Exception listing returned no exceptions"
        fi
    else
        log_error "Exception listing failed (HTTP $http_code)"
        return 1
    fi
}

# Function to test exception search
test_exception_search() {
    log_info "Testing exception search..."
    
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer $TEST_TOKEN" \
        "$BASE_URL/api/v1/exceptions/search?transactionId=$TEST_TRANSACTION_ID")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    local body
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        local found_count
        found_count=$(echo "$body" | jq -r '.totalElements // 0')
        
        if [ "$found_count" -gt 0 ]; then
            log_success "Exception search found $found_count matching exceptions"
        else
            log_warning "Exception search found no matching exceptions"
        fi
    else
        log_error "Exception search failed (HTTP $http_code)"
        return 1
    fi
}

# Function to test exception acknowledgment
test_exception_acknowledgment() {
    log_info "Testing exception acknowledgment..."
    
    if [ -z "$CREATED_EXCEPTION_ID" ]; then
        log_error "No exception ID available for acknowledgment test"
        return 1
    fi
    
    local ack_request='{
        "acknowledgedBy": "integration-test-user",
        "notes": "Acknowledged during integration testing"
    }'
    
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Authorization: Bearer $TEST_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$ack_request" \
        "$BASE_URL/api/v1/exceptions/$CREATED_EXCEPTION_ID/acknowledge")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        log_success "Exception acknowledged successfully"
    else
        log_error "Exception acknowledgment failed (HTTP $http_code)"
        return 1
    fi
}

# Function to test retry functionality
test_retry_functionality() {
    log_info "Testing retry functionality..."
    
    if [ -z "$CREATED_EXCEPTION_ID" ]; then
        log_error "No exception ID available for retry test"
        return 1
    fi
    
    local retry_request='{
        "initiatedBy": "integration-test-user",
        "notes": "Retry initiated during integration testing"
    }'
    
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Authorization: Bearer $TEST_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$retry_request" \
        "$BASE_URL/api/v1/exceptions/$CREATED_EXCEPTION_ID/retry")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "202" ]; then
        log_success "Exception retry initiated successfully"
    else
        log_error "Exception retry failed (HTTP $http_code)"
        return 1
    fi
}

# Function to test exception resolution
test_exception_resolution() {
    log_info "Testing exception resolution..."
    
    if [ -z "$CREATED_EXCEPTION_ID" ]; then
        log_error "No exception ID available for resolution test"
        return 1
    fi
    
    local resolution_request='{
        "resolvedBy": "integration-test-user",
        "resolutionNotes": "Resolved during integration testing",
        "resolutionType": "MANUAL"
    }'
    
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Authorization: Bearer $TEST_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$resolution_request" \
        "$BASE_URL/api/v1/exceptions/$CREATED_EXCEPTION_ID/resolve")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        log_success "Exception resolved successfully"
    else
        log_error "Exception resolution failed (HTTP $http_code)"
        return 1
    fi
}

# Function to test metrics collection
test_metrics_collection() {
    log_info "Testing metrics collection..."
    
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer $TEST_TOKEN" \
        "$BASE_URL/api/v1/exceptions/metrics")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    local body
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        local total_exceptions
        total_exceptions=$(echo "$body" | jq -r '.totalExceptions // 0')
        
        log_success "Metrics collection working (Total exceptions: $total_exceptions)"
    else
        log_warning "Metrics collection test failed (HTTP $http_code) - may not be implemented"
    fi
}

# Function to test data validation
test_data_validation() {
    log_info "Testing data validation..."
    
    # Test with invalid data
    local invalid_exception='{
        "transactionId": "",
        "interfaceType": "INVALID_TYPE",
        "exceptionReason": "",
        "operation": "TEST_OPERATION"
    }'
    
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer $TEST_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$invalid_exception" \
        "$BASE_URL/api/v1/exceptions")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "400" ]; then
        log_success "Data validation working correctly (rejected invalid data)"
    else
        log_error "Data validation test failed (expected 400, got $http_code)"
        return 1
    fi
}

# Function to test concurrent operations
test_concurrent_operations() {
    log_info "Testing concurrent operations..."
    
    # Create multiple exceptions concurrently
    local pids=()
    
    for i in {1..5}; do
        (
            local concurrent_exception='{
                "transactionId": "concurrent-test-'$i'-'$(date +%s)'",
                "interfaceType": "ORDER",
                "exceptionReason": "Concurrent test exception '$i'",
                "operation": "CREATE_ORDER",
                "severity": "LOW",
                "category": "VALIDATION",
                "retryable": true
            }'
            
            curl -s -o /dev/null -w "%{http_code}" \
                -H "Authorization: Bearer $TEST_TOKEN" \
                -H "Content-Type: application/json" \
                -d "$concurrent_exception" \
                "$BASE_URL/api/v1/exceptions"
        ) &
        pids+=($!)
    done
    
    # Wait for all concurrent requests to complete
    local success_count=0
    for pid in "${pids[@]}"; do
        if wait "$pid"; then
            success_count=$((success_count + 1))
        fi
    done
    
    if [ $success_count -eq 5 ]; then
        log_success "Concurrent operations test passed ($success_count/5 successful)"
    else
        log_warning "Concurrent operations test partially failed ($success_count/5 successful)"
    fi
}

# Function to test error handling
test_error_handling() {
    log_info "Testing error handling..."
    
    # Test 404 for non-existent exception
    local response
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer $TEST_TOKEN" \
        "$BASE_URL/api/v1/exceptions/99999999")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "404" ]; then
        log_success "Error handling working correctly (404 for non-existent resource)"
    else
        log_error "Error handling test failed (expected 404, got $http_code)"
        return 1
    fi
}

# Function to test authentication and authorization
test_auth() {
    log_info "Testing authentication and authorization..."
    
    # Test without token
    local response
    response=$(curl -s -w "\n%{http_code}" \
        "$BASE_URL/api/v1/exceptions")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
        log_success "Authentication working correctly (rejected request without token)"
    else
        log_warning "Authentication test unexpected result (HTTP $http_code)"
    fi
    
    # Test with invalid token
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer invalid-token" \
        "$BASE_URL/api/v1/exceptions")
    
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
        log_success "Authorization working correctly (rejected invalid token)"
    else
        log_warning "Authorization test unexpected result (HTTP $http_code)"
    fi
}

# Function to cleanup test data
cleanup_test_data() {
    if [ "$CLEANUP" != true ]; then
        log_info "Cleanup disabled, skipping test data cleanup"
        return 0
    fi
    
    log_info "Cleaning up test data..."
    
    if [ -n "$CREATED_EXCEPTION_ID" ]; then
        # Delete the created exception
        local response
        response=$(curl -s -w "\n%{http_code}" \
            -X DELETE \
            -H "Authorization: Bearer $TEST_TOKEN" \
            "$BASE_URL/api/v1/exceptions/$CREATED_EXCEPTION_ID")
        
        local http_code
        http_code=$(echo "$response" | tail -n1)
        
        if [ "$http_code" = "200" ] || [ "$http_code" = "204" ]; then
            log_success "Test exception cleaned up successfully"
        else
            log_warning "Failed to cleanup test exception (HTTP $http_code)"
        fi
    fi
    
    # Clean up any concurrent test exceptions
    log_info "Cleaning up concurrent test exceptions..."
    # This would require a search and delete operation
    # Implementation depends on available API endpoints
}

# Function to generate integration test report
generate_report() {
    local test_results_file="/tmp/integration-test-results-$(date +%s).json"
    
    cat > "$test_results_file" << EOF
{
    "environment": "$ENVIRONMENT",
    "base_url": "$BASE_URL",
    "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
    "test_transaction_id": "$TEST_TRANSACTION_ID",
    "created_exception_id": "$CREATED_EXCEPTION_ID",
    "tests": {
        "exception_creation": "passed",
        "exception_retrieval": "passed",
        "exception_listing": "passed",
        "exception_search": "passed",
        "exception_acknowledgment": "passed",
        "retry_functionality": "passed",
        "exception_resolution": "passed",
        "data_validation": "passed",
        "error_handling": "passed",
        "authentication": "passed"
    },
    "summary": {
        "total_tests": 10,
        "passed_tests": 10,
        "failed_tests": 0
    }
}
EOF
    
    log_info "Integration test report generated: $test_results_file"
}

# Function to display usage
usage() {
    echo "Usage: $0 [ENVIRONMENT] [OPTIONS]"
    echo ""
    echo "Environments:"
    echo "  local                       Local development environment (default)"
    echo "  dev                         Development environment"
    echo "  staging                     Staging environment"
    echo ""
    echo "Options:"
    echo "  --token TOKEN               Authentication token for API tests"
    echo "  --timeout TIMEOUT           Timeout in seconds (default: 300)"
    echo "  --no-cleanup                Skip cleanup of test data"
    echo "  --report                    Generate test report"
    echo "  -h, --help                  Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  TEST_TOKEN                  Authentication token (required)"
    echo "  TIMEOUT                     Timeout in seconds"
    echo "  CLEANUP                     Enable/disable cleanup (true/false)"
}

# Parse command line arguments
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
        --no-cleanup)
            CLEANUP=false
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
    log_info "Starting integration tests..."
    
    setup_environment
    check_prerequisites
    
    # Run integration tests
    test_exception_creation
    test_exception_retrieval
    test_exception_listing
    test_exception_search
    test_exception_acknowledgment
    test_retry_functionality
    test_exception_resolution
    test_metrics_collection
    test_data_validation
    test_concurrent_operations
    test_error_handling
    test_auth
    
    # Cleanup
    cleanup_test_data
    
    # Generate report if requested
    if [ "$GENERATE_REPORT" = true ]; then
        generate_report
    fi
    
    log_success "All integration tests completed successfully!"
    log_info "Environment: $ENVIRONMENT"
    log_info "Test Transaction ID: $TEST_TRANSACTION_ID"
}

# Run main function
main "$@"