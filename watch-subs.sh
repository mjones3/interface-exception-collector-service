#!/bin/bash

# Simple GraphQL Subscription Watcher for WSL
# Optimized for terminal usage

set -e

# Configuration
BASE_URL="http://localhost:8080"
WS_URL="ws://localhost:8080/graphql"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}🔴 GraphQL Subscription Watcher${NC}"
echo "=================================="

# Generate JWT token
echo -e "${BLUE}🔑 Generating JWT token...${NC}"
if [ -f "generate-jwt-correct-secret.js" ]; then
    TOKEN_OUTPUT=$(node generate-jwt-correct-secret.js 2>/dev/null)
    if [ $? -eq 0 ]; then
        TOKEN=$(echo "$TOKEN_OUTPUT" | tail -n 1 | tr -d '\r\n')
        echo -e "${GREEN}✅ Token generated successfully${NC}"
        echo -e "${CYAN}Token: ${TOKEN:0:20}...${NC}"
    else
        echo -e "${YELLOW}⚠️  Token generation failed, using fallback${NC}"
        TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
    fi
else
    echo -e "${YELLOW}⚠️  JWT generator not found, using fallback token${NC}"
    TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
fi

# Test authentication
echo -e "${BLUE}🔐 Testing authentication...${NC}"
AUTH_TEST=$(curl -s -X POST "$BASE_URL/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ __schema { subscriptionType { fields { name } } } }"}')

if echo "$AUTH_TEST" | grep -q "subscriptionType"; then
    echo -e "${GREEN}✅ Authentication successful!${NC}"
else
    echo -e "${RED}❌ Authentication failed${NC}"
    echo "$AUTH_TEST"
    exit 1
fi

echo
echo -e "${BLUE}💡 Usage Tips:${NC}"
echo "  - Run './trigger-events.sh' in another terminal to generate test events"
echo "  - Press Ctrl+C to stop watching"
echo

# Check if Node.js and ws module are available for WebSocket mode
if command -v node >/dev/null 2>&1 && node -e "require('ws')" 2>/dev/null; then
    echo -e "${GREEN}🚀 Using WebSocket mode for real-time subscriptions${NC}"
    
    # Create WebSocket client
    cat > /tmp/ws-client.js << 'EOF'
const WebSocket = require('ws');

const token = process.argv[2];
const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: { 'Authorization': `Bearer ${token}` }
});

let subscriptionActive = false;

ws.on('open', function() {
    console.log('✅ WebSocket connected');
    ws.send(JSON.stringify({ type: 'connection_init' }));
});

ws.on('message', function(data) {
    const msg = JSON.parse(data.toString());
    const timestamp = new Date().toLocaleTimeString();
    
    switch (msg.type) {
        case 'connection_ack':
            console.log(`[${timestamp}] ✅ Connection acknowledged`);
            console.log(`[${timestamp}] 📡 Starting subscription...`);
            ws.send(JSON.stringify({
                id: 'exception-subscription',
                type: 'start',
                payload: {
                    query: `
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
                    `
                }
            }));
            subscriptionActive = true;
            break;
            
        case 'data':
            console.log(`\n🔔 [${timestamp}] SUBSCRIPTION EVENT!`);
            console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
            
            if (msg.payload?.data?.exceptionUpdated) {
                const event = msg.payload.data.exceptionUpdated;
                console.log(`📋 Event Type: ${event.eventType}`);
                console.log(`⏰ Timestamp: ${event.timestamp}`);
                console.log(`👤 Triggered By: ${event.triggeredBy || 'System'}`);
                
                if (event.exception) {
                    const ex = event.exception;
                    console.log(`🆔 Transaction ID: ${ex.transactionId}`);
                    console.log(`📊 Status: ${ex.status}`);
                    console.log(`⚠️  Severity: ${ex.severity}`);
                    console.log(`🔧 Interface Type: ${ex.interfaceType}`);
                    console.log(`🔄 Retry Count: ${ex.retryCount}`);
                    console.log(`💬 Reason: ${ex.exceptionReason}`);
                }
            } else {
                console.log('📄 Raw data:', JSON.stringify(msg.payload, null, 2));
            }
            console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
            process.stdout.write('\x07'); // Beep
            break;
            
        case 'error':
            console.log(`❌ [${timestamp}] Subscription error:`, msg.payload);
            break;
            
        case 'complete':
            console.log(`✅ [${timestamp}] Subscription completed`);
            break;
            
        default:
            console.log(`📨 [${timestamp}] Message:`, msg);
    }
});

ws.on('error', function(err) {
    console.error('❌ WebSocket error:', err.message);
});

ws.on('close', function(code, reason) {
    console.log(`🔌 WebSocket closed (${code}): ${reason || 'Connection closed'}`);
    process.exit(0);
});

// Keep alive
setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) ws.ping();
}, 30000);

// Status updates
setInterval(() => {
    if (subscriptionActive) {
        console.log(`[${new Date().toLocaleTimeString()}] 👂 Listening... (Ctrl+C to stop)`);
    }
}, 60000);

process.on('SIGINT', () => {
    console.log('\n🛑 Shutting down...');
    ws.close();
});

console.log('👂 Waiting for subscription events...');
console.log('🛑 Press Ctrl+C to stop\n');
EOF

    # Run WebSocket client
    node /tmp/ws-client.js "$TOKEN"
    rm -f /tmp/ws-client.js
    
else
    echo -e "${YELLOW}🔄 Using polling mode (WebSocket not available)${NC}"
    
    # Polling fallback
    LAST_HASH=""
    POLL_COUNT=0
    
    while true; do
        POLL_COUNT=$((POLL_COUNT + 1))
        TIMESTAMP=$(date '+%H:%M:%S')
        
        echo
        echo -e "${BLUE}[$TIMESTAMP] Poll #$POLL_COUNT${NC}"
        
        RESPONSE=$(curl -s -X POST "$BASE_URL/graphql" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d '{"query": "{ exceptions(pagination: { first: 10 }) { totalCount edges { node { transactionId status severity interfaceType timestamp exceptionReason retryCount } } } }"}')
        
        if echo "$RESPONSE" | grep -q "exceptions"; then
            TOTAL=$(echo "$RESPONSE" | jq -r '.data.exceptions.totalCount' 2>/dev/null || echo "N/A")
            CURRENT_HASH=$(echo "$RESPONSE" | md5sum | cut -d' ' -f1)
            
            echo "  Total exceptions: $TOTAL"
            
            if [ "$CURRENT_HASH" != "$LAST_HASH" ]; then
                echo -e "  ${GREEN}🔔 NEW DATA DETECTED!${NC}"
                echo "  Recent exceptions:"
                
                if command -v jq >/dev/null 2>&1; then
                    echo "$RESPONSE" | jq -r '.data.exceptions.edges[] | 
                        "    🔸 " + (.node.timestamp | split("T")[0] + " " + (.node.timestamp | split("T")[1] | split(".")[0])) + " " + .node.transactionId + 
                        "\n      Status: " + .node.status + " | Severity: " + .node.severity + " | Retries: " + (.node.retryCount | tostring) +
                        "\n      Type: " + .node.interfaceType +
                        "\n      Reason: " + .node.exceptionReason'
                else
                    echo "$RESPONSE"
                fi
                
                LAST_HASH="$CURRENT_HASH"
                command -v tput >/dev/null && tput bel
            else
                echo "  No changes detected"
            fi
        else
            echo -e "  ${YELLOW}⚠️  No data or error${NC}"
        fi
        
        sleep 3
    done
fi