#!/usr/bin/env node

/**
 * Node.js GraphQL Subscription Test Client
 * 
 * Usage:
 *   npm install ws
 *   node test-subscriptions.js
 */

const WebSocket = require('ws');

class GraphQLSubscriptionClient {
    constructor(url, options = {}) {
        this.url = url;
        this.options = options;
        this.ws = null;
        this.subscriptions = new Map();
        this.subscriptionId = 1;
    }

    connect() {
        return new Promise((resolve, reject) => {
            console.log(`🔌 Connecting to ${this.url}...`);
            
            const wsOptions = {
                headers: {
                    'Sec-WebSocket-Protocol': 'graphql-ws'
                }
            };

            if (this.options.token) {
                wsOptions.headers['Authorization'] = `Bearer ${this.options.token}`;
            }

            this.ws = new WebSocket(this.url, wsOptions);

            this.ws.on('open', () => {
                console.log('✅ Connected to GraphQL subscriptions');
                
                // Send connection init
                this.send({ type: 'connection_init' });
                resolve();
            });

            this.ws.on('message', (data) => {
                const message = JSON.parse(data);
                this.handleMessage(message);
            });

            this.ws.on('close', () => {
                console.log('❌ WebSocket connection closed');
                this.subscriptions.clear();
            });

            this.ws.on('error', (error) => {
                console.error('🚨 WebSocket error:', error);
                reject(error);
            });
        });
    }

    handleMessage(message) {
        console.log('📨 Received:', JSON.stringify(message, null, 2));

        switch (message.type) {
            case 'connection_ack':
                console.log('🤝 Connection acknowledged');
                break;
            case 'data':
                console.log('📊 Subscription data:', JSON.stringify(message.payload, null, 2));
                break;
            case 'error':
                console.error('❌ Subscription error:', message.payload);
                break;
            case 'complete':
                console.log('✅ Subscription completed:', message.id);
                this.subscriptions.delete(message.id);
                break;
        }
    }

    send(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        } else {
            console.error('❌ WebSocket not connected');
        }
    }

    subscribe(query, variables = {}) {
        const id = `sub_${this.subscriptionId++}`;
        
        const message = {
            id,
            type: 'start',
            payload: {
                query,
                variables
            }
        };

        this.send(message);
        this.subscriptions.set(id, { query, variables });
        
        console.log(`📡 Subscribed with ID: ${id}`);
        console.log(`Query: ${query}`);
        
        return id;
    }

    unsubscribe(id) {
        this.send({
            id,
            type: 'stop'
        });
        
        this.subscriptions.delete(id);
        console.log(`🛑 Unsubscribed: ${id}`);
    }

    disconnect() {
        if (this.ws) {
            this.ws.close();
        }
    }
}

// Test subscriptions
async function testSubscriptions() {
    const client = new GraphQLSubscriptionClient('ws://localhost:8080/subscriptions');

    try {
        await client.connect();

        // Test 1: Subscribe to exception updates
        console.log('\n🧪 Test 1: Exception Updates Subscription');
        const exceptionSub = client.subscribe(`
            subscription {
                exceptionUpdated {
                    eventType
                    exception {
                        transactionId
                        status
                        severity
                        interfaceType
                    }
                    timestamp
                    triggeredBy
                }
            }
        `);

        // Test 2: Subscribe to retry status updates
        console.log('\n🧪 Test 2: Retry Status Subscription');
        const retrySub = client.subscribe(`
            subscription {
                retryStatusUpdated {
                    transactionId
                    eventType
                    timestamp
                    retryAttempt {
                        attemptNumber
                        status
                    }
                }
            }
        `);

        // Test 3: Subscribe to summary updates
        console.log('\n🧪 Test 3: Summary Updates Subscription');
        const summarySub = client.subscribe(`
            subscription {
                summaryUpdated(timeRange: LAST_HOUR) {
                    totalExceptions
                    keyMetrics {
                        retrySuccessRate
                        criticalExceptionCount
                    }
                }
            }
        `);

        console.log('\n⏰ Listening for events... (Press Ctrl+C to exit)');
        console.log('💡 Tip: Trigger events by creating exceptions or retries in another terminal');

        // Keep the connection alive
        process.on('SIGINT', () => {
            console.log('\n🛑 Shutting down...');
            client.unsubscribe(exceptionSub);
            client.unsubscribe(retrySub);
            client.unsubscribe(summarySub);
            client.disconnect();
            process.exit(0);
        });

    } catch (error) {
        console.error('❌ Failed to connect:', error);
        process.exit(1);
    }
}

// Helper function to test with authentication
async function testWithAuth(token) {
    const client = new GraphQLSubscriptionClient('ws://localhost:8080/subscriptions', { token });
    
    try {
        await client.connect();
        
        const sub = client.subscribe(`
            subscription {
                exceptionUpdated {
                    eventType
                    exception { transactionId status }
                    timestamp
                }
            }
        `);

        console.log('🔐 Authenticated subscription active...');
        
    } catch (error) {
        console.error('❌ Authentication failed:', error);
    }
}

// Command line interface
if (require.main === module) {
    const args = process.argv.slice(2);
    
    if (args.includes('--help') || args.includes('-h')) {
        console.log(`
GraphQL Subscription Test Client

Usage:
  node test-subscriptions.js [options]

Options:
  --token <jwt>     Use JWT token for authentication
  --help, -h        Show this help message

Examples:
  node test-subscriptions.js
  node test-subscriptions.js --token eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Make sure the interface-exception-collector service is running on localhost:8080
        `);
        process.exit(0);
    }

    const tokenIndex = args.indexOf('--token');
    if (tokenIndex !== -1 && args[tokenIndex + 1]) {
        const token = args[tokenIndex + 1];
        testWithAuth(token);
    } else {
        testSubscriptions();
    }
}

module.exports = GraphQLSubscriptionClient;