const WebSocket = require('ws');

const token = process.argv[2] || 'no-token';
const wsUrl = 'ws://localhost:8080/graphql';

console.log('🔥 RAW EVENT MONITOR - STARTING');
console.log('================================');
console.log('Endpoint:', wsUrl);
console.log('Press Ctrl+C to stop');
console.log('');

const ws = new WebSocket(wsUrl, {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});

ws.on('open', () => {
    console.log('✅ CONNECTED');
    
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
    
    console.log('📨 RAW MESSAGE:', JSON.stringify(message, null, 2));
    
    if (message.type === 'connection_ack') {
        console.log('🔗 CONNECTION ACKNOWLEDGED - STARTING SUBSCRIPTION');
        
        // Start exception subscription
        ws.send(JSON.stringify({
            id: 'raw-sub',
            type: 'start',
            payload: {
                query: 'subscription { exceptionUpdated { eventType exception { transactionId exceptionReason severity status } timestamp triggeredBy } }'
            }
        }));
        
        console.log('📡 SUBSCRIPTION ACTIVE - WAITING FOR EVENTS...');
        console.log('');
    }
});

ws.on('error', (error) => {
    console.log('❌ ERROR:', error.message);
});

ws.on('close', (code, reason) => {
    console.log('🔌 CLOSED:', code, reason.toString());
});

// Keep alive
setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.ping();
    }
}, 30000);