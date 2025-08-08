#!/bin/bash

# Development startup script for Interface Exception Collector Service

set -e

echo "🚀 Starting Interface Exception Collector Service Development Environment"

# Check if Tilt is installed
if ! command -v tilt &> /dev/null; then
    echo "❌ Tilt is not installed. Please install Tilt first:"
    echo "   https://docs.tilt.dev/install.html"
    exit 1
fi

# Check if kubectl is installed and configured
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed. Please install kubectl first."
    exit 1
fi

# Check if we can connect to Kubernetes cluster
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Cannot connect to Kubernetes cluster. Please ensure your cluster is running."
    echo "   For local development, you can use:"
    echo "   - Docker Desktop with Kubernetes enabled"
    echo "   - minikube"
    echo "   - kind"
    exit 1
fi

# Prepare the build
echo "🔧 Preparing Maven build..."
./scripts/prepare-build.sh

# Start Tilt
echo "🎯 Starting Tilt..."
echo "📊 Tilt UI will be available at: http://localhost:10350"
echo "🔍 Application will be available at: http://localhost:8080"
echo "📖 API Documentation: http://localhost:8080/swagger-ui.html"
echo "💊 Health Check: http://localhost:8080/actuator/health"
echo "📈 Metrics: http://localhost:8080/actuator/prometheus"
echo "🎛️  Kafka UI: http://localhost:8081"
echo ""
echo "Press Ctrl+C to stop the development environment"

tilt up