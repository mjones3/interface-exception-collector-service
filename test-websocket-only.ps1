# Test WebSocket subscription without triggering new events
# This will help us see if the subscription mechanism itself works

Add-Type -AssemblyName System.Net.WebSockets
Add-Type -AssemblyName System.Threading.Tasks

Write-Host "WebSocket Subscription Test (No Event Triggering)" -ForegroundColor Blue
Write-Host "=================================================" -ForegroundColor Blue

# Generate token
$tokenOutput = node generate-jwt-correct-secret.js 2>$null
$token = ($tokenOutput -split "`n")[-1].Trim()
Write-Host "Token: $($token.Substring(0, 20))..." -ForegroundColor Cyan

# Connect to WebSocket
$uri = "ws://localhost:8080/graphql"
Write-Host "Connecting to: $uri" -ForegroundColor Cyan

$ws = $null
try {
    $ws = New-Object System.Net.WebSockets.ClientWebSocket
    $ws.Options.SetRequestHeader("Authorization", "Bearer $token")
    
    # Connect
    $connectTask = $ws.ConnectAsync([System.Uri]::new($uri), [System.Threading.CancellationToken]::None)
    $connectTask.Wait(5000)
    
    if ($ws.State -eq [System.Net.WebSockets.WebSocketState]::Open) {
        Write-Host "WebSocket connected successfully!" -ForegroundColor Green
        
        # Send connection_init
        $initMessage = '{"type":"connection_init"}'
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($initMessage)
        $buffer = New-Object System.ArraySegment[byte] -ArgumentList @(,$bytes)
        
        Write-Host "Sending connection_init..." -ForegroundColor Yellow
        $sendTask = $ws.SendAsync($buffer, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None)
        $sendTask.Wait(2000)
        
        # Wait for connection_ack
        $receiveBuffer = New-Object byte[] 4096
        $receiveSegment = New-Object System.ArraySegment[byte] -ArgumentList @(,$receiveBuffer)
        
        Write-Host "Waiting for connection_ack..." -ForegroundColor Yellow
        $receiveTask = $ws.ReceiveAsync($receiveSegment, [System.Threading.CancellationToken]::None)
        
        if ($receiveTask.Wait(5000)) {
            $result = $receiveTask.Result
            $receivedBytes = $receiveBuffer[0..($result.Count-1)]
            $receivedMessage = [System.Text.Encoding]::UTF8.GetString($receivedBytes)
            
            Write-Host "Received: $receivedMessage" -ForegroundColor Green
            
            if ($receivedMessage -like '*connection_ack*') {
                Write-Host "Connection acknowledged!" -ForegroundColor Green
                
                # Start subscription
                $subscriptionMessage = @'
{
  "id": "test-sub-1",
  "type": "start",
  "payload": {
    "query": "subscription { exceptionUpdated { eventType exception { transactionId status severity exceptionReason } timestamp triggeredBy } }"
  }
}
'@
                
                $subBytes = [System.Text.Encoding]::UTF8.GetBytes($subscriptionMessage)
                $subBuffer = New-Object System.ArraySegment[byte] -ArgumentList @(,$subBytes)
                
                Write-Host "Starting subscription..." -ForegroundColor Yellow
                $subSendTask = $ws.SendAsync($subBuffer, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None)
                $subSendTask.Wait(2000)
                
                Write-Host "Subscription started! Listening for 10 seconds..." -ForegroundColor Magenta
                Write-Host "(This will show if there are any existing events being published)" -ForegroundColor Gray
                Write-Host ""
                
                # Listen for events for 10 seconds
                $timeout = (Get-Date).AddSeconds(10)
                $eventCount = 0
                
                while ((Get-Date) -lt $timeout -and $ws.State -eq [System.Net.WebSockets.WebSocketState]::Open) {
                    try {
                        $eventReceiveTask = $ws.ReceiveAsync($receiveSegment, [System.Threading.CancellationToken]::None)
                        
                        if ($eventReceiveTask.Wait(1000)) {
                            $eventResult = $eventReceiveTask.Result
                            $eventBytes = $receiveBuffer[0..($eventResult.Count-1)]
                            $eventMessage = [System.Text.Encoding]::UTF8.GetString($eventBytes)
                            
                            $eventCount++
                            $timestamp = Get-Date -Format "HH:mm:ss"
                            
                            Write-Host "[$timestamp] Event #$eventCount received:" -ForegroundColor Magenta
                            Write-Host "  $eventMessage" -ForegroundColor Gray
                            Write-Host ""
                        }
                        
                        Start-Sleep -Milliseconds 100
                    } catch {
                        Write-Host "Receive error: $_" -ForegroundColor Yellow
                        break
                    }
                }
                
                Write-Host "Listening completed. Total events received: $eventCount" -ForegroundColor Blue
                
                if ($eventCount -eq 0) {
                    Write-Host ""
                    Write-Host "No events received during listening period." -ForegroundColor Yellow
                    Write-Host "This means either:" -ForegroundColor Yellow
                    Write-Host "1. No events are being published to the subscription" -ForegroundColor White
                    Write-Host "2. The subscription is not properly connected to the event stream" -ForegroundColor White
                    Write-Host "3. There are no active events being generated" -ForegroundColor White
                    Write-Host ""
                    Write-Host "Check application logs for:" -ForegroundColor Cyan
                    Write-Host "- 'DEBUG: Sink has subscribers: [N]' (should be > 0)" -ForegroundColor White
                    Write-Host "- 'Successfully published exception update event' messages" -ForegroundColor White
                    Write-Host "- Any error messages in ExceptionEventPublisher or ExceptionSubscriptionResolver" -ForegroundColor White
                } else {
                    Write-Host ""
                    Write-Host "SUCCESS! Events are being received via WebSocket!" -ForegroundColor Green
                    Write-Host "The subscription mechanism is working correctly." -ForegroundColor Green
                }
                
            } else {
                Write-Host "Unexpected response: $receivedMessage" -ForegroundColor Red
            }
        } else {
            Write-Host "No connection_ack received within timeout" -ForegroundColor Yellow
        }
        
    } else {
        Write-Host "WebSocket connection failed - State: $($ws.State)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "WebSocket error: $_" -ForegroundColor Red
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

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Check application logs for subscription-related messages" -ForegroundColor White
Write-Host "2. Look for 'DEBUG: Sink has subscribers:' messages" -ForegroundColor White
Write-Host "3. If subscribers = 0, the WebSocket subscription isn't connecting to the Flux" -ForegroundColor White
Write-Host "4. If subscribers > 0 but no events, check event publishing chain" -ForegroundColor White