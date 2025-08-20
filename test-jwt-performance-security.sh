#!/bin/bash

# JWT Performance and Security Validation Test
# Tests JWT validation performance under load and validates security aspects

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

# Function to make API call and measure time
make_timed_api_call() {
    local token=$1
    local endpoint=$2
    
    local start_time=$(date +%s%N)
    local response=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $token" -H "Content-Type: application/json" "$endpoint")
    local end_time=$(date +%s%N)
    
    local duration=$(( (end_time - start_time) / 1000000 )) # Convert to milliseconds
    local http_code="${response: -3}"
    
    echo "$http_code|$duration"
}

# Function to test JWT validation performance
test_jwt_performance() {
    echo "⚡ Testing JWT Validation Performance"
    echo "===================================="
    
    # Generate a valid token
    local token=$(node generate-jwt-fixed.js "perf-user" "ADMIN" 2>/dev/null | tail -1)
    
    echo "🔍 Running performance tests..."
    
    # Test single request performance
    local result=$(make_timed_api_call "$token" "$API_BASE/exceptions")
    local http_code=$(echo "$result" | cut -d'|' -f1)
    local duration=$(echo "$result" | cut -d'|' -f2)
    
    if [ "$http_code" = "200" ]; then
        if [ $duration -lt 1000 ]; then
            print_status "PASS" "Single JWT validation performance: ${duration}ms (< 1000ms)"
        else
            print_status "FAIL" "Single JWT validation performance: ${duration}ms (>= 1000ms)"
        fi
    else
        print_status "FAIL" "JWT validation failed (HTTP $http_code)"
        return
    fi
    
    # Test multiple requests performance
    echo "🔄 Testing multiple requests performance..."
    local total_time=0
    local successful_requests=0
    local iterations=20
    
    for i in $(seq 1 $iterations); do
        local result=$(make_timed_api_call "$token" "$API_BASE/exceptions")
        local http_code=$(echo "$result" | cut -d'|' -f1)
        local duration=$(echo "$result" | cut -d'|' -f2)
        
        if [ "$http_code" = "200" ]; then
            total_time=$((total_time + duration))
            ((successful_requests++))
        fi
    done
    
    if [ $successful_requests -eq $iterations ]; then
        local avg_time=$((total_time / iterations))
        if [ $avg_time -lt 500 ]; then
            print_status "PASS" "Average JWT validation performance: ${avg_time}ms over $iterations requests (< 500ms)"
        else
            print_status "FAIL" "Average JWT validation performance: ${avg_time}ms over $iterations requests (>= 500ms)"
        fi
    else
        print_status "FAIL" "Only $successful_requests/$iterations requests succeeded"
    fi
}

# Function to test concurrent authentication
test_concurrent_authentication() {
    echo ""
    echo "🔄 Testing Concurrent Authentication"
    echo "==================================="
    
    local token=$(node generate-jwt-fixed.js "concurrent-user" "ADMIN" 2>/dev/null | tail -1)
    local concurrent_requests=10
    local temp_dir=$(mktemp -d)
    local pids=()
    
    echo "🚀 Launching $concurrent_requests concurrent requests..."
    
    # Launch concurrent requests
    for i in $(seq 1 $concurrent_requests); do
        (
            local result=$(make_timed_api_call "$token" "$API_BASE/exceptions")
            local http_code=$(echo "$result" | cut -d'|' -f1)
            local duration=$(echo "$result" | cut -d'|' -f2)
            echo "$http_code|$duration" > "$temp_dir/result_$i"
        ) &
        pids+=($!)
    done
    
    # Wait for all requests to complete
    for pid in "${pids[@]}"; do
        wait $pid
    done
    
    # Analyze results
    local successful_requests=0
    local total_time=0
    local max_time=0
    
    for i in $(seq 1 $concurrent_requests); do
        local result=$(cat "$temp_dir/result_$i")
        local http_code=$(echo "$result" | cut -d'|' -f1)
        local duration=$(echo "$result" | cut -d'|' -f2)
        
        if [ "$http_code" = "200" ]; then
            ((successful_requests++))
            total_time=$((total_time + duration))
            if [ $duration -gt $max_time ]; then
                max_time=$duration
            fi
        fi
    done
    
    # Cleanup
    rm -rf "$temp_dir"
    
    if [ $successful_requests -eq $concurrent_requests ]; then
        local avg_time=$((total_time / concurrent_requests))
        print_status "PASS" "Concurrent authentication: $successful_requests/$concurrent_requests requests succeeded"
        print_status "INFO" "Average response time: ${avg_time}ms, Max response time: ${max_time}ms"
        
        if [ $max_time -lt 2000 ]; then
            print_status "PASS" "Maximum concurrent response time: ${max_time}ms (< 2000ms)"
        else
            print_status "FAIL" "Maximum concurrent response time: ${max_time}ms (>= 2000ms)"
        fi
    else
        print_status "FAIL" "Concurrent authentication: only $successful_requests/$concurrent_requests requests succeeded"
    fi
}

