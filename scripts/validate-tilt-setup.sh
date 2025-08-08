#!/bin/bash

# Script to validate Tilt setup for Interface Exception Collector Service

set -e

echo "🔍 Validating Tilt development setup..."

# Check if required tools are installed
echo "📋 Checking prerequisites..."

# Check Tilt
if ! command -v tilt &> /dev/null; then
    echo "❌ Tilt is not installed"
    echo "   Install from: https://docs.tilt.dev/install.html"
    exit 1
else
    echo "✅ Tilt is installed: $(tilt version)"
fi

# Check kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed"
    exit 1
else
    echo "✅ kubectl is installed: $(kubectl version --client --short 2>/dev/null || echo 'kubectl available')"
fi

# Check Kubernetes cluster
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Cannot connect to Kubernetes cluster"
    echo "   Please ensure your cluster is running"
    exit 1
else
    echo "✅ Kubernetes cluster is accessible"
fi

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed"
    exit 1
else
    echo "✅ Java is installed: $(java -version 2>&1 | head -n 1)"
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed"
    exit 1
else
    echo "✅ Maven is installed: $(mvn -version | head -n 1)"
fi

# Validate Tiltfile syntax
echo "📝 Validating Tiltfile syntax..."
if tilt validate Tiltfile &> /dev/null; then
    echo "✅ Tiltfile syntax is valid"
else
    echo "❌ Tiltfile syntax validation failed"
    tilt validate Tiltfile
    exit 1
fi

# Check required files exist
echo "📁 Checking required files..."

required_files=(
    "Tiltfile"
    "Dockerfile.dev"
    ".tiltignore"
    "dev-start.sh"
    "k8s/postgres.yaml"
    "k8s/redis.yaml"
    "k8s/kafka.yaml"
    "k8s/kafka-ui.yaml"
    "k8s/migration-job.yaml"
    "k8s/kafka-topics-job.yaml"
    "k8s/app.yaml"
    "src/main/resources/application-local.yml"
    "scripts/prepare-build.sh"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file exists"
    else
        echo "❌ $file is missing"
        exit 1
    fi
done

# Check if scripts are executable
echo "🔧 Checking script permissions..."
if [ -x "dev-start.sh" ]; then
    echo "✅ dev-start.sh is executable"
else
    echo "❌ dev-start.sh is not executable"
    echo "   Run: chmod +x dev-start.sh"
    exit 1
fi

if [ -x "scripts/prepare-build.sh" ]; then
    echo "✅ scripts/prepare-build.sh is executable"
else
    echo "❌ scripts/prepare-build.sh is not executable"
    echo "   Run: chmod +x scripts/prepare-build.sh"
    exit 1
fi

# Test Maven compilation
echo "🏗️  Testing Maven compilation..."
if mvn compile -q; then
    echo "✅ Maven compilation successful"
else
    echo "❌ Maven compilation failed"
    exit 1
fi

echo ""
echo "🎉 Tilt setup validation completed successfully!"
echo ""
echo "🚀 You can now start the development environment with:"
echo "   ./dev-start.sh"
echo ""
echo "📊 Once started, access:"
echo "   - Tilt UI: http://localhost:10350"
echo "   - Application: http://localhost:8080"
echo "   - Kafka UI: http://localhost:8081"