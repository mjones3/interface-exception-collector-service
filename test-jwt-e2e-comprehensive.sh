#!/bin/bash

# JWT End-to-End Authentication Testing Script
# Tests the complete JWT authentication flow with all API endpoints

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
ACTUATOR_BASE="${BASE_URL}/actuator"

# Test results tracking
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "PASS")
            echo -e "${GREEN}✓ PASS${NC}: $message"
            ((PASSED_TESTS++))
            ;;
        "FAIL")
            echo -e "${RED}✗ FAIL${NC}: $message"
            ((FAILED_TESTS++))
            ;;
        "INFO")
            echo -e "${BLUE}ℹ INFO${NC}: $message"
            ;;
        "WARN")
            echo -e "${YELLOW}⚠ WARN${NC}: $message"
            ;;
    esac
    ((TOTAL_TESTS++))
}

# Function to test HTTP response
test_http_response() {
    local description=$1
    local expected_status=$2
    local actual_status=$3
    local response_body=$4
    
    if [ "$actual_status" = "$expected_status" ]; then
        print_status "PASS" "$description (HTTP $actual_status)"
        if [ -n "$response_body" ] && [ "$response_body" != "null" ]; then
            echo "    Response: $(echo "$response_body" | head -c 200)..."
        fi
    else
        print_status "FAIL" "$description (Expected: $expected_status, Got: $actual_status)"
        if [ -n "$response_body" ]; then
            echo "    Response: $response_body"
        fi
    fi
}

# Function to generate JWT token
generate_jwt_token() {
    local username=${1:-"test-user"}
    local roles=${2:-"ADMIN"}
    
    echo "🔑 Generating JWT token for user: $username, roles: $roles"
    
    # Use correct Node.js script to generate token with actual service secret
    if command -v node >/dev/null 2>&1; then
        local token=$(node generate-jwt-correct-secret.js "$username" "$roles" 2>/dev/null | tail -1)
        echo "$token"
    else
        # Fallback to Python script
        if command -v python3 >/dev/null 2>&1; then
            local token=$(python3 generate-jwt.py "$username" "$roles" 2>/dev/null | tail -1)
            echo "$token"
        else
            echo "ERROR: Neither Node.js nor Python3 available for token generation"
            exit 1
        fi
    fi
}

# Function to make authenticated API call
make_api_call() {
    local method=$1
    local endpoint=$2
    local token=$3
    local data=$4
    local description=$5
    
    local curl_cmd="curl -s -w '%{http_code}' -X $method"
    
    if [ -n "$token" ]; then
        curl_cmd="$curl_cmd -H 'Authorization: Bearer $token'"
    fi
    
    curl_cmd="$curl_cmd -H 'Content-Type: application/json'"
    
    if [ -n "$data" ]; then
        curl_cmd="$curl_cmd -d '$data'"
    fi
    
    curl_cmd="$curl_cmd '$endpoint'"
    
    echo "📡 Making API call: $description"
    echo "    $method $endpoint"
    
    local response=$(eval $curl_cmd)
    local http_code="${response: -3}"
    local response_body="${response%???}"
    
    # Debug output for troubleshooting
    if [ "$endpoint" = "$ACTUATOR_BASE/health" ]; then
        echo "Debug - Full response: '$response'" >&2
        echo "Debug - HTTP code: '$http_code'" >&2
        echo "Debug - Response body: '$response_body'" >&2
    fi
    
    echo "$http_code|$response_body"
}

# Function to test token validation
test_token_validation() {
    local token=$1
    local expected_valid=$2
    local description=$3
    
    echo "🔍 Testing token validation: $description"
    
    # Test with /api/v1/exceptions endpoint
    local result=$(make_api_call "GET" "$API_BASE/exceptions" "$token" "" "Token validation test")
    local http_code=$(echo "$result" | cut -d'|' -f1)
    local response_body=$(echo "$result" | cut -d'|' -f2)
    
    if [ "$expected_valid" = "true" ]; then
        if [ "$http_code" = "200" ] || [ "$http_code" = "404" ]; then
            print_status "PASS" "$description - Token accepted"
        else
            print_status "FAIL" "$description - Token rejected (HTTP $http_code)"
            echo "    Response: $response_body"
        fi
    else
        if [ "$http_code" = "401" ]; then
            print_status "PASS" "$description - Token correctly rejected"
        else
            print_status "FAIL" "$description - Token should be rejected (HTTP $http_code)"
            echo "    Response: $response_body"
        fi
    fi
}

# Function to test role-based access
test_role_access() {
    local token=$1
    local role=$2
    local endpoint=$3
    local method=$4
    local expected_status=$5
    local description=$6
    
    echo "🛡️ Testing role-based access: $description"
    
    local result=$(make_api_call "$method" "$endpoint" "$token" "" "Role access test")
    local http_code=$(echo "$result" | cut -d'|' -f1)
    local response_body=$(echo "$result" | cut -d'|' -f2)
    
    test_http_response "$description" "$expected_status" "$http_code" "$response_body"
}

