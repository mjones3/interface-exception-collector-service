const WebSocket = require('ws');

const token = process.argv[2] || 'your-token-here';
const wsUrl = 'ws://localhost:8080/graphql';

console.log('🎧 LIVE EXCEPTION LISTENER');
console.log('==========================');
console.log('🔌 Connecting to:', wsUrl);
console.log('⏳ Waiting for real exception events...');
console.log('📝 Press Ctrl+C to stop listening');
console.log('');

let connectionTime = new Date();
let eventsReceived = 0;
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;

function connect() {
    const ws = new WebSocket(wsUrl, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    ws.on('open', () => {
        reconnectAttempts = 0;
        console.log(`✅ [${new Date().toLocaleTimeString()}] WebSocket connected`);

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

        if (message.type === 'connection_ack') {
            console.log(`🔗 [${new Date().toLocaleTimeString()}] Connection acknowledged - Starting subscription...`);

            // Start exception subscription
            ws.send(JSON.stringify({
                id: 'live-exception-sub',
                type: 'start',
                payload: {
                    query: `subscription { 
                        exceptionUpdated { 
                            eventType 
                            exception { 
                                transactionId 
                                externalId
                                interfaceType
                                exceptionReason 
                                severity 
                                status
                                customerId
                                retryable
                                retryCount
                            } 
                            timestamp 
                            triggeredBy 
                        } 
                    }`
                }
            }));

            console.log(`📡 [${new Date().toLocaleTimeString()}] Subscription active - Listening for real events...`);
            console.log('');

        } else if (message.type === 'next' && message.payload && message.payload.data) {
            const eventData = message.payload.data.exceptionUpdated;
            if (eventData) {
                eventsReceived++;
                const timestamp = new Date().toLocaleTimeString();

                console.log(`🚨 [${timestamp}] EXCEPTION EVENT #${eventsReceived}`);
                console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
                console.log(`📋 Event Type: ${eventData.eventType}`);
                console.log(`🆔 Transaction ID: ${eventData.exception.transactionId}`);

                if (eventData.exception.externalId) {
                    console.log(`🔗 External ID: ${eventData.exception.externalId}`);
                }

                console.log(`🔧 Interface: ${eventData.exception.interfaceType}`);
                console.log(`❌ Reason: ${eventData.exception.exceptionReason}`);
                console.log(`⚠️  Severity: ${eventData.exception.severity}`);
                console.log(`📊 Status: ${eventData.exception.status}`);

                if (eventData.exception.customerId) {
                    console.log(`👤 Customer: ${eventData.exception.customerId}`);
                }

                console.log(`🔄 Retryable: ${eventData.exception.retryable ? 'Yes' : 'No'}`);

                if (eventData.exception.retryCount > 0) {
                    console.log(`🔢 Retry Count: ${eventData.exception.retryCount}`);
                }

                console.log(`⏰ Event Time: ${eventData.timestamp}`);
                console.log(`👤 Triggered By: ${eventData.triggeredBy || 'system'}`);
                console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
                console.log('');
            }
        } else if (message.type === 'error') {
            console.log(`❌ [${new Date().toLocaleTimeString()}] Subscription error:`, message.payload);
        }
    });

    ws.on('error', (error) => {
        console.log(`❌ [${new Date().toLocaleTimeString()}] WebSocket error:`, error.message);

        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++;
            console.log(`🔄 [${new Date().toLocaleTimeString()}] Attempting to reconnect (${reconnectAttempts}/${maxReconnectAttempts})...`);
            setTimeout(() => connect(), 5000);
        } else {
            console.log(`💀 [${new Date().toLocaleTimeString()}] Max reconnection attempts reached. Exiting.`);
            process.exit(1);
        }
    });

    ws.on('close', (code, reason) => {
        const uptime = Math.round((new Date() - connectionTime) / 1000);
        console.log(`🔌 [${new Date().toLocaleTimeString()}] WebSocket closed: ${code} ${reason.toString()}`);
        console.log(`📊 Session stats: ${eventsReceived} events received in ${uptime} seconds`);

        if (code !== 1000 && reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++;
            console.log(`🔄 [${new Date().toLocaleTimeString()}] Attempting to reconnect (${reconnectAttempts}/${maxReconnectAttempts})...`);
            setTimeout(() => connect(), 3000);
        }
    });

    // Heartbeat to keep connection alive
    const heartbeat = setInterval(() => {
        if (ws.readyState === WebSocket.OPEN) {
            ws.ping();
        } else {
            clearInterval(heartbeat);
        }
    }, 30000);

    return ws;
}

// Handle graceful shutdown
process.on('SIGINT', () => {
    const uptime = Math.round((new Date() - connectionTime) / 1000);
    console.log('');
    console.log(`🛑 [${new Date().toLocaleTimeString()}] Shutting down listener...`);
    console.log(`📊 Final stats: ${eventsReceived} events received in ${uptime} seconds`);
    console.log('👋 Goodbye!');
    process.exit(0);
});

// Start the connection
connect();