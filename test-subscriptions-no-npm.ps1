#!/usr/bin/env pwsh

# Test GraphQL subscriptions without npm dependencies

Write-Host "Testing GraphQL Subscriptions (No npm required)" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green

Write-Host "`n🎯 Method 1: GraphiQL (Recommended)" -ForegroundColor Cyan
Write-Host "1. Start your service:"
Write-Host "   cd interface-exception-collector"
Write-Host "   mvn spring-boot:run -Dspring-boot.run.profiles=local"
Write-Host ""
Write-Host "2. Open GraphiQL in your browser:"
Write-Host "   http://localhost:8080/graphiql"
Write-Host ""
Write-Host "3. Test this subscription in GraphiQL:"

$subscription = @"
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
"@

Write-Host $subscription -ForegroundColor Gray

Write-Host "`n🎯 Method 2: HTML Test Client (No dependencies)" -ForegroundColor Cyan
Write-Host "1. Open graphql-subscription-test.html in your browser"
Write-Host "2. Click Connect"
Write-Host "3. Subscribe to events"
Write-Host "4. Watch real-time updates"

Write-Host "`n🎯 Method 3: PowerShell WebSocket Test" -ForegroundColor Cyan
Write-Host "Using built-in .NET WebSocket client:"

# Create a simple PowerShell WebSocket test
$psWebSocketTest = @"
# PowerShell WebSocket Test (requires PowerShell 6+)
`$uri = "ws://localhost:8080/subscriptions"
`$ws = New-Object System.Net.WebSockets.ClientWebSocket
`$ct = [System.Threading.CancellationToken]::None

try {
    `$connectTask = `$ws.ConnectAsync(`$uri, `$ct)
    `$connectTask.Wait()
    Write-Host "Connected to WebSocket"
    
    # Send connection init
    `$initMsg = '{"type":"connection_init"}'
    `$bytes = [System.Text.Encoding]::UTF8.GetBytes(`$initMsg)
    `$buffer = New-Object System.ArraySegment[byte] -ArgumentList @(,`$bytes)
    `$sendTask = `$ws.SendAsync(`$buffer, [System.Net.WebSockets.WebSocketMessageType]::Text, `$true, `$ct)
    `$sendTask.Wait()
    
    Write-Host "Sent connection init"
} catch {
    Write-Host "Error: `$_"
} finally {
    `$ws.Dispose()
}
"@

Write-Host $psWebSocketTest -ForegroundColor Gray

Write-Host "`n🔧 Fix npm Certificate Issue (Optional):" -ForegroundColor Yellow
Write-Host "If you want to use Node.js later, try these fixes:"
Write-Host ""
Write-Host "1. Use different registry:"
Write-Host "   npm config set registry https://registry.npmjs.org/"
Write-Host ""
Write-Host "2. Disable SSL verification (corporate networks):"
Write-Host "   npm config set strict-ssl false"
Write-Host ""
Write-Host "3. Set corporate proxy (if applicable):"
Write-Host "   npm config set proxy http://your-proxy:port"
Write-Host "   npm config set https-proxy http://your-proxy:port"

Write-Host "`n🚀 Quick Start (No npm needed):" -ForegroundColor Green
Write-Host "1. Start service: cd interface-exception-collector && mvn spring-boot:run -Dspring-boot.run.profiles=local"
Write-Host "2. Open: http://localhost:8080/graphiql"
Write-Host "3. Click the 'Subscription' tab in GraphiQL"
Write-Host "4. Paste the subscription query above"
Write-Host "5. Click the play button"
Write-Host "6. Watch for real-time events!"

Write-Host "`n💡 Pro Tip:" -ForegroundColor Magenta
Write-Host "GraphiQL has everything built-in - subscriptions, syntax highlighting, auto-complete!"
Write-Host "It's the best way to test GraphQL subscriptions without any setup."