# Function to test API endpoints
test_api_endpoints() {
    local token=$1
    local role=$2
    
    echo "🌐 Testing API endpoints with role: $role"
    
    # Test GET /api/v1/exceptions (should work for all roles)
    test_role_access "$token" "$role" "$API_BASE/exceptions" "GET" "200" "List exceptions with $role role"
    
    # Test GET /api/v1/exceptions/search (should work for all roles)
    test_role_access "$token" "$role" "$API_BASE/exceptions/search?query=test" "GET" "200" "Search exceptions with $role role"
    
    # Test GET /api/v1/exceptions/summary (should work for all roles)
    test_role_access "$token" "$role" "$API_BASE/exceptions/summary" "GET" "200" "Get exception summary with $role role"
    
    # Test POST /api/v1/exceptions (should work for OPERATOR and ADMIN)
    local create_data='{"externalId":"test-123","operation":"CREATE","rejectedReason":"Test reason","customerId":"CUST001","locationCode":"LOC001"}'
    if [ "$role" = "ADMIN" ] || [ "$role" = "OPERATOR" ]; then
        test_role_access "$token" "$role" "$API_BASE/exceptions" "POST" "201" "Create exception with $role role"
    else
        test_role_access "$token" "$role" "$API_BASE/exceptions" "POST" "403" "Create exception with $role role (should be forbidden)"
    fi
    
    # Test actuator endpoints (should work only for ADMIN)
    if [ "$role" = "ADMIN" ]; then
        test_role_access "$token" "$role" "$ACTUATOR_BASE/metrics" "GET" "200" "Access actuator metrics with $role role"
    else
        test_role_access "$token" "$role" "$ACTUATOR_BASE/metrics" "GET" "403" "Access actuator metrics with $role role (should be forbidden)"
    fi
}

