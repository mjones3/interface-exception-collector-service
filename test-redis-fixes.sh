#!/bin/bash

echo "=== Testing Redis Memory Leak Fixes ==="

# Start application with Redis enabled and our fixes
echo "Starting application with Redis and our memory leak fixes..."
java -cp "interface-exception-collector/target/classes:$(mvn dependency:build-classpath -f interface-exception-collector/pom.xml -q -Dmdep.outputFile=/dev/stdout)" \
    com.arcone.biopro.exception.collector.InterfaceExceptionCollectorApplication \
    --spring.profiles.active=redis-test \
    --spring.main.web-application-type=servlet \
    > redis-test.log 2>&1 &

APP_PID=$!
echo "Application PID: $APP_PID"

# Wait for startup
echo "Waiting for application to start..."
sleep 15

# Check if app is running
if ! kill -0 $APP_PID 2>/dev/null; then
    echo "❌ Application failed to start"
    echo "Startup logs:"
    tail -30 redis-test.log
    exit 1
fi

echo "✅ Application started successfully"

# Stop application
echo "Stopping application..."
kill -TERM $APP_PID

# Wait for shutdown
echo "Waiting for shutdown..."
sleep 10

# Check for memory leak warnings
echo "Checking for memory leak warnings..."
if grep -q "appears to have started a thread named \[lettuce-" redis-test.log; then
    echo "❌ MEMORY LEAK WARNINGS STILL FOUND:"
    grep "appears to have started a thread named \[lettuce-" redis-test.log
    echo ""
    echo "Our fixes are not working properly"
else
    echo "✅ NO MEMORY LEAK WARNINGS FOUND!"
    echo "Our Redis memory leak fixes are working!"
fi

echo ""
echo "Shutdown configuration logs:"
grep -E "(Creating Lettuce|JVM shutdown hook|Redis shutdown|Lettuce.*cleanup)" redis-test.log

echo ""
echo "Full shutdown sequence:"
grep -A 10 -B 5 "Stopping service" redis-test.log

# Cleanup
rm -f redis-test.log

echo "Test completed!"