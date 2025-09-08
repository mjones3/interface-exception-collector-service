# Start Live Exception Listener
Write-Host "🎧 STARTING LIVE EXCEPTION LISTENER" -ForegroundColor Blue
Write-Host "====================================" -ForegroundColor Blue

# Generate JWT
Write-Host "Generating JWT token..." -ForegroundColor Cyan
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()

if ($token -and $token.Length -gt 50) {
    Write-Host "✅ JWT token generated successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to generate JWT token" -ForegroundColor Red
    exit 1
}

Write-Host "`nStarting persistent WebSocket listener..." -ForegroundColor Cyan
Write-Host "This will listen for real exception events in real-time." -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop the listener." -ForegroundColor Yellow
Write-Host ""

# Start the listener
node live-exception-listener.js $token