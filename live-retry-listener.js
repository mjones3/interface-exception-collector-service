const WebSocket = require('ws');

// Get JWT token from command line argument
const token = process.argv[2];

if (!token) {
    console.log('❌ Please provide JWT token as argument');
    console.log('Usage: node live-retry-listener.js <jwt-token>');
    process.exit(1);
}

console.log('🔄 LIVE RETRY SUBSCRIPTION LISTENER');
console.log('===================================');
console.log('📋 Token length:', token.length);
console.log('🔗 Connecting to: ws://localhost:8080/graphql');
console.log('');

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: { 'Authorization': 'Bearer ' + token }
});

let retryEventsReceived = 0;
let connectionEstablished = false;
let subscriptionStarted = false;

ws.on('open', () => {
    console.log('✅ WebSocket connection opened');
    console.log('📤 Sending connection_init...');
    
    ws.send(JSON.stringify({
        type: 'connection_init',
        payload: { Authorization: 'Bearer ' + token }
    }));
});

ws.on('message', (data) => {
    const message = JSON.parse(data.toString());
    
    console.log('📨 RAW MESSAGE RECEIVED:');
    console.log('========================');
    console.log(JSON.stringify(message, null, 2));
    console.log('========================');
    console.log('');
    
    if (message.type === 'connection_ack') {
        console.log('✅ Connection acknowledged - starting retry subscription');
        connectionEstablished = true;
        
        console.log('📤 Sending retry subscription request...');
        ws.send(JSON.stringify({
            id: 'live-retry-listener',
            type: 'start',
            payload: {
                query: 'subscription { retryStatusUpdated { transactionId retryAttempt { attemptNumber } eventType timestamp } }'
            }
        }));
        
        subscriptionStarted = true;
        console.log('📡 Retry subscription started - waiting for events...');
        console.log('');
        console.log('🎯 Ready to receive retry events! Trigger some events now.');
        console.log('');
        
    } else if (message.type === 'next' && message.payload && message.payload.data) {
        
        // Check specifically for retry events
        if (message.payload.data.retryStatusUpdated) {
            retryEventsReceived++;
            const retryData = message.payload.data.retryStatusUpdated;
            
            console.log('🔄 RETRY EVENT #' + retryEventsReceived + ' RECEIVED:');
            console.log('   Transaction ID:', retryData.transactionId);
            console.log('   Attempt Number:', retryData.retryAttempt.attemptNumber);
            console.log('   Event Type:', retryData.eventType);
            console.log('   Timestamp:', retryData.timestamp);
            console.log('');
            
        } else {
            console.log('📨 Other event received (not retry):');
            console.log('   Event type:', Object.keys(message.payload.data)[0]);
            console.log('');
        }
        
    } else if (message.type === 'error') {
        console.log('❌ Subscription error:');
        console.log(JSON.stringify(message.payload, null, 2));
        console.log('');
        
    } else if (message.type === 'complete') {
        console.log('✅ Subscription completed');
        console.log('');
    }
});

ws.on('error', (error) => {
    console.log('❌ WebSocket error:', error.message);
    console.log('');
});

ws.on('close', (code, reason) => {
    console.log('🔌 WebSocket connection closed');
    console.log('   Code:', code);
    console.log('   Reason:', reason.toString());
    console.log('');
    console.log('📊 SESSION SUMMARY:');
    console.log('   Connection Established:', connectionEstablished);
    console.log('   Subscription Started:', subscriptionStarted);
    console.log('   Retry Events Received:', retryEventsReceived);
    console.log('');
    
    if (connectionEstablished && subscriptionStarted) {
        if (retryEventsReceived > 0) {
            console.log('✅ RETRY SUBSCRIPTION: WORKING (' + retryEventsReceived + ' events received)');
        } else {
            console.log('⚠️ RETRY SUBSCRIPTION: CONNECTED BUT NO EVENTS RECEIVED');
        }
    } else {
        console.log('❌ RETRY SUBSCRIPTION: CONNECTION OR SETUP FAILED');
    }
});

// Keep connection alive indefinitely (until manually stopped)
console.log('ℹ️ Listener will run indefinitely. Press Ctrl+C to stop.');
console.log('');

// Handle graceful shutdown
process.on('SIGINT', () => {
    console.log('');
    console.log('🛑 Shutting down listener...');
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
    process.exit(0);
});

process.on('SIGTERM', () => {
    console.log('');
    console.log('🛑 Terminating listener...');
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
    process.exit(0);
});