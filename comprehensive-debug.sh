#!/bin/bash

# Comprehensive debugging from multiple angles
set -e

echo "🔍 COMPREHENSIVE SUBSCRIPTION DEBUGGING"
echo "======================================"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Generate token
echo -e "${BLUE}🔑 Step 1: Token Generation${NC}"
if [ -f "generate-jwt-correct-secret.js" ]; then
    TOKEN=$(node generate-jwt-correct-secret.js 2>/dev/null | tail -n 1 | tr -d '\r\n')
    echo -e "${GREEN}✅ Token: ${TOKEN:0:30}...${NC}"
else
    echo -e "${RED}❌ No JWT generator${NC}"
    exit 1
fi

echo
echo -e "${BLUE}🌐 Step 2: Basic Connectivity Tests${NC}"

# Test if server is running
echo "Testing server connectivity..."
if curl -s -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Server is running${NC}"
else
    echo -e "${RED}❌ Server not responding${NC}"
    exit 1
fi

# Test GraphQL endpoint basic connectivity
echo "Testing GraphQL endpoint..."
BASIC_GRAPHQL=$(curl -s -w "%{http_code}" -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ __typename }"}')

HTTP_CODE=$(echo "$BASIC_GRAPHQL" | tail -n1)
RESPONSE=$(echo "$BASIC_GRAPHQL" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✅ GraphQL endpoint responding (HTTP $HTTP_CODE)${NC}"
    echo "Response: $RESPONSE"
else
    echo -e "${RED}❌ GraphQL endpoint failed (HTTP $HTTP_CODE)${NC}"
    echo "Response: $RESPONSE"
fi

echo
echo -e "${BLUE}📋 Step 3: GraphQL Schema Introspection${NC}"

# Check if subscription type exists
SCHEMA_CHECK=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ __schema { subscriptionType { name fields { name } } } }"}')

echo "Schema check response:"
echo "$SCHEMA_CHECK" | jq '.' 2>/dev/null || echo "$SCHEMA_CHECK"

# Check specific subscription
SUBSCRIPTION_CHECK=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ __type(name: \"Subscription\") { fields { name type { name } } } }"}')

echo "Subscription type check:"
echo "$SUBSCRIPTION_CHECK" | jq '.' 2>/dev/null || echo "$SUBSCRIPTION_CHECK"

echo
echo -e "${BLUE}🗄️ Step 4: Database & Exception Check${NC}"

# Check if we can query exceptions
EXCEPTION_QUERY=$(curl -s -X POST "http://localhost:8080/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ exceptions(pagination: { first: 1 }) { totalCount } }"}')

echo "Exception query test:"
echo "$EXCEPTION_QUERY" | jq '.' 2>/dev/null || echo "$EXCEPTION_QUERY"

echo
echo -e "${BLUE}🔌 Step 5: WebSocket Connection Test${NC}"

# Create comprehensive WebSocket test
cat > /tmp/ws-debug.js << 'EOF'
const WebSocket = require('ws');

const token = process.argv[2];
console.log('🔗 Testing WebSocket connection to ws://localhost:8080/graphql');

let connectionState = 'CONNECTING';
let subscriptionState = 'NONE';
let receivedMessages = [];

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: { 
        'Authorization': `Bearer ${token}`,
        'Sec-WebSocket-Protocol': 'graphql-ws'
    }
});

// Connection timeout
const connectionTimeout = setTimeout(() => {
    if (connectionState === 'CONNECTING') {
        console.log('❌ WebSocket connection timeout');
        process.exit(1);
    }
}, 10000);

ws.on('open', function() {
    console.log('✅ WebSocket connected');
    connectionState = 'CONNECTED';
    clearTimeout(connectionTimeout);
    
    console.log('📤 Sending connection_init...');
    ws.send(JSON.stringify({ 
        type: 'connection_init',
        payload: {}
    }));
});

