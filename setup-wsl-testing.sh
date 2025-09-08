#!/bin/bash

# Setup script for WSL GraphQL subscription testing

echo "🔧 WSL GraphQL Subscription Testing Setup"
echo "========================================"

# Check WSL environment
if [ -n "$WSL_DISTRO_NAME" ]; then
    echo "✅ WSL detected: $WSL_DISTRO_NAME"
else
    echo "⚠️  Not running in WSL, but that's okay!"
fi

echo

# Check required tools
echo "🔍 Checking required tools..."

# Check curl
if command -v curl >/dev/null 2>&1; then
    echo "✅ curl is available"
else
    echo "❌ curl not found. Install with: sudo apt update && sudo apt install curl"
    exit 1
fi

# Check jq (optional but recommended)
if command -v jq >/dev/null 2>&1; then
    echo "✅ jq is available (great for JSON parsing)"
else
    echo "⚠️  jq not found (optional). Install with: sudo apt install jq"
fi

# Check Node.js (for WebSocket subscriptions)
if command -v node >/dev/null 2>&1; then
    node_version=$(node --version)
    echo "✅ Node.js is available: $node_version"
    
    # Check if ws module is available
    if node -e "require('ws')" 2>/dev/null; then
        echo "✅ WebSocket module 'ws' is available"
    else
        echo "⚠️  WebSocket module 'ws' not found"
        echo "💡 Install with: npm install ws"
        echo "   (This enables real-time WebSocket subscription testing)"
    fi
else
    echo "⚠️  Node.js not found (optional for WebSocket testing)"
    echo "💡 Install with: curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash - && sudo apt-get install -y nodejs"
fi

echo

# Check if services are running
echo "🌐 Checking service availability..."

# Check GraphQL endpoint
if curl -s -f "http://localhost:8080/graphql" >/dev/null 2>&1; then
    echo "✅ GraphQL service is running on port 8080"
else
    echo "❌ GraphQL service not available on port 8080"
    echo "💡 Make sure your application is running with: tilt up"
fi

# Check partner order service
if curl -s -f "http://localhost:8090/actuator/health" >/dev/null 2>&1; then
    echo "✅ Partner Order Service is running on port 8090"
else
    echo "⚠️  Partner Order Service not available on port 8090"
    echo "💡 This is needed for triggering test exceptions"
fi

echo

# List available scripts
echo "📋 Available bash scripts:"
ls -la *.sh | grep -E "(watch|test|trigger|debug|run)" | awk '{print "  " $9}' | sort

echo
echo "🚀 Quick start:"
echo "  1. Run './run-tests.sh' for interactive menu"
echo "  2. Run './quick-test-subscriptions.sh' for a quick test"
echo "  3. Run './watch-graphql.sh' to start monitoring"
echo

# Create a simple alias helper
echo "💡 Tip: Add these aliases to your ~/.bashrc for quick access:"
echo "  alias gql-test='./quick-test-subscriptions.sh'"
echo "  alias gql-watch='./watch-graphql.sh'"
echo "  alias gql-trigger='./trigger-events.sh'"
echo "  alias gql-menu='./run-tests.sh'"

echo
echo "✅ Setup check complete!"