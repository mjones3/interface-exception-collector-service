# Run WebSocket Test with Token
Write-Host "RUNNING WEBSOCKET TEST" -ForegroundColor Blue

# Generate JWT
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
Write-Host "Generated JWT token" -ForegroundColor Green

# Run WebSocket test with token
Write-Host "Testing WebSocket subscription..." -ForegroundColor Cyan
node test-websocket.js $token

Write-Host "WEBSOCKET TEST COMPLETE" -ForegroundColor Blue