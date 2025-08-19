#!/bin/bash

echo "=== Minimal Memory Leak Test (No Database, No Redis) ==="

# Start application with minimal config
echo "Starting application with minimal configuration..."
java -cp "interface-exception-collector/target/classes:$(mvn dependency:build-classpath -f interface-exception-collector/pom.xml -q -Dmdep.outputFile=/dev/stdout)" \
    com.arcone.biopro.exception.collector.InterfaceExceptionCollectorApplication \
    --spring.profiles.active=minimal \
    --spring.main.web-application-type=servlet \
    > minimal-test.log 2>&1 &

APP_PID=$!
echo "Application PID: $APP_PID"

# Wait for startup
echo "Waiting for application to start..."
sleep 10

# Check if app is running
if ! kill -0 $APP_PID 2>/dev/null; then
    echo "❌ Application failed to start"
    echo "Startup logs:"
    tail -30 minimal-test.log
    exit 1
fi

echo "✅ Application started successfully"

# Stop application
echo "Stopping application..."
kill -TERM $APP_PID

# Wait for shutdown
echo "Waiting for shutdown..."
sleep 5

# Check for memory leak warnings
echo "Checking for memory leak warnings..."
if grep -q "appears to have started a thread named \[lettuce-" minimal-test.log; then
    echo "❌ MEMORY LEAK WARNINGS FOUND (even without Redis!):"
    grep "appears to have started a thread named \[lettuce-" minimal-test.log
    echo ""
    echo "This suggests the issue is not with our Redis configuration"
else
    echo "✅ NO MEMORY LEAK WARNINGS FOUND!"
    echo "This suggests Redis was the source of the memory leaks"
fi

echo ""
echo "Full shutdown logs:"
grep -A 5 -B 5 "Stopping service\|shutdown" minimal-test.log

# Cleanup
rm -f minimal-test.log

echo "Test completed!"