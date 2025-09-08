const WebSocket = require('ws');

const token = process.argv[2] || 'your-token-here';
const wsUrl = 'ws://localhost:8080/graphql';

console.log('ðŸŽ¯ COMPREHENSIVE SUBSCRIPTION TEST');
console.log('==================================');
console.log('ðŸ”Œ Connecting to:', wsUrl);

const ws = new WebSocket(wsUrl, {
    headers: {
        'Authorization': Bearer ${token}
    }
});

let messageCount = 0;
let subscriptionActive = false;

ws.on('open', () => {
    console.log('âœ… WebSocket connected successfully');
    
    // Send connection init
    ws.send(JSON.stringify({
        type: 'connection_init',
        payload: {
            Authorization: Bearer ${token}
        }
    }));
});

ws.on('message', (data) => {
    const message = JSON.parse(data.toString());
    messageCount++;
    
    console.log(ðŸ“¨ Message ${messageCount}:, JSON.stringify(message, null, 2));
    
    if (message.type === 'connection_ack') {
        console.log('âœ… Connection acknowledged - Starting subscription...');
        
        // Start exception subscription
        ws.send(JSON.stringify({
            id: 'exception-sub',
            type: 'start',
            payload: {
                query: 'subscription { exceptionUpdated { eventType exception { transactionId exceptionReason severity } timestamp triggeredBy } }'
            }
        }));
        
        subscriptionActive = true;
        console.log('ðŸ“¡ Exception subscription started - Listening for events...');
        
    } else if (message.type === 'next' && message.payload && message.payload.data) {
        const eventData = message.payload.data.exceptionUpdated;
        if (eventData) {
            console.log('ðŸ”” REAL-TIME EVENT RECEIVED:');
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
    console.log('âŒ WebSocket error:', error.message);
});

ws.on('close', (code, reason) => {
    console.log('ðŸ”Œ WebSocket closed:', code, reason.toString());
    console.log('ðŸ“Š Total messages received:', messageCount);
    console.log('ðŸ“¡ Subscription was active:', subscriptionActive);
});

// Keep connection alive for 30 seconds to catch events
setTimeout(() => {
    console.log('â° Test completed - Closing connection');
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
}, 30000);

console.log('â³ Listening for 30 seconds...');
