const WebSocket = require('ws');

// Get token from command line or use a default
const token = process.argv[2] || 'your-token-here';
const wsUrl = 'ws://localhost:8080/graphql';

console.log('🔌 Connecting to WebSocket:', wsUrl);

const ws = new WebSocket(wsUrl, {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});

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
    console.log('📨 Received:', JSON.stringify(message, null, 2));
    
    if (message.type === 'connection_ack') {
        console.log('✅ Connection acknowledged, starting subscription...');
        
        // Start subscription
        ws.send(JSON.stringify({
            id: 'test-sub',
            type: 'start',
            payload: {
                query: 'subscription { exceptionEvents { eventType exception { transactionId } } }'
            }
        }));
        
        // Close after 5 seconds
        setTimeout(() => {
            console.log('⏰ Closing connection after 5 seconds');
            ws.close();
        }, 5000);
    }
});

ws.on('error', (error) => {
    console.log('❌ WebSocket error:', error.message);
});

ws.on('close', (code, reason) => {
    console.log('🔌 WebSocket closed:', code, reason.toString());
});