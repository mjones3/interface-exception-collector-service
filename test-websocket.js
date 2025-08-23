const WebSocket = require('ws');

const ws = new WebSocket('ws://localhost:8080/graphql-subscriptions', {
  headers: {
    'Authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTU4Mjc2NDAsImV4cCI6MTc1NTgzMTI0MH0.eII2qwVI2-7-vY4Nq5NWTI486SQs7qQ4sFxQcJAd4G0'
  }
});

ws.on('open', function open() {
  console.log('Connected to WebSocket');
  
  // Send connection init
  ws.send(JSON.stringify({
    type: 'connection_init',
    payload: {}
  }));
});

ws.on('message', function message(data) {
  console.log('Received:', data.toString());
  
  const msg = JSON.parse(data.toString());
  
  // After receiving connection_ack, start subscription
  if (msg.type === 'connection_ack') {
    console.log('Connection acknowledged, starting subscription...');
    
    ws.send(JSON.stringify({
      id: '1',
      type: 'start',
      payload: {
        query: 'subscription { exceptionUpdated { transactionId status } }'
      }
    }));
  }
});

ws.on('error', function error(err) {
  console.error('WebSocket error:', err);
});

ws.on('close', function close() {
  console.log('WebSocket connection closed');
});