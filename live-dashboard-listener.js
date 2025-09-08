const WebSocket = require('ws');

// Get JWT token from command line argument
const token = process.argv[2];

if (!token) {
    console.log('❌ Please provide JWT token as argument');
    console.log('Usage: node live-dashboard-listener.js <jwt-token>');
    process.exit(1);
}

console.log('📊 LIVE DASHBOARD SUBSCRIPTION LISTENER');
console.log('=======================================');
console.log('🔗 Connecting to: ws://localhost:8080/graphql');
console.log('');

const ws = new WebSocket('ws://localhost:8080/graphql', {
    headers: { 'Authorization': 'Bearer ' + token }
});

let dashboardUpdatesReceived = 0;
let connectionEstablished = false;
let subscriptionStarted = false;

// Store last dashboard state for comparison
let lastDashboard = null;

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
    
    if (message.type === 'connection_ack') {
        console.log('✅ Connection acknowledged - starting dashboard subscription');
        connectionEstablished = true;
        
        console.log('📤 Sending dashboard subscription request...');
        ws.send(JSON.stringify({
            id: 'live-dashboard',
            type: 'start',
            payload: {
                query: `subscription { 
                    dashboardSummary { 
                        activeExceptions 
                        todayExceptions 
                        failedRetries 
                        successfulRetries 
                        totalRetries 
                        retrySuccessRate 
                        apiSuccessRate 
                        totalApiCallsToday 
                        lastUpdated 
                    } 
                }`
            }
        }));
        
        subscriptionStarted = true;
        console.log('📊 Dashboard subscription started - waiting for updates...');
        console.log('');
        console.log('🎯 Dashboard will update automatically every 30 seconds');
        console.log('🔄 Or immediately when exceptions/retries occur');
        console.log('');
        
    } else if (message.type === 'next' && message.payload && message.payload.data) {
        
        if (message.payload.data.dashboardSummary) {
            dashboardUpdatesReceived++;
            const dashboard = message.payload.data.dashboardSummary;
            
            // Clear screen and show updated dashboard
            console.clear();
            console.log('📊 LIVE DASHBOARD - UPDATE #' + dashboardUpdatesReceived);
            console.log('═══════════════════════════════════════════════');
            console.log('');
            
            // Show metrics in a nice format
            console.log('🚨 ACTIVE EXCEPTIONS');
            console.log('   Current Active: ' + dashboard.activeExceptions);
            console.log('   Today Total: ' + dashboard.todayExceptions);
            console.log('');
            
            console.log('🔄 RETRY STATISTICS');
            console.log('   ✅ Successful: ' + dashboard.successfulRetries);
            console.log('   ❌ Failed: ' + dashboard.failedRetries);
            console.log('   📊 Total: ' + dashboard.totalRetries);
            console.log('   📈 Success Rate: ' + dashboard.retrySuccessRate + '%');
            console.log('');
            
            console.log('🎯 API PERFORMANCE');
            console.log('   📞 Total Calls Today: ' + dashboard.totalApiCallsToday);
            console.log('   ✅ Success Rate: ' + dashboard.apiSuccessRate + '%');
            console.log('');
            
            console.log('🕐 LAST UPDATED: ' + new Date(dashboard.lastUpdated).toLocaleString());
            console.log('');
            
            // Show changes if this is not the first update
            if (lastDashboard && dashboardUpdatesReceived > 1) {
                console.log('📈 CHANGES FROM LAST UPDATE:');
                
                const changes = [];
                if (dashboard.activeExceptions !== lastDashboard.activeExceptions) {
                    const diff = dashboard.activeExceptions - lastDashboard.activeExceptions;
                    changes.push(`Active Exceptions: ${diff > 0 ? '+' : ''}${diff}`);
                }
                if (dashboard.todayExceptions !== lastDashboard.todayExceptions) {
                    const diff = dashboard.todayExceptions - lastDashboard.todayExceptions;
                    changes.push(`Today Exceptions: ${diff > 0 ? '+' : ''}${diff}`);
                }
                if (dashboard.totalRetries !== lastDashboard.totalRetries) {
                    const diff = dashboard.totalRetries - lastDashboard.totalRetries;
                    changes.push(`Total Retries: ${diff > 0 ? '+' : ''}${diff}`);
                }
                if (dashboard.retrySuccessRate !== lastDashboard.retrySuccessRate) {
                    const diff = dashboard.retrySuccessRate - lastDashboard.retrySuccessRate;
                    changes.push(`Retry Success Rate: ${diff > 0 ? '+' : ''}${diff.toFixed(2)}%`);
                }
                
                if (changes.length > 0) {
                    changes.forEach(change => console.log('   ' + change));
                } else {
                    console.log('   No changes');
                }
                console.log('');
            }
            
            console.log('═══════════════════════════════════════════════');
            console.log('ℹ️ Dashboard updates automatically. Press Ctrl+C to stop.');
            
            // Store current state for next comparison
            lastDashboard = { ...dashboard };
        }
        
    } else if (message.type === 'error') {
        console.log('❌ Subscription error:');
        console.log(JSON.stringify(message.payload, null, 2));
        
    } else if (message.type === 'complete') {
        console.log('✅ Subscription completed');
    }
});

ws.on('error', (error) => {
    console.log('❌ WebSocket error:', error.message);
});

ws.on('close', (code, reason) => {
    console.log('');
    console.log('🔌 WebSocket connection closed');
    console.log('   Code:', code);
    console.log('   Reason:', reason.toString());
    console.log('');
    console.log('📊 SESSION SUMMARY:');
    console.log('   Connection Established:', connectionEstablished);
    console.log('   Subscription Started:', subscriptionStarted);
    console.log('   Dashboard Updates Received:', dashboardUpdatesReceived);
    console.log('');
    
    if (connectionEstablished && subscriptionStarted) {
        if (dashboardUpdatesReceived > 0) {
            console.log('✅ DASHBOARD SUBSCRIPTION: WORKING (' + dashboardUpdatesReceived + ' updates received)');
        } else {
            console.log('⚠️ DASHBOARD SUBSCRIPTION: CONNECTED BUT NO UPDATES');
        }
    } else {
        console.log('❌ DASHBOARD SUBSCRIPTION: CONNECTION OR SETUP FAILED');
    }
});

// Keep connection alive indefinitely (until manually stopped)
console.log('ℹ️ Dashboard will run indefinitely. Press Ctrl+C to stop.');
console.log('');

// Handle graceful shutdown
process.on('SIGINT', () => {
    console.log('');
    console.log('🛑 Shutting down dashboard...');
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
    process.exit(0);
});

process.on('SIGTERM', () => {
    console.log('');
    console.log('🛑 Terminating dashboard...');
    if (ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
    process.exit(0);
});