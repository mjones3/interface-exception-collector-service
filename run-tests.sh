#!/bin/bash

# Main test runner menu for GraphQL subscriptions

echo "🚀 GraphQL Subscription Test Suite"
echo "=================================="
echo

# Check if we're in WSL
if [ -n "$WSL_DISTRO_NAME" ]; then
    echo "✅ Running in WSL: $WSL_DISTRO_NAME"
else
    echo "ℹ️  Running in native Linux/bash environment"
fi

echo

while true; do
    echo "📋 Available Tests:"
    echo "1. 🔍 Quick subscription test"
    echo "2. 🔐 Test with authentication"
    echo "3. 👂 Watch live subscriptions (WebSocket)"
    echo "4. 📊 Watch with polling (HTTP)"
    echo "5. 🎯 Trigger test events"
    echo "6. 🐛 Debug subscription events"
    echo "7. 📈 Simple watcher"
    echo "8. 🚪 Exit"
    echo
    
    read -p "Choose an option (1-8): " choice
    
    case $choice in
        1)
            echo "🔍 Running quick subscription test..."
            ./quick-test-subscriptions.sh
            ;;
        2)
            echo "🔐 Testing authentication..."
            ./test-with-auth.sh
            ;;
        3)
            echo "👂 Starting live WebSocket subscription watcher..."
            ./watch-live-subscriptions.sh
            ;;
        4)
            echo "📊 Starting polling-based watcher..."
            ./watch-graphql.sh
            ;;
        5)
            echo "🎯 Opening event trigger menu..."
            ./trigger-events.sh
            ;;
        6)
            echo "🐛 Running debug test..."
            ./debug-subscription-events.sh
            ;;
        7)
            echo "📈 Starting simple watcher..."
            ./watch-simple.sh
            ;;
        8)
            echo "👋 Goodbye!"
            exit 0
            ;;
        *)
            echo "❌ Invalid choice. Please select 1-8."
            ;;
    esac
    
    echo
    echo "Press Enter to return to menu..."
    read
    echo
done