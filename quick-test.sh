#!/bin/bash

echo "=== Quick Memory Leak Test ==="

# Build first
echo "Building application..."
mvn clean compile -f interface-exception-collector/pom.xml -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"

# Start application
echo "Starting application..."
java -cp "interface-exception-collector/target/classes:$(mvn dependency:build-classpath -f interface-exception-collector/pom.xml -q -Dmdep.outputFile=/dev/stdout)" \
    com.arcone.biopro.exception.collector.InterfaceExceptionCollectorApplication \
    --spring.profiles.active=test \
    --server.port=8081 \
    --logging.level.com.arcone.biopro.exception.collector.infrastructure.config=INFO \
    > app-test.log 2>&1 &

APP_PID=$!
echo "Application PID: $APP_PID"

# Wait for startup
echo "Waiting for application to start..."
sleep 15

# Check if app is running
if ! kill -0 $APP_PID 2>/dev/null; then
    echo "❌ Application failed to start"
    echo "Startup logs:"
    tail -20 app-test.log
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
if grep -q "appears to have started a thread named \[lettuce-" app-test.log; then
    echo "❌ MEMORY LEAK WARNINGS FOUND:"
    grep "appears to have started a thread named \[lettuce-" app-test.log
    echo ""
    echo "Shutdown logs:"
    grep -A 10 -B 5 "Stopping service\|shutdown\|cleanup" app-test.log | tail -20
else
    echo "✅ NO MEMORY LEAK WARNINGS FOUND!"
    echo ""
    echo "Shutdown logs:"
    grep -A 5 -B 5 "shutdown\|cleanup\|JVM shutdown hook" app-test.log | tail -15
fi

# Show our configuration logs
echo ""
echo "Configuration logs:"
grep -E "(Creating Lettuce|JVM shutdown hook|Redis shutdown|Lettuce.*cleanup)" app-test.log

# Cleanup
rm -f app-test.log

echo "Test completed!"