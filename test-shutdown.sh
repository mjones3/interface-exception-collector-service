#!/bin/bash

echo "Testing Lettuce Redis shutdown behavior..."

# Build the application
echo "Building application..."
mvn clean package -f interface-exception-collector/pom.xml -DskipTests=true -q

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Build successful!"

# Start the application in background
echo "Starting application..."
java -jar interface-exception-collector/target/interface-exception-collector-1.0.0-SNAPSHOT.jar \
    --spring.profiles.active=test \
    --server.port=8081 \
    --logging.level.com.arcone.biopro.exception.collector.infrastructure.config=DEBUG \
    > app.log 2>&1 &

APP_PID=$!
echo "Application started with PID: $APP_PID"

# Wait for application to start
echo "Waiting for application to start..."
sleep 15

# Check if application is running
if ! kill -0 $APP_PID 2>/dev/null; then
    echo "Application failed to start!"
    cat app.log
    exit 1
fi

echo "Application is running. Testing shutdown..."

# Send SIGTERM to gracefully shutdown
kill -TERM $APP_PID

# Wait for shutdown and capture output
echo "Waiting for graceful shutdown..."
wait $APP_PID

echo "Application shutdown complete. Checking logs for memory leak warnings..."

# Check for memory leak warnings
if grep -q "appears to have started a thread named \[lettuce-" app.log; then
    echo "❌ MEMORY LEAK WARNINGS FOUND:"
    grep "appears to have started a thread named \[lettuce-" app.log
    echo ""
    echo "Full shutdown logs:"
    grep -A 10 -B 10 "Redis shutdown" app.log
    exit 1
else
    echo "✅ NO MEMORY LEAK WARNINGS FOUND!"
    echo ""
    echo "Shutdown logs:"
    grep -A 5 -B 5 "Redis shutdown\|Lettuce.*cleanup" app.log
fi

# Cleanup
rm -f app.log

echo "Test completed successfully!"