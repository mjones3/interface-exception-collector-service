#!/bin/bash

# Enhanced GraphQL Subscription Watcher
# Supports both WebSocket subscriptions and polling fallback

set -e

# Configuration
BASE_URL="http://localhost:8080"
WS_URL="ws://localhost:8080/graphql"
POLL_INTERVAL=3
KEEP_ALIVE_INTERVAL=30

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_colored() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to generate or get JWT token
get_jwt_token() {
    local token=""
    
    if [ -f "generate-jwt-correct-secret.js" ]; then
        # The JWT generator outputs multiple lines, the token is on the last line
        local output=$(node generate-jwt-correct-secret.js 2>/dev/null)
        if [ $? -eq 0 ] && [ -n "$output" ]; then
            # Get the last line which contains the token
            token=$(echo "$output" | tail -n 1 | tr -d '\r\n' | sed 's/^[[:space:]]*//' | sed 's/[[:space:]]*$//')
            
            # Validate token format (should start with eyJ and have 3 parts separated by dots)
            if [[ "$token" =~ ^eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$ ]]; then
                # Token is valid, just return it
                echo "$token"
                return 0
            else
                # Token format invalid, fall through to use fallback
                token=""
            fi
        fi
    fi
    
    # Use fallback token
    echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
}

# Function to test authentication
test_auth() {
    local token=$1
    
    print_colored $BLUE "🔐 Testing authentication..."
    
    local auth_test=$(curl -s -X POST "$BASE_URL/graphql" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d '{"query": "{ __schema { subscriptionType { fields { name } } } }"}')
    
    if echo "$auth_test" | grep -q "subscriptionType"; then
        print_colored $GREEN "✅ Authentication successful!"
        
        if command -v jq >/dev/null 2>&1; then
            print_colored $CYAN "📋 Available subscriptions:"
            echo "$auth_test" | jq -r '.data.__schema.subscriptionType.fields[].name' | sed 's/^/  - /'
        fi
        return 0
    else
        print_colored $RED "❌ Authentication failed"
        if command -v jq >/dev/null 2>&1; then
            echo "$auth_test" | jq '.'
        else
            echo "$auth_test"
        fi
        return 1
    fi
}