# Function to test security aspects
test_security_validation() {
    echo ""
    echo "🛡️ Testing Security Validation"
    echo "=============================="
    
    # Test 1: Token expiration enforcement
    echo "⏰ Testing token expiration enforcement..."
    
    # Generate a token with very short expiration (1 second)
    local short_token=$(node -e "
        const crypto = require('crypto');
        const secret = 'mySecretKey1234567890123456789012345678901234567890';
        const header = Buffer.from(JSON.stringify({alg:'HS256',typ:'JWT'})).toString('base64url');
        const now = Math.floor(Date.now()/1000);
        const payload = Buffer.from(JSON.stringify({
            sub:'test-user',
            roles:['ADMIN'],
            iat:now,
            exp:now+1
        })).toString('base64url');
        const data = header + '.' + payload;
        const signature = crypto.createHmac('sha256', secret).update(data).digest('base64url');
        console.log(data + '.' + signature);
    ")
    
    # Test immediately (should work)
    local result=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $short_token" -H "Content-Type: application/json" "$API_BASE/exceptions")
    local http_code="${result: -3}"
    
    if [ "$http_code" = "200" ]; then
        print_status "PASS" "Short-lived token accepted when valid"
        
        # Wait for expiration and test again
        echo "    Waiting 2 seconds for token expiration..."
        sleep 2
        
        local expired_result=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $short_token" -H "Content-Type: application/json" "$API_BASE/exceptions")
        local expired_code="${expired_result: -3}"
        
        if [ "$expired_code" = "401" ] || [ "$expired_code" = "403" ]; then
            print_status "PASS" "Expired token correctly rejected (HTTP $expired_code)"
        else
            print_status "FAIL" "Expired token should be rejected (HTTP $expired_code)"
        fi
    else
        print_status "FAIL" "Short-lived token should be accepted initially (HTTP $http_code)"
    fi
    
    # Test 2: Invalid signature detection
    echo "🔐 Testing invalid signature detection..."
    
    local valid_token=$(node generate-jwt-fixed.js "test-user" "ADMIN" 2>/dev/null | tail -1)
    local invalid_token="${valid_token%.*}.invalid_signature"
    
    local invalid_result=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $invalid_token" -H "Content-Type: application/json" "$API_BASE/exceptions")
    local invalid_code="${invalid_result: -3}"
    
    if [ "$invalid_code" = "401" ] || [ "$invalid_code" = "403" ]; then
        print_status "PASS" "Invalid signature correctly rejected (HTTP $invalid_code)"
    else
        print_status "FAIL" "Invalid signature should be rejected (HTTP $invalid_code)"
    fi
    
    # Test 3: Malformed token detection
    echo "🚫 Testing malformed token detection..."
    
    local malformed_tokens=(
        "not.a.jwt"
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid"
        "invalid_header.eyJzdWIiOiJ0ZXN0In0.signature"
        ""
    )
    
    local malformed_passed=0
    for malformed_token in "${malformed_tokens[@]}"; do
        local malformed_result=$(curl -s -w '%{http_code}' -H "Authorization: Bearer $malformed_token" -H "Content-Type: application/json" "$API_BASE/exceptions")
        local malformed_code="${malformed_result: -3}"
        
        if [ "$malformed_code" = "401" ] || [ "$malformed_code" = "403" ]; then
            ((malformed_passed++))
        fi
    done
    
    if [ $malformed_passed -eq ${#malformed_tokens[@]} ]; then
        print_status "PASS" "All malformed tokens correctly rejected"
    else
        print_status "FAIL" "Only $malformed_passed/${#malformed_tokens[@]} malformed tokens were rejected"
    fi
    
    # Test 4: No data leakage in error responses
    echo "🔒 Testing no data leakage in error responses..."
    
    local error_result=$(curl -s -H "Authorization: Bearer invalid_token" -H "Content-Type: application/json" "$API_BASE/exceptions")
    
    # Check that error response doesn't contain sensitive information
    if echo "$error_result" | grep -qi "secret\|key\|password\|token"; then
        print_status "FAIL" "Error response may contain sensitive information"
        echo "    Response: $error_result"
    else
        print_status "PASS" "Error response does not leak sensitive information"
    fi
}

# Main execution
main() {
    echo "🚀 JWT Performance and Security Validation"
    echo "==========================================="
    echo "Base URL: $BASE_URL"
    echo "Test started at: $(date)"
    echo ""
    
    # Check if service is running
    local health_check=$(curl -s -w '%{http_code}' "$BASE_URL/actuator/health")
    local health_code="${health_check: -3}"
    
    if [ "$health_code" != "200" ]; then
        echo "❌ Service is not running or not healthy (HTTP $health_code)"
        exit 1
    fi
    
    print_status "INFO" "Service is healthy and ready for testing"
    echo ""
    
    # Run performance tests
    test_jwt_performance
    
    # Run concurrent authentication tests
    test_concurrent_authentication
    
    # Run security validation tests
    test_security_validation
    
    # Final summary
    echo ""
    echo "📊 Test Summary"
    echo "==============="
    echo "Total tests: $TOTAL_TESTS"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}🎉 All performance and security tests passed!${NC}"
        exit 0
    else
        echo -e "\n${RED}❌ Some tests failed. Please review the output above.${NC}"
        exit 1
    fi
}

# Run main function
main "$@"