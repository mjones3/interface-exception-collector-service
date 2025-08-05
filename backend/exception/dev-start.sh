#!/bin/bash
set -e

echo "Starting Interface Exception Collector Service Development Environment..."

echo ""
echo "Building Maven project..."
mvn clean package -DskipTests

echo ""
echo "Starting Tilt development environment..."
tilt up

echo ""
echo "Development environment is ready!"
echo "- Application: http://localhost:8080"
echo "- Swagger UI: http://localhost:8080/swagger-ui.html"
echo "- Health Check: http://localhost:8080/actuator/health"
echo "- PostgreSQL: localhost:5432"
echo "- Kafka: localhost:9092"
echo ""
echo "Press Ctrl+C to stop all services"