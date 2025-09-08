const WebSocket = require('ws');

const token = process.argv[2] || 'your-token-here';
const wsUrl = 'ws://localhost:8080/graphql';

console.log('🎯 WORKING SUBSCRIPTION TEST');
console.log('============================');
console.log('🔌 Connecting to:', wsUrl);

const ws = new WebSocket(wsUrl, {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});

let messageCount = 0;
let subscriptionActive = false;
let eventsReceived = 0;

ws.on('open', () => {
    console.log('✅ WebSocket connected successfully');
    
    // Send connection init
    ws.send(JSON.stringify({
        type: 'connection_init',
        payload: {
            Authorization: `Bearer ${token}`
        }
    }));
});

ws.on('message', (data) => {
    const message = JSON.parse(data.toString());
    messageCount++;
    
    console.log(`📨 Message ${messageCount}:`, JSON.stringify(message, null, 2));
    
    if (message.type === 'connection_ack') {
        console.log('✅ Connection acknowledged - Starting subscription...');
        
        // Start exception subscription
        ws.send(JSON.stringify({
            id: 'exception-sub',
            type: 'start',
            payload: {
                query: 'subscription { exceptionUpdated { eventType exception { transactionId exceptionReason severity } timestamp triggeredBy } }'
            }
        }));
        
        subscriptionActive = true;
        console.log('📡 Exception subscription started - Listening for events...');
        
    } else if (message.type === 'next' && message.payload && message.payload.data) {
        const eventData = message.payload.data.exceptionUpdated;
        if (eventData) {
            eventsReceived++;
            console.log('🔔 REAL-TIME EVENT RECEIVED #' + eventsReceived + ':');
            console.log('   Event Type:', eventData.eventType);
            console.log('   Transaction ID:', eventData.exception.transactionId);
            console.log('   Reason:', eventData.exception.exceptionReason);
            console.log('   Severity:', eventData.exception.severity);
            console.log('   Timestamp:', eventData.timestamp);
            console.log('   Triggered By:', eventData.triggeredBy);
            console.log('');
        }
    }
});

ws.on('error', (error) => {
    console.log('❌ WebSocket error:', error.message);
});

ws.on('close', (code, reason) => {
    console.log('🔌 WebSocket closed:', code, reason.toString());
    console.log('📊 FINAL STATS:');
    console.log('   Total messages received:', messageCount);
    console.log('   Subscription was active:', subscriptionActive);
    console.log('   Real events received:', eventsReceived);
    
    if (subscriptionActive && eventsReceived > 0) {
        console.log('✅ SUBSCRIPTION SYSTEM: FULLY WORKING');
    } else if (subscriptionActive) {
        console.log('⚠️ SUBSCRIPTION SYSTEM: CONNECTED BUT NO EVENTS');
    } else {
        console.log('❌ SUBSCRIPTION SYSTEM: CONNECTION FAILED');
    }
});

// Keep connection alive for 15 seconds
setTimeout(() => {
    console.log('⏰ Test completed - Closing connection');
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
}, 15000);

console.log('⏳ Listening for 15 seconds...');