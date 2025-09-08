const WebSocket = require('ws');

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: {
        'Authorization': 'Bearer 🔑 Using CORRECT secret from application.yml default value📏 Secret: mySecretKey1234567890123456789012345678901234567890📏 Length: 51👤 User: test-user🛡️  Roles: ADMIN⏰ Expires: 2025-09-08T17:35:11.000Z🎯 Generated Token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDkzMTEsImV4cCI6MTc1NzM1MjkxMX0.9DjwpSK0O52EJHRFkfQedvGu8utEjX6DnkCa15h_vGA'
    }
});

let eventReceived = false;
let subscriptionReady = false;

ws.on('open', function open() {
    console.log('✅ WebSocket connected');
    
    // Send connection init
    ws.send(JSON.stringify({
        type: 'connection_init'
    }));
});

ws.on('message', function message(data) {
    const msg = JSON.parse(data.toString());
    const timestamp = new Date().toISOString();
    
    if (msg.type === 'connection_ack') {
        console.log('✅ Connection acknowledged');
        
        // Start subscription
        ws.send(JSON.stringify({
            id: 'real-endpoint-test',
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
                                externalId
                            }
                            timestamp
                            triggeredBy
                        }
                    }
                `
            }
        }));
        
        subscriptionReady = true;
        console.log('📡 Subscription started - ready for real business events!');
        
    } else if (msg.type === 'data') {
        console.log(`\n🎉 [${timestamp}] REAL BUSINESS EVENT RECEIVED!`);
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        
        if (msg.payload && msg.payload.data && msg.payload.data.exceptionUpdated) {
            const event = msg.payload.data.exceptionUpdated;
            console.log(`📋 Event Type: ${event.eventType}`);
            console.log(`⏰ Timestamp: ${event.timestamp}`);
            console.log(`👤 Triggered By: ${event.triggeredBy || 'System'}`);
            
            if (event.exception) {
                const ex = event.exception;
                console.log(`🆔 Transaction ID: ${ex.transactionId}`);
                console.log(`🔗 External ID: ${ex.externalId}`);
                console.log(`📊 Status: ${ex.status}`);
                console.log(`⚠️  Severity: ${ex.severity}`);
                console.log(`💬 Reason: ${ex.exceptionReason}`);
            }
        } else {
            console.log('📄 Raw event data:', JSON.stringify(msg.payload, null, 2));
        }
        
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
        
        eventReceived = true;
        
        // Exit after receiving event
        setTimeout(() => {
            console.log('✅ SUCCESS! Real business GraphQL subscription working!');
            process.exit(0);
        }, 1000);
        
    } else if (msg.type === 'error') {
        console.log('❌ Subscription error:', msg.payload);
        process.exit(1);
        
    } else {
        console.log(`📨 Message (${msg.type}):`, msg.payload || '');
    }
});

ws.on('error', function error(err) {
    console.error('❌ WebSocket error:', err.message);
    process.exit(1);
});

ws.on('close', function close() {
    if (!eventReceived) {
        console.log('❌ WebSocket closed without receiving events');
        process.exit(1);
    }
});

// Timeout after 45 seconds
setTimeout(() => {
    if (!subscriptionReady) {
        console.log('❌ Timeout: Subscription not ready after 45 seconds');
        process.exit(1);
    } else if (!eventReceived) {
        console.log('❌ Timeout: No subscription events received after 45 seconds');
        console.log('💡 This might indicate Kafka consumers are not processing events');
        process.exit(1);
    }
}, 45000);

// Signal when ready for testing
setTimeout(() => {
    if (subscriptionReady) {
        console.log('🚀 Subscription ready - creating business exception via real API!');
    }
}, 2000);