# Function for WebSocket subscription
websocket_subscription() {
    local token=$1
    
    print_colored $GREEN "🔗 Starting WebSocket subscription..."
    
    # Create WebSocket client
    cat > /tmp/subscription-client.js << EOF
const WebSocket = require('ws');

const token = process.argv[2];
if (!token) {
    console.error('❌ No token provided');
    process.exit(1);
}

const ws = new WebSocket('$WS_URL', {
    headers: {
        'Authorization': \`Bearer \${token}\`
    }
});

let isConnected = false;
let subscriptionActive = false;

ws.on('open', function open() {
    console.log('✅ WebSocket connected');
    
    // Send connection init
    ws.send(JSON.stringify({
        type: 'connection_init'
    }));
});

ws.on('message', function message(data) {
    const msg = JSON.parse(data.toString());
    const timestamp = new Date().toLocaleTimeString();
    
    switch (msg.type) {
        case 'connection_ack':
            console.log(\`[\${timestamp}] ✅ Connection acknowledged\`);
            isConnected = true;
            
            // Start subscription
            console.log(\`[\${timestamp}] 📡 Starting subscription...\`);
            ws.send(JSON.stringify({
                id: 'exception-subscription',
                type: 'start',
                payload: {
                    query: \`
                        subscription {
                            exceptionUpdated {
                                eventType
                                exception {
                                    transactionId
                                    status
                                    severity
                                    exceptionReason
                                    interfaceType
                                    retryCount
                                }
                                timestamp
                                triggeredBy
                            }
                        }
                    \`
                }
            }));
            subscriptionActive = true;
            break;
            
        case 'data':
            console.log(\`\\n🔔 [\${timestamp}] SUBSCRIPTION EVENT RECEIVED!\`);
            console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━���━');
            
            if (msg.payload && msg.payload.data && msg.payload.data.exceptionUpdated) {
                const event = msg.payload.data.exceptionUpdated;
                console.log(\`📋 Event Type: \${event.eventType}\`);
                console.log(\`⏰ Timestamp: \${event.timestamp}\`);
                console.log(\`👤 Triggered By: \${event.triggeredBy || 'System'}\`);
                
                if (event.exception) {
                    const ex = event.exception;
                    console.log(\`🆔 Transaction ID: \${ex.transactionId}\`);
                    console.log(\`📊 Status: \${ex.status}\`);
                    console.log(\`⚠️  Severity: \${ex.severity}\`);
                    console.log(\`🔧 Interface Type: \${ex.interfaceType}\`);
                    console.log(\`🔄 Retry Count: \${ex.retryCount}\`);
                    console.log(\`💬 Reason: \${ex.exceptionReason}\`);
                }
            } else {
                console.log('📄 Raw data:', JSON.stringify(msg.payload, null, 2));
            }
            console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\\n');
            
            // Beep sound (if terminal supports it)
            process.stdout.write('\\x07');
            break;
            
        case 'error':
            console.log(\`❌ [\${timestamp}] Subscription error:\`, msg.payload);
            break;
            
        case 'complete':
            console.log(\`✅ [\${timestamp}] Subscription completed\`);
            subscriptionActive = false;
            break;
            
        default:
            console.log(\`📨 [\${timestamp}] Message:\`, msg);
    }
});

ws.on('error', function error(err) {
    console.error('❌ WebSocket error:', err.message);
});

ws.on('close', function close(code, reason) {
    console.log(\`🔌 WebSocket closed (\${code}): \${reason || 'No reason provided'}\`);
    process.exit(0);
});

// Keep alive ping
const keepAlive = setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.ping();
    }
}, $KEEP_ALIVE_INTERVAL * 1000);

// Status updates
const statusInterval = setInterval(() => {
    const timestamp = new Date().toLocaleTimeString();
    if (subscriptionActive) {
        console.log(\`[\${timestamp}] 👂 Listening for events... (Press Ctrl+C to stop)\`);
    }
}, 60000);

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\\n🛑 Shutting down...');
    clearInterval(keepAlive);
    clearInterval(statusInterval);
    
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    } else {
        process.exit(0);
    }
});

console.log('👂 Waiting for subscription events...');
console.log('💡 Tip: Run ./trigger-events.sh in another terminal to generate test events');
console.log('🛑 Press Ctrl+C to stop\\n');
EOF

    # Run WebSocket client
    node /tmp/subscription-client.js "$token"
    
    # Cleanup
    rm -f /tmp/subscription-client.js
}

# Function for polling fallback
polling_subscription() {
    local token=$1
    
    print_colored $YELLOW "📊 Starting polling mode (fallback)..."
    
    local last_hash=""
    local poll_count=0
    
    while true; do
        poll_count=$((poll_count + 1))
        local timestamp=$(date '+%H:%M:%S')
        
        echo
        print_colored $BLUE "[$timestamp] Poll #$poll_count"
        
        # Query for exceptions
        local response=$(curl -s -X POST "$BASE_URL/graphql" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $token" \
            -d '{
                "query": "{ exceptions(pagination: { first: 10 }) { totalCount edges { node { transactionId status severity interfaceType timestamp exceptionReason retryCount } } } }"
            }')
        
        if echo "$response" | grep -q "exceptions"; then
            local total=$(echo "$response" | jq -r '.data.exceptions.totalCount' 2>/dev/null || echo "N/A")
            local current_hash=$(echo "$response" | md5sum | cut -d' ' -f1)
            
            echo "  Total exceptions: $total"
            
            if [ "$current_hash" != "$last_hash" ]; then
                print_colored $GREEN "  🔔 NEW DATA DETECTED!"
                echo "  Recent exceptions:"
                
                if command -v jq >/dev/null 2>&1; then
                    echo "$response" | jq -r '.data.exceptions.edges[] | 
                        "    🔸 " + (.node.timestamp | split("T")[0] + " " + (.node.timestamp | split("T")[1] | split(".")[0])) + " " + .node.transactionId + 
                        "\n      Status: " + .node.status + " | Severity: " + .node.severity + " | Retries: " + (.node.retryCount | tostring) +
                        "\n      Type: " + .node.interfaceType +
                        "\n      Reason: " + .node.exceptionReason'
                else
                    echo "$response"
                fi
                
                last_hash="$current_hash"
                
                # Beep (if available)
                command -v tput >/dev/null && tput bel
                
            else
                echo "  No changes detected"
            fi
            
        elif echo "$response" | grep -q "errors"; then
            print_colored $RED "  ❌ GraphQL errors:"
            if command -v jq >/dev/null 2>&1; then
                echo "$response" | jq -r '.errors[].message' | sed 's/^/    /'
            else
                echo "$response"
            fi
        else
            print_colored $YELLOW "  ⚠️  No data received"
        fi
        
        sleep $POLL_INTERVAL
    done
}

# Main function
main() {
    print_colored $PURPLE "🔴 Enhanced GraphQL Subscription Watcher"
    print_colored $PURPLE "========================================"
    
    # Get JWT token
    print_colored $BLUE "🔑 Generating fresh JWT token..."
    local token
    token=$(get_jwt_token)
    
    # Check if we got a fresh token or fallback
    if [[ "$token" =~ ^eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$ ]] && [ ${#token} -gt 100 ]; then
        if [ -f "generate-jwt-correct-secret.js" ]; then
            print_colored $GREEN "✅ Fresh token generated successfully"
        else
            print_colored $YELLOW "⚠️  Using fallback JWT token"
        fi
    else
        print_colored $YELLOW "⚠️  Using fallback JWT token"
    fi
    
    print_colored $CYAN "Token: ${token:0:20}... (${#token} chars)"
    
    # Test authentication
    if ! test_auth "$token"; then
        exit 1
    fi
    
    echo
    print_colored $BLUE "💡 Usage Tips:"
    echo "  - Run './trigger-events.sh' in another terminal to generate test events"
    echo "  - Press Ctrl+C to stop watching"
    echo "  - WebSocket mode provides real-time updates"
    echo "  - Polling mode is used as fallback"
    echo
    
    # Try WebSocket first, fallback to polling
    if command -v node >/dev/null 2>&1; then
        if node -e "require('ws')" 2>/dev/null; then
            print_colored $GREEN "🚀 Using WebSocket mode for real-time subscriptions"
            websocket_subscription "$token"
        else
            print_colored $YELLOW "⚠️  WebSocket module 'ws' not found. Install with: npm install ws"
            print_colored $YELLOW "🔄 Falling back to polling mode..."
            polling_subscription "$token"
        fi
    else
        print_colored $YELLOW "⚠️  Node.js not found. Using polling mode..."
        polling_subscription "$token"
    fi
}

# Handle Ctrl+C gracefully
trap 'echo -e "\n🛑 Stopping subscription watcher..."; exit 0' INT

# Run main function
main "$@"