# Function to test error scenarios
test_error_scenarios() {
    echo "❌ Testing error scenarios"
    
    # Test with no token
    local result=$(make_api_call "GET" "$API_BASE/exceptions" "" "" "No token test")
    local http_code=$(echo "$result" | cut -d'|' -f1)
    test_http_response "Access protected endpoint without token" "401" "$http_code"
    
    # Test with invalid token
    test_token_validation "invalid.token.here" "false" "Invalid token format"
    
    # Test with malformed token
    test_token_validation "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature" "false" "Malformed token"
    
    # Test with expired token (generate token with past expiration)
    echo "⏰ Testing expired token..."
    local expired_token=$(node -e "
        const crypto = require('crypto');
        const secret = 'mySecretKey1234567890123456789012345678901234567890';
        const header = Buffer.from(JSON.stringify({alg:'HS256',typ:'JWT'})).toString('base64url');
        const payload = Buffer.from(JSON.stringify({
            sub:'test-user',
            roles:['ADMIN'],
            iat:Math.floor(Date.now()/1000)-3600,
            exp:Math.floor(Date.now()/1000)-1800
        })).toString('base64url');
        const data = header + '.' + payload;
        const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');
        console.log(data + '.' + signature);
    ")
    test_token_validation "$expired_token" "false" "Expired token"
}

# Function to test performance
test_performance() {
    local token=$1
    
    echo "⚡ Testing JWT validation performance"
    
    local start_time=$(date +%s%N)
    local iterations=10
    
    for i in $(seq 1 $iterations); do
        make_api_call "GET" "$API_BASE/exceptions" "$token" "" "Performance test $i" >/dev/null
    done
    
    local end_time=$(date +%s%N)
    local duration=$(( (end_time - start_time) / 1000000 )) # Convert to milliseconds
    local avg_time=$(( duration / iterations ))
    
    if [ $avg_time -lt 1000 ]; then
        print_status "PASS" "JWT validation performance: ${avg_time}ms average (< 1000ms)"
    else
        print_status "FAIL" "JWT validation performance: ${avg_time}ms average (>= 1000ms)"
    fi
}

# Function to test concurrent authentication
test_concurrent_authentication() {
    local token=$1
    
    echo "🔄 Testing concurrent authentication scenarios"
    
    # Create multiple background requests
    local pids=()
    local temp_dir=$(mktemp -d)
    
    for i in $(seq 1 5); do
        (
            local result=$(make_api_call "GET" "$API_BASE/exceptions" "$token" "" "Concurrent test $i")
            local http_code=$(echo "$result" | cut -d'|' -f1)
            echo "$http_code" > "$temp_dir/result_$i"
        ) &
        pids+=($!)
    done
    
    # Wait for all background jobs
    for pid in "${pids[@]}"; do
        wait $pid
    done
    
    # Check results
    local success_count=0
    for i in $(seq 1 5); do
        local http_code=$(cat "$temp_dir/result_$i")
        if [ "$http_code" = "200" ]; then
            ((success_count++))
        fi
    done
    
    # Cleanup
    rm -rf "$temp_dir"
    
    if [ $success_count -eq 5 ]; then
        print_status "PASS" "Concurrent authentication: $success_count/5 requests succeeded"
    else
        print_status "FAIL" "Concurrent authentication: $success_count/5 requests succeeded"
    fi
}

# Function to check if service is running
check_service_health() {
    echo "🏥 Checking service health..."
    
    local result=$(make_api_call "GET" "$ACTUATOR_BASE/health" "" "" "Health check")
    local http_code=$(echo "$result" | cut -d'|' -f1)
    local response_body=$(echo "$result" | cut -d'|' -f2)
    
    if [ "$http_code" = "200" ]; then
        print_status "PASS" "Service is healthy"
        echo "    Health status: $(echo "$response_body" | grep -o '"status":"[^"]*"' || echo "Unknown")"
    else
        print_status "FAIL" "Service health check failed (HTTP $http_code)"
        echo "    Response: $response_body"
        echo "❌ Service is not running or not healthy. Please start the service first."
        exit 1
    fi
}

# Main test execution
main() {
    echo "🚀 Starting JWT End-to-End Authentication Testing"
    echo "=================================================="
    echo "Base URL: $BASE_URL"
    echo "Test started at: $(date)"
    echo ""
    
    # Check service health first
    check_service_health
    echo ""
    
    # Test 1: Generate and validate tokens for different roles
    echo "📋 Test 1: Token Generation and Validation"
    echo "----------------------------------------"
    
    # Test ADMIN role
    local admin_token=$(generate_jwt_token "admin-user" "ADMIN")
    echo "Admin token: ${admin_token:0:50}..."
    test_token_validation "$admin_token" "true" "Valid ADMIN token"
    echo ""
    
    # Test OPERATOR role
    local ops_token=$(generate_jwt_token "ops-user" "OPERATOR")
    echo "Operator token: ${ops_token:0:50}..."
    test_token_validation "$ops_token" "true" "Valid OPERATOR token"
    echo ""
    
    # Test VIEWER role
    local viewer_token=$(generate_jwt_token "viewer-user" "VIEWER")
    echo "Viewer token: ${viewer_token:0:50}..."
    test_token_validation "$viewer_token" "true" "Valid VIEWER token"
    echo ""
    
    # Test 2: Role-based access control
    echo "📋 Test 2: Role-Based Access Control"
    echo "-----------------------------------"
    test_api_endpoints "$admin_token" "ADMIN"
    echo ""
    test_api_endpoints "$ops_token" "OPERATOR"
    echo ""
    test_api_endpoints "$viewer_token" "VIEWER"
    echo ""
    
    # Test 3: Error scenarios
    echo "📋 Test 3: Error Scenarios and Edge Cases"
    echo "----------------------------------------"
    test_error_scenarios
    echo ""
    
    # Test 4: Performance testing
    echo "📋 Test 4: Performance Validation"
    echo "--------------------------------"
    test_performance "$admin_token"
    echo ""
    
    # Test 5: Concurrent authentication
    echo "📋 Test 5: Concurrent Authentication"
    echo "-----------------------------------"
    test_concurrent_authentication "$admin_token"
    echo ""
    
    # Test 6: Token expiration enforcement
    echo "📋 Test 6: Token Expiration Enforcement"
    echo "--------------------------------------"
    echo "⏰ Testing token expiration handling..."
    
    # Generate a token with very short expiration (1 second)
    local short_token=$(node -e "
        const crypto = require('crypto');
        const secret = 'mySecretKey1234567890123456789012345678901234567890';
        const header = Buffer.from(JSON.stringify({alg:'HS256',typ:'JWT'})).toString('base64url');
        const payload = Buffer.from(JSON.stringify({
            sub:'test-user',
            roles:['ADMIN'],
            iat:Math.floor(Date.now()/1000),
            exp:Math.floor(Date.now()/1000)+1
        })).toString('base64url');
        const data = header + '.' + payload;
        const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');
        console.log(data + '.' + signature);
    ")
    
    # Test immediately (should work)
    test_token_validation "$short_token" "true" "Short-lived token (immediate use)"
    
    # Wait for expiration and test again
    echo "    Waiting 2 seconds for token expiration..."
    sleep 2
    test_token_validation "$short_token" "false" "Short-lived token (after expiration)"
    echo ""
    
    # Final summary
    echo "📊 Test Summary"
    echo "==============="
    echo "Total tests: $TOTAL_TESTS"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}🎉 All tests passed! JWT authentication is working correctly.${NC}"
        exit 0
    else
        echo -e "\n${RED}❌ Some tests failed. Please review the output above.${NC}"
        exit 1
    fi
}

# Run main function
main "$@"