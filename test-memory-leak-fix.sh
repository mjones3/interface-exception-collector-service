#!/bin/bash

echo "=== Testing Lettuce Memory Leak Fixes ==="

# Function to test with different configurations
test_configuration() {
    local config_name=$1
    local profile=$2
    
    echo ""
    echo "Testing configuration: $config_name"
    echo "Profile: $profile"
    echo "----------------------------------------"
    
    # Start application
    echo "Starting application..."
    mvn spring-boot:run -f interface-exception-collector/pom.xml \
        -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=$profile" \
        -Dspring-boot.run.arguments="--server.port=8081" \
        > "test-$config_name.log" 2>&1 &
    
    APP_PID=$!
    echo "Application PID: $APP_PID"
    
    # Wait for startup
    echo "Waiting for application to start..."
    sleep 15
    
    # Check if app started successfully
    if ! kill -0 $APP_PID 2>/dev/null; then
        echo "❌ Application failed to start"
        echo "Startup logs:"
        tail -20 "test-$config_name.log"
        return 1
    fi
    
    echo "✅ Application started successfully"
    
    # Stop application
    echo "Stopping application..."
    kill -TERM $APP_PID
    
    # Wait for shutdown
    echo "Waiting for shutdown..."
    wait $APP_PID 2>/dev/null
    
    # Check for memory leak warnings
    echo "Checking for memory leak warnings..."
    if grep -q "appears to have started a thread named \[lettuce-" "test-$config_name.log"; then
        echo "❌ MEMORY LEAK WARNINGS FOUND:"
        grep "appears to have started a thread named \[lettuce-" "test-$config_name.log"
        echo ""
        echo "Shutdown logs:"
        grep -A 10 -B 5 "Stopping service\|shutdown\|cleanup" "test-$config_name.log" | tail -20
        return 1
    else
        echo "✅ NO MEMORY LEAK WARNINGS FOUND!"
        echo ""
        echo "Shutdown logs:"
        grep -A 5 -B 5 "shutdown\|cleanup\|JVM shutdown hook" "test-$config_name.log" | tail -15
        return 0
    fi
}

# Build application first
echo "Building application..."
mvn clean compile -f interface-exception-collector/pom.xml -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"

# Test 1: With Redis disabled
echo ""
echo "=== TEST 1: Redis Disabled ==="
test_configuration "no-redis" "test-no-redis"
TEST1_RESULT=$?

# Test 2: With Redis enabled and our fixes
echo ""
echo "=== TEST 2: Redis Enabled with Fixes ==="
test_configuration "with-fixes" "test"
TEST2_RESULT=$?

# Summary
echo ""
echo "=== TEST SUMMARY ==="
echo "Test 1 (No Redis): $([ $TEST1_RESULT -eq 0 ] && echo "✅ PASSED" || echo "❌ FAILED")"
echo "Test 2 (With Fixes): $([ $TEST2_RESULT -eq 0 ] && echo "✅ PASSED" || echo "❌ FAILED")"

if [ $TEST1_RESULT -eq 0 ] && [ $TEST2_RESULT -ne 0 ]; then
    echo ""
    echo "🔍 DIAGNOSIS: Redis is the source of memory leaks"
    echo "   The fixes need further enhancement"
elif [ $TEST1_RESULT -ne 0 ] && [ $TEST2_RESULT -ne 0 ]; then
    echo ""
    echo "🔍 DIAGNOSIS: Memory leaks exist even without Redis"
    echo "   The issue may be with other components"
elif [ $TEST1_RESULT -eq 0 ] && [ $TEST2_RESULT -eq 0 ]; then
    echo ""
    echo "🎉 SUCCESS: All memory leaks have been fixed!"
else
    echo ""
    echo "🤔 UNEXPECTED: Redis disabled failed but fixes worked"
fi

# Cleanup
echo ""
echo "Cleaning up test files..."
rm -f test-*.log

echo "Test completed!"