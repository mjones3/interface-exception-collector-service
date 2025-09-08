# Start Live Dashboard Subscription
Write-Host "📊 STARTING LIVE DASHBOARD SUBSCRIPTION" -ForegroundColor Blue
Write-Host "=======================================" -ForegroundColor Blue

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
Write-Host "📊 LIVE DASHBOARD FEATURES" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green
Write-Host ""
Write-Host "Real-time metrics displayed:" -ForegroundColor Yellow
Write-Host "  🚨 Active exceptions (NEW, ACKNOWLEDGED)" -ForegroundColor Yellow
Write-Host "  📅 Today's total exceptions" -ForegroundColor Yellow
Write-Host "  🔄 Retry statistics (success/failed)" -ForegroundColor Yellow
Write-Host "  📈 Retry success rate percentage" -ForegroundColor Yellow
Write-Host "  🎯 Overall API success rate" -ForegroundColor Yellow
Write-Host "  📞 Total API calls today" -ForegroundColor Yellow
Write-Host "  📊 Change tracking between updates" -ForegroundColor Yellow
Write-Host ""
Write-Host "Update frequency:" -ForegroundColor Cyan
Write-Host "  • Automatic updates every 30 seconds" -ForegroundColor Cyan
Write-Host "  • Immediate updates when events occur" -ForegroundColor Cyan
Write-Host ""
Write-Host "To test with live data:" -ForegroundColor Cyan
Write-Host "  1. Open another PowerShell window" -ForegroundColor Cyan
Write-Host "  2. Run: powershell -File trigger-retry-test.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to start the live dashboard..." -ForegroundColor Green
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host ""
Write-Host "🚀 Starting live dashboard..." -ForegroundColor Green
Write-Host "   (Press Ctrl+C to stop)" -ForegroundColor Gray
Write-Host ""

# Start the dashboard
node live-dashboard-listener.js $token