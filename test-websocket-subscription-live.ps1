# Live WebSocket subscription test to catch events in real-time

Add-Type -AssemblyName System.Net.WebSockets
Add-Type -AssemblyName System.Threading.Tasks

Write-Host "🔌 Live WebSocket Subscription Test" -ForegroundColor Blue
Write-Host "===================================" -ForegroundColor Blue

# Generate token
$tokenOutput = node generate-jwt-correct-secret.js 2>$null
$token = ($tokenOutput -split "`n")[-1].Trim()
Write-Host "🔑 Token: $($token.Substring(0, 20))..." -ForegroundColor Cyan

# Connect to WebSocket
$uri = "ws://localhost:8080/graphql"
Write-Host "🔗 Connecting to: $uri" -ForegroundColor Cyan

$ws = $null
try {
    $ws = New-Object System.Net.WebSockets.ClientWebSocket
    $ws.Options.SetRequestHeader("Authorization", "Bearer $token")
    
    # Connect
    $connectTask = $ws.ConnectAsync([System.Uri]::new($uri), [System.Threading.CancellationToken]::None)
    $connectTask.Wait(5000)
    
    if ($ws.State -eq [System.Net.WebSockets.WebSocketState]::Open) {
        Write-Host "✅ WebSocket connected!" -ForegroundColor Green
        
        # Send connection_init
        $initMessage = '{"type":"connection_init"}'
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($initMessage)
        $buffer = New-Object System.ArraySegment[byte] -ArgumentList @(,$bytes)
        
        Write-Host "📤 Sending connection_init..." -ForegroundColor Yellow
        $sendTask = $ws.SendAsync($buffer, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None)
        $sendTask.Wait(2000)
        
        # Wait for connection_ack
        $receiveBuffer = New-Object byte[] 4096
        $receiveSegment = New-Object System.ArraySegment[byte] -ArgumentList @(,$receiveBuffer)
        
        Write-Host "👂 Waiting for connection_ack..." -ForegroundColor Yellow
        $receiveTask = $ws.ReceiveAsync($receiveSegment, [System.Threading.CancellationToken]::None)
        
        if ($receiveTask.Wait(5000)) {
            $result = $receiveTask.Result
            $receivedBytes = $receiveBuffer[0..($result.Count-1)]
            $receivedMessage = [System.Text.Encoding]::UTF8.GetString($receivedBytes)
            
            Write-Host "📨 Received: $receivedMessage" -ForegroundColor Green
            
            if ($receivedMessage -like '*connection_ack*') {
                Write-Host "✅ Connection acknowledged!" -ForegroundColor Green
                
                # Start subscription
                $subscriptionMessage = @'
{
  "id": "live-test-sub",
  "type": "start",
  "payload": {
    "query": "subscription { exceptionUpdated { eventType exception { transactionId status severity exceptionReason interfaceType } timestamp triggeredBy } }"
  }
}
'@
                
                $subBytes = [System.Text.Encoding]::UTF8.GetBytes($subscriptionMessage)
                $subBuffer = New-Object System.ArraySegment[byte] -ArgumentList @(,$subBytes)
                
                Write-Host "📡 Starting subscription..." -ForegroundColor Yellow
                $subSendTask = $ws.SendAsync($subBuffer, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None)
                $subSendTask.Wait(2000)
                
                Write-Host "🎧 LISTENING FOR EVENTS..." -ForegroundColor Magenta
                Write-Host "   (Run debug-event-publishing-flow.ps1 in another terminal)" -ForegroundColor Gray
                Write-Host "   Press Ctrl+C to stop" -ForegroundColor Gray
                Write-Host ""
                
                # Listen for events indefinitely
                $eventCount = 0
                while ($ws.State -eq [System.Net.WebSockets.WebSocketState]::Open) {
                    try {
                        $eventReceiveTask = $ws.ReceiveAsync($receiveSegment, [System.Threading.CancellationToken]::None)
                        
                        if ($eventReceiveTask.Wait(1000)) {
                            $eventResult = $eventReceiveTask.Result
                            $eventBytes = $receiveBuffer[0..($eventResult.Count-1)]
                            $eventMessage = [System.Text.Encoding]::UTF8.GetString($eventBytes)
                            
                            $eventCount++
                            $timestamp = Get-Date -Format "HH:mm:ss"
                            
                            Write-Host "🔔 [$timestamp] Event #$eventCount received:" -ForegroundColor Magenta
                            
                            # Try to parse and display nicely
                            try {
                                $eventData = $eventMessage | ConvertFrom-Json
                                if ($eventData.type -eq "next" -and $eventData.payload.data.exceptionUpdated) {
                                    $update = $eventData.payload.data.exceptionUpdated
                                    Write-Host "   Type: $($update.eventType)" -ForegroundColor Cyan
                                    Write-Host "   Transaction: $($update.exception.transactionId)" -ForegroundColor Cyan
                                    Write-Host "   Status: $($update.exception.status)" -ForegroundColor Cyan
                                    Write-Host "   Severity: $($update.exception.severity)" -ForegroundColor Cyan
                                    Write-Host "   Reason: $($update.exception.exceptionReason)" -ForegroundColor Gray
                                    Write-Host "   Triggered By: $($update.triggeredBy)" -ForegroundColor Gray
                                    Write-Host ""
                                    
                                    if ($update.exception.transactionId -like "debug-flow-*") {
                                        Write-Host "🎉 CAUGHT OUR DEBUG EVENT!" -ForegroundColor Green
                                        Write-Host "   This proves the event publishing is working!" -ForegroundColor Green
                                        Write-Host ""
                                    }
                                } else {
                                    Write-Host "   Raw: $eventMessage" -ForegroundColor Gray
                                }
                            } catch {
                                Write-Host "   Raw: $eventMessage" -ForegroundColor Gray
                            }
                        }
                        
                        Start-Sleep -Milliseconds 100
                    } catch {
                        Write-Host "⚠️ Receive error: $_" -ForegroundColor Yellow
                        break
                    }
                }
                
                Write-Host "`n📊 Total events received: $eventCount" -ForegroundColor Blue
                
            } else {
                Write-Host "❌ Unexpected response: $receivedMessage" -ForegroundColor Red
            }
        } else {
            Write-Host "⏰ No connection_ack received" -ForegroundColor Yellow
        }
        
    } else {
        Write-Host "❌ WebSocket connection failed - State: $($ws.State)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ WebSocket error: $_" -ForegroundColor Red
    Write-Host "Exception: $($_.Exception.GetType().Name)" -ForegroundColor Red
} finally {
    if ($ws -and $ws.State -eq [System.Net.WebSockets.WebSocketState]::Open) {
        try {
            $closeTask = $ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "Test complete", [System.Threading.CancellationToken]::None)
            $closeTask.Wait(2000)
        } catch {
            # Ignore close errors
        }
    }
    if ($ws) {
        $ws.Dispose()
    }
}

Write-Host "`n💡 If no events were received:" -ForegroundColor Yellow
Write-Host "1. Check application logs for publishing messages" -ForegroundColor White
Write-Host "2. Verify the event publishing chain is working" -ForegroundColor White
Write-Host "3. Check if there are active subscribers in the logs" -ForegroundColor White