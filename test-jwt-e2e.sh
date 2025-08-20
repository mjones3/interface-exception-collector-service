#!/bin/bash

# JWT End-to-End Authentication Testing Script
# This script tests the complete JWT authentication flow

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
API_BASE="${BASE_URL}/api/v1"

# Test results tracking
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO")
            echo -e "${BLUE}[INFO]${NC} $message"
            ;;
        "SUCCESS")
            echo -e "${GREEN}[SUCCESS]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[ERROR]${NC} $message"
            ;;
        "WARNING")
            echo -e "${YELLOW}[WARNING]${NC} $message"
            ;;
    esac
}

# Function to increment test counters
record_test_result() {
    local result=$1
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if [ "$result" = "PASS" ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        print_status "SUCCESS" "Test passed"
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        print_status "ERROR" "Test failed"
    fi
}

# Function to check if service is running
check_service_health() {
    print_status "INFO" "Checking service health..."
    
    local health_response
    health_response=$(curl -s -w "%{http_code}" -o /tmp/health_response.json "${BASE_URL}/actuator/health" || echo "000")
    
    if [ "$health_response" = "200" ]; then
        print_status "SUCCESS" "Service is healthy"
        return 0
    else
        print_status "ERROR" "Service is not healthy (HTTP $health_response)"
        if [ -f /tmp/health_response.json ]; then
            cat /tmp/health_response.json
        fi
        return 1
    fi
}

# Function to generate JWT token
generate_jwt_token() {
    local username=$1
    local roles=$2
    
    print_status "INFO" "Generating JWT token for user: $username with roles: $roles"
    
    # Use the Node.js script to generate token, extract only the raw token
    local token
    token=$(node generate-jwt.js "$username" "$roles" 2>/dev/null | grep -v "🔑\|━\|👤\|🛡️\|⏰\|📋\|🔗" | grep -E "^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$" | head -1)
    
    if [ -z "$token" ]; then
        print_status "ERROR" "Failed to generate JWT token"
        return 1
    fi
    
    print_status "SUCCESS" "Generated JWT token (length: ${#token})"
    echo "$token"
}

# Function to test API endpoint with token
test_api_endpoint() {
    local method=$1
    local endpoint=$2
    local token=$3
    local expected_status=$4
    local description=$5
    
    print_status "INFO" "Testing: $description"
    print_status "INFO" "Request: $method $endpoint"
    
    local response_file="/tmp/api_response_$$.json"
    local headers_file="/tmp/api_headers_$$.txt"
    
    local actual_status
    if [ -n "$token" ]; then
        actual_status=$(curl -s -w "%{http_code}" -o "$response_file" -D "$headers_file" \
            -X "$method" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            "$endpoint" || echo "000")
    else
        actual_status=$(curl -s -w "%{http_code}" -o "$response_file" -D "$headers_file" \
            -X "$method" \
            -H "Content-Type: application/json" \
            "$endpoint" || echo "000")
    fi
    
    print_status "INFO" "Expected status: $expected_status, Actual status: $actual_status"
    
    if [ "$actual_status" = "$expected_status" ]; then
        record_test_result "PASS"
        
        # Show response for successful requests
        if [ "$actual_status" = "200" ] && [ -f "$response_file" ]; then
            local response_size
            response_size=$(wc -c < "$response_file")
            print_status "INFO" "Response size: $response_size bytes"
            
            # Show first few lines of response for verification
            if [ "$response_size" -gt 0 ]; then
                print_status "INFO" "Response preview:"
                head -c 200 "$response_file" | jq . 2>/dev/null || head -c 200 "$response_file"
                echo ""
            fi
        fi
    else
        record_test_result "FAIL"
        
        # Show error response
        if [ -f "$response_file" ]; then
            print_status "ERROR" "Error response:"
            cat "$response_file" | jq . 2>/dev/null || cat "$response_file"
            echo ""
        fi
    fi
    
    # Cleanup
    rm -f "$response_file" "$headers_file"
    echo ""
}

# Function to test token generation and validation
test_token_generation() {
    print_status "INFO" "=== Testing Token Generation ==="
    
    # Clean any existing token file
    rm -f /tmp/jwt_tokens.env
    
    # Test 1: Generate token with ADMIN role
    local admin_token
    admin_token=$(generate_jwt_token "admin-user" "ADMIN")
    if [ $? -eq 0 ] && [ -n "$admin_token" ]; then
        record_test_result "PASS"
        echo "ADMIN_TOKEN=\"$admin_token\"" > /tmp/jwt_tokens.env
    else
        record_test_result "FAIL"
        return 1
    fi
    
    # Test 2: Generate token with OPERATIONS role
    local ops_token
    ops_token=$(generate_jwt_token "ops-user" "OPERATIONS")
    if [ $? -eq 0 ] && [ -n "$ops_token" ]; then
        record_test_result "PASS"
        echo "OPS_TOKEN=\"$ops_token\"" >> /tmp/jwt_tokens.env
    else
        record_test_result "FAIL"
        return 1
    fi
    
    # Test 3: Generate token with VIEWER role
    local viewer_token
    viewer_token=$(generate_jwt_token "viewer-user" "VIEWER")
    if [ $? -eq 0 ] && [ -n "$viewer_token" ]; then
        record_test_result "PASS"
        echo "VIEWER_TOKEN=\"$viewer_token\"" >> /tmp/jwt_tokens.env
    else
        record_test_result "FAIL"
        return 1
    fi
    
    # Test 4: Generate token with multiple roles
    local multi_role_token
    multi_role_token=$(generate_jwt_token "multi-user" "ADMIN,OPERATIONS")
    if [ $? -eq 0 ] && [ -n "$multi_role_token" ]; then
        record_test_result "PASS"
        echo "MULTI_ROLE_TOKEN=\"$multi_role_token\"" >> /tmp/jwt_tokens.env
    else
        record_test_result "FAIL"
        return 1
    fi
    
    print_status "SUCCESS" "Token generation tests completed"
    echo ""
}

# Function to test public endpoints (no authentication required)
test_public_endpoints() {
    print_status "INFO" "=== Testing Public Endpoints ==="
    
    # Test health endpoint
    test_api_endpoint "GET" "${BASE_URL}/actuator/health" "" "200" "Health check endpoint"
    
    # Test info endpoint
    test_api_endpoint "GET" "${BASE_URL}/actuator/info" "" "200" "Info endpoint"
    
    print_status "SUCCESS" "Public endpoints tests completed"
    echo ""
}

# Function to test protected endpoints with valid tokens
test_protected_endpoints_valid_tokens() {
    print_status "INFO" "=== Testing Protected Endpoints with Valid Tokens ==="
    
    # Load tokens
    source /tmp/jwt_tokens.env
    
    # Test 1: GET /api/v1/exceptions with ADMIN token
    test_api_endpoint "GET" "${API_BASE}/exceptions" "$ADMIN_TOKEN" "200" "List exceptions with ADMIN token"
    
    # Test 2: GET /api/v1/exceptions with OPERATIONS token
    test_api_endpoint "GET" "${API_BASE}/exceptions" "$OPS_TOKEN" "200" "List exceptions with OPERATIONS token"
    
    # Test 3: GET /api/v1/exceptions with VIEWER token
    test_api_endpoint "GET" "${API_BASE}/exceptions" "$VIEWER_TOKEN" "200" "List exceptions with VIEWER token"
    
    # Test 4: GET /api/v1/exceptions/summary with ADMIN token
    test_api_endpoint "GET" "${API_BASE}/exceptions/summary" "$ADMIN_TOKEN" "200" "Get exception summary with ADMIN token"
    
    # Test 5: GET /api/v1/exceptions/search with OPERATIONS token
    test_api_endpoint "GET" "${API_BASE}/exceptions/search?query=test" "$OPS_TOKEN" "200" "Search exceptions with OPERATIONS token"
    
    # Test 6: Admin-only actuator endpoint with ADMIN token
    test_api_endpoint "GET" "${BASE_URL}/actuator/metrics" "$ADMIN_TOKEN" "200" "Access admin actuator endpoint with ADMIN token"
    
    print_status "SUCCESS" "Protected endpoints with valid tokens tests completed"
    echo ""
}

# Function to test protected endpoints with invalid/missing tokens
test_protected_endpoints_invalid_tokens() {
    print_status "INFO" "=== Testing Protected Endpoints with Invalid/Missing Tokens ==="
    
    # Test 1: No token
    test_api_endpoint "GET" "${API_BASE}/exceptions" "" "401" "List exceptions without token"
    
    # Test 2: Invalid token format
    test_api_endpoint "GET" "${API_BASE}/exceptions" "invalid-token" "401" "List exceptions with invalid token format"
    
    # Test 3: Malformed JWT token
    test_api_endpoint "GET" "${API_BASE}/exceptions" "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature" "401" "List exceptions with malformed token"
    
    # Test 4: Expired token (simulate by using a token with past expiration)
    local expired_token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE2OTIzMDcyMDAsImV4cCI6MTY5MjMwNzIwMX0.invalid"
    test_api_endpoint "GET" "${API_BASE}/exceptions" "$expired_token" "401" "List exceptions with expired token"
    
    # Test 5: Admin endpoint with non-admin token
    source /tmp/jwt_tokens.env
    test_api_endpoint "GET" "${BASE_URL}/actuator/metrics" "$VIEWER_TOKEN" "403" "Access admin endpoint with VIEWER token"
    
    print_status "SUCCESS" "Protected endpoints with invalid tokens tests completed"
    echo ""
}

# Function to test role-based access control
test_role_based_access() {
    print_status "INFO" "=== Testing Role-Based Access Control ==="
    
    source /tmp/jwt_tokens.env
    
    # Test 1: VIEWER can read but not modify
    test_api_endpoint "GET" "${API_BASE}/exceptions" "$VIEWER_TOKEN" "200" "VIEWER can read exceptions"
    
    # Test 2: OPERATIONS can read and modify
    test_api_endpoint "GET" "${API_BASE}/exceptions" "$OPS_TOKEN" "200" "OPERATIONS can read exceptions"
    
    # Test 3: ADMIN can access everything
    test_api_endpoint "GET" "${API_BASE}/exceptions" "$ADMIN_TOKEN" "200" "ADMIN can read exceptions"
    test_api_endpoint "GET" "${BASE_URL}/actuator/metrics" "$ADMIN_TOKEN" "200" "ADMIN can access admin endpoints"
    
    # Test 4: Multi-role token works
    test_api_endpoint "GET" "${API_BASE}/exceptions" "$MULTI_ROLE_TOKEN" "200" "Multi-role token can read exceptions"
    test_api_endpoint "GET" "${BASE_URL}/actuator/metrics" "$MULTI_ROLE_TOKEN" "200" "Multi-role token can access admin endpoints"
    
    print_status "SUCCESS" "Role-based access control tests completed"
    echo ""
}

# Function to test error scenarios and edge cases
test_error_scenarios() {
    print_status "INFO" "=== Testing Error Scenarios and Edge Cases ==="
    
    # Test 1: Very long token
    local long_token
    long_token=$(printf 'A%.0s' {1..5000})
    test_api_endpoint "GET" "${API_BASE}/exceptions" "$long_token" "401" "Very long invalid token"
    
    # Test 2: Empty Authorization header
    local response_file="/tmp/empty_auth_response.json"
    local actual_status
    actual_status=$(curl -s -w "%{http_code}" -o "$response_file" \
        -X "GET" \
        -H "Authorization: " \
        -H "Content-Type: application/json" \
        "${API_BASE}/exceptions" || echo "000")
    
    if [ "$actual_status" = "401" ]; then
        record_test_result "PASS"
        print_status "SUCCESS" "Empty Authorization header correctly rejected"
    else
        record_test_result "FAIL"
        print_status "ERROR" "Empty Authorization header test failed (status: $actual_status)"
    fi
    rm -f "$response_file"
    
    # Test 3: Authorization header without Bearer prefix
    actual_status=$(curl -s -w "%{http_code}" -o "$response_file" \
        -X "GET" \
        -H "Authorization: Token some-token" \
        -H "Content-Type: application/json" \
        "${API_BASE}/exceptions" || echo "000")
    
    if [ "$actual_status" = "401" ]; then
        record_test_result "PASS"
        print_status "SUCCESS" "Non-Bearer Authorization header correctly rejected"
    else
        record_test_result "FAIL"
        print_status "ERROR" "Non-Bearer Authorization header test failed (status: $actual_status)"
    fi
    rm -f "$response_file"
    
    print_status "SUCCESS" "Error scenarios tests completed"
    echo ""
}

# Function to test token consistency between generation and validation
test_token_consistency() {
    print_status "INFO" "=== Testing Token Consistency ==="
    
    # Test 1: Generate token with Node.js script and validate with API
    local test_token
    test_token=$(generate_jwt_token "consistency-test" "ADMIN")
    
    if [ -n "$test_token" ]; then
        test_api_endpoint "GET" "${API_BASE}/exceptions" "$test_token" "200" "Node.js generated token validation"
    else
        record_test_result "FAIL"
        print_status "ERROR" "Failed to generate token for consistency test"
    fi
    
    # Test 2: Generate token with Python script and validate with API
    local python_token
    python_token=$(python3 generate-jwt.py "python-test" "ADMIN" 2>/dev/null | grep -v "🔑\|━\|👤\|🛡️\|⏰\|📋\|🔗" | grep -E "^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$" | head -1)
    
    if [ -n "$python_token" ]; then
        test_api_endpoint "GET" "${API_BASE}/exceptions" "$python_token" "200" "Python generated token validation"
    else
        record_test_result "FAIL"
        print_status "ERROR" "Failed to generate token with Python script"
    fi
    
    print_status "SUCCESS" "Token consistency tests completed"
    echo ""
}

# Function to print test summary
print_test_summary() {
    echo ""
    print_status "INFO" "=== Test Summary ==="
    echo -e "${BLUE}Total Tests:${NC} $TOTAL_TESTS"
    echo -e "${GREEN}Passed:${NC} $PASSED_TESTS"
    echo -e "${RED}Failed:${NC} $FAILED_TESTS"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        print_status "SUCCESS" "All tests passed! 🎉"
        echo ""
        return 0
    else
        print_status "ERROR" "Some tests failed! ❌"
        echo ""
        return 1
    fi
}

# Main execution
main() {
    print_status "INFO" "Starting JWT End-to-End Authentication Tests"
    print_status "INFO" "Base URL: $BASE_URL"
    echo ""
    
    # Check if service is running
    if ! check_service_health; then
        print_status "ERROR" "Service is not running. Please start the application first."
        exit 1
    fi
    echo ""
    
    # Run all test suites
    test_token_generation
    test_public_endpoints
    test_protected_endpoints_valid_tokens
    test_protected_endpoints_invalid_tokens
    test_role_based_access
    test_error_scenarios
    test_token_consistency
    
    # Print summary and exit with appropriate code
    if print_test_summary; then
        exit 0
    else
        exit 1
    fi
}

# Cleanup function
cleanup() {
    rm -f /tmp/jwt_tokens.env
    rm -f /tmp/health_response.json
    rm -f /tmp/api_response_*.json
    rm -f /tmp/api_headers_*.txt
    rm -f /tmp/empty_auth_response.json
}

# Set up cleanup on exit
trap cleanup EXIT

# Run main function
main "$@"