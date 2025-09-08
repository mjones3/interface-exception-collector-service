# Start Live Retry Subscription Listener
Write-Host "🔄 STARTING LIVE RETRY SUBSCRIPTION LISTENER" -ForegroundColor Blue
Write-Host "=============================================" -ForegroundColor Blue

# Check if application is running
Write-Host "🔍 Checking if application is running..." -ForegroundColor Cyan
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "✅ Application is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Application is not running!" -ForegroundColor Red
    Write-Host "   Please start the application first:" -ForegroundColor Yellow
    Write-Host "   cd interface-exception-collector" -ForegroundColor Yellow
    Write-Host "   mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# Generate JWT token
Write-Host "🔑 Generating JWT token..." -ForegroundColor Cyan
try {
    $token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
    if (-not $token) {
        throw "No token generated"
    }
    Write-Host "✅ JWT token generated (length: $($token.Length))" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to generate JWT token: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎯 READY TO START LIVE RETRY LISTENER" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
Write-Host "The listener will:" -ForegroundColor Yellow
Write-Host "  • Connect to the retry subscription WebSocket" -ForegroundColor Yellow
Write-Host "  • Show ALL raw messages received" -ForegroundColor Yellow
Write-Host "  • Display retry events with full details" -ForegroundColor Yellow
Write-Host "  • Run indefinitely until you stop it (Ctrl+C)" -ForegroundColor Yellow
Write-Host ""
Write-Host "To test retry events while the listener is running:" -ForegroundColor Cyan
Write-Host "  1. Open another PowerShell window" -ForegroundColor Cyan
Write-Host "  2. Run: powershell -File trigger-retry-test.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to start the listener..." -ForegroundColor Green
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host ""
Write-Host "🚀 Starting live retry listener..." -ForegroundColor Green
Write-Host "   (Press Ctrl+C to stop)" -ForegroundColor Gray
Write-Host ""

# Start the listener
node live-retry-listener.js $token