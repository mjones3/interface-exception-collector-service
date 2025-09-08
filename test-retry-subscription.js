const WebSocket = require('ws');

const token = process.argv[2] || 'your-token-here';
const wsUrl = 'ws://localhost:8080/graphql';

console.log('🔄 RETRY SUBSCRIPTION TEST');
console.log('==========================');
console.log('🔌 Connecting to:', wsUrl);

const ws = new WebSocket(wsUrl, {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});

let messageCount = 0;
let retryEventsReceived = 0;

ws.on('open', () => {
    console.log('✅ WebSocket connected');
    
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
    
    console.log(`📨 RAW MESSAGE ${messageCount}:`);
    console.log(JSON.stringify(message, null, 2));
    console.log('');
    
    if (message.type === 'connection_ack') {
        console.log('✅ Connection acknowledged - Starting retry subscription...');
        
        // Start retry status subscription
        ws.send(JSON.stringify({
            id: 'retry-sub',
            type: 'start',
            payload: {
                query: `subscription { 
                    retryStatusUpdated { 
                        transactionId
                        retryAttempt {
                            attemptNumber
                        }
                        eventType 
                        timestamp 
                    } 
                }`
            }
        }));
        
        console.log('📡 Retry subscription started - Listening for retry events...');
        console.log('');
        
    } else if (message.type === 'next' && message.payload && message.payload.data) {
        const retryData = message.payload.data.retryStatusUpdated;
        if (retryData) {
            retryEventsReceived++;
            console.log(`🔄 RETRY EVENT #${retryEventsReceived} RECEIVED:`);
            console.log('   Transaction ID:', retryData.transactionId);
            console.log('   Attempt Number:', retryData.retryAttempt.attemptNumber);
            console.log('   Event Type:', retryData.eventType);
            console.log('   Timestamp:', retryData.timestamp);
            console.log('');
        }
    } else if (message.type === 'error') {
        console.log('❌ SUBSCRIPTION ERROR:');
        console.log(JSON.stringify(message.payload, null, 2));
    }
});

ws.on('error', (error) => {
    console.log('❌ WebSocket error:', error.message);
});

ws.on('close', (code, reason) => {
    console.log('🔌 WebSocket closed:', code, reason.toString());
    console.log('📊 FINAL STATS:');
    console.log('   Total messages:', messageCount);
    console.log('   Retry events received:', retryEventsReceived);
    
    if (retryEventsReceived > 0) {
        console.log('✅ RETRY SUBSCRIPTION: WORKING');
    } else {
        console.log('⚠️ RETRY SUBSCRIPTION: NO EVENTS RECEIVED');
    }
});

// Keep connection alive for 20 seconds
setTimeout(() => {
    console.log('⏰ Test completed - Closing connection');
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
}, 20000);

console.log('⏳ Listening for retry events for 20 seconds...');
console.log('');