ws.on('message', function(data) {
    const msg = JSON.parse(data.toString());
    receivedMessages.push(msg);
    
    console.log(`📨 Received: ${msg.type}`);
    
    switch (msg.type) {
        case 'connection_ack':
            console.log('✅ Connection acknowledged');
            connectionState = 'ACKNOWLEDGED';
            
            console.log('📡 Starting subscription...');
            subscriptionState = 'STARTING';
            ws.send(JSON.stringify({
                id: 'debug-sub-1',
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
                                }
                                timestamp
                                triggeredBy
                            }
                        }
                    `
                }
            }));
            break;
            
        case 'next':
            console.log('🔔 SUBSCRIPTION DATA RECEIVED!');
            console.log('Data:', JSON.stringify(msg.payload, null, 2));
            subscriptionState = 'RECEIVING';
            break;
            
        case 'error':
            console.log('❌ Error:', JSON.stringify(msg.payload, null, 2));
            subscriptionState = 'ERROR';
            break;
            
        case 'complete':
            console.log('✅ Subscription completed');
            subscriptionState = 'COMPLETED';
            break;
            
        default:
            console.log('📨 Unknown message type:', msg.type);
            console.log('Payload:', JSON.stringify(msg.payload, null, 2));
    }
});

ws.on('error', function(err) {
    console.error('❌ WebSocket error:', err.message);
    connectionState = 'ERROR';
});

ws.on('close', function(code, reason) {
    console.log(`🔌 WebSocket closed (${code}): ${reason || 'No reason'}`);
    
    console.log('\n📊 Final State Summary:');
    console.log(`Connection State: ${connectionState}`);
    console.log(`Subscription State: ${subscriptionState}`);
    console.log(`Messages Received: ${receivedMessages.length}`);
    
    if (receivedMessages.length > 0) {
        console.log('Message Types:', receivedMessages.map(m => m.type).join(', '));
    }
    
    process.exit(connectionState === 'ACKNOWLEDGED' ? 0 : 1);
});

// Test timeout
setTimeout(() => {
    console.log('⏰ Test timeout - closing connection');
    ws.close();
}, 15000);
EOF

if command -v node >/dev/null 2>&1; then
    if node -e "require('ws')" 2>/dev/null; then
        echo "Running WebSocket test..."
        node /tmp/ws-debug.js "$TOKEN" &
        WS_PID=$!
        
        # Wait for WebSocket to establish
        sleep 3
        
        echo
        echo -e "${BLUE}📤 Step 6: Trigger Test Events${NC}"
        
        # Method 1: Direct REST API
        echo "Method 1: Testing direct REST API..."
        REST_RESPONSE=$(curl -s -w "%{http_code}" -X POST "http://localhost:8080/api/exceptions" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d '{
                "transactionId": "debug-rest-'$(date +%s)'",
                "interfaceType": "ORDER",
                "exceptionReason": "REST API test",
                "operation": "CREATE_ORDER",
                "severity": "MEDIUM",
                "category": "VALIDATION_ERROR"
            }' 2>/dev/null)
        
        REST_CODE=$(echo "$REST_RESPONSE" | tail -n1)
        REST_BODY=$(echo "$REST_RESPONSE" | head -n -1)
        echo "REST API Response (HTTP $REST_CODE): $REST_BODY"
        
        sleep 2
        
        # Method 2: Kafka endpoint (if exists)
        echo "Method 2: Testing Kafka trigger endpoint..."
        KAFKA_RESPONSE=$(curl -s -w "%{http_code}" -X POST "http://localhost:8080/api/test/kafka/order-rejected" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d '{
                "transactionId": "debug-kafka-'$(date +%s)'",
                "externalId": "DEBUG-EXT-'$(date +%s)'",
                "operation": "CREATE_ORDER",
                "rejectedReason": "Kafka test event",
                "customerId": "DEBUG-CUST",
                "locationCode": "DEBUG-LOC"
            }' 2>/dev/null)
        
        KAFKA_CODE=$(echo "$KAFKA_RESPONSE" | tail -n1)
        KAFKA_BODY=$(echo "$KAFKA_RESPONSE" | head -n -1)
        echo "Kafka API Response (HTTP $KAFKA_CODE): $KAFKA_BODY"
        
        sleep 2
        
        # Method 3: GraphQL Mutation (if exists)
        echo "Method 3: Testing GraphQL mutation..."
        MUTATION_RESPONSE=$(curl -s -X POST "http://localhost:8080/graphql" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d '{
                "query": "mutation { acknowledgeException(input: { transactionId: \"test\", acknowledgedBy: \"debug\" }) { success } }"
            }')
        echo "GraphQL Mutation Response: $MUTATION_RESPONSE"
        
        # Wait for WebSocket test to complete
        wait $WS_PID
        WS_EXIT_CODE=$?
        
        rm -f /tmp/ws-debug.js
        
        echo
        echo -e "${BLUE}🔍 Step 7: Final Verification${NC}"
        
        # Check if any exceptions were created
        FINAL_CHECK=$(curl -s -X POST "http://localhost:8080/graphql" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d '{"query": "{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity timestamp } } } }"}')
        
        echo "Final exception check:"
        echo "$FINAL_CHECK" | jq '.' 2>/dev/null || echo "$FINAL_CHECK"
        
        echo
        echo -e "${BLUE}📋 DIAGNOSIS SUMMARY${NC}"
        echo "==================="
        
        if [ $WS_EXIT_CODE -eq 0 ]; then
            echo -e "${GREEN}✅ WebSocket connection: SUCCESS${NC}"
        else
            echo -e "${RED}❌ WebSocket connection: FAILED${NC}"
        fi
        
        if echo "$FINAL_CHECK" | grep -q "totalCount"; then
            echo -e "${GREEN}✅ GraphQL queries: WORKING${NC}"
        else
            echo -e "${RED}❌ GraphQL queries: FAILED${NC}"
        fi
        
        echo
        echo -e "${YELLOW}🔧 TROUBLESHOOTING STEPS:${NC}"
        echo "1. Check application logs for GraphQL event publishing"
        echo "2. Verify Spring Boot GraphQL WebSocket configuration"
        echo "3. Check if @SubscriptionMapping is properly registered"
        echo "4. Verify ExceptionEventPublisher is being called"
        echo "5. Check WebSocket security configuration"
        
    else
        echo -e "${RED}❌ Node.js 'ws' module not available${NC}"
        echo "Install with: npm install ws"
    fi
else
    echo -e "${RED}❌ Node.js not available${NC}"
fi

echo
echo -e "${BLUE}🔧 Step 8: Configuration Check${NC}"

# Check if GraphQL WebSocket is enabled
echo "Checking application configuration..."
if [ -f "interface-exception-collector/src/main/resources/application-local.yml" ]; then
    echo "Application config exists"
    if grep -q "graphql" interface-exception-collector/src/main/resources/application-local.yml; then
        echo "GraphQL config found in application.yml"
    else
        echo "⚠️  No GraphQL config found in application.yml"
    fi
else
    echo "⚠️  No application-local.yml found"
fi

echo
echo -e "${GREEN}🎯 DEBUG COMPLETE${NC}"
echo "Check the output above to identify where the subscription chain is breaking."