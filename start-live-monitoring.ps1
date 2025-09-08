# Start Live Exception Monitoring
Write-Host "🎧 STARTING LIVE EXCEPTION MONITORING" -ForegroundColor Blue
Write-Host "=====================================" -ForegroundColor Blue

# Check if application is running
Write-Host "1. Checking application status..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    if ($health.status -eq "UP") {
        Write-Host "✅ Application is running" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Application status: $($health.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Application not running. Please start it first:" -ForegroundColor Red
    Write-Host "   cd interface-exception-collector" -ForegroundColor White
    Write-Host "   mvn spring-boot:run" -ForegroundColor White
    exit 1
}

# Generate JWT token
Write-Host "2. Generating JWT token..." -ForegroundColor Cyan
try {
    $token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
    if ($token -and $token.Length -gt 50) {
        Write-Host "✅ JWT token generated successfully" -ForegroundColor Green
    } else {
        Write-Host "❌ JWT token generation failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ JWT generation error: $_" -ForegroundColor Red
    exit 1
}

# Test WebSocket connectivity
Write-Host "3. Testing WebSocket connectivity..." -ForegroundColor Cyan
try {
    # Quick connectivity test
    $testResult = node working-subscription-test.js $token 2>&1
    if ($testResult -like "*FULLY WORKING*") {
        Write-Host "✅ WebSocket connection test passed" -ForegroundColor Green
    } else {
        Write-Host "⚠️ WebSocket test completed with warnings" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ WebSocket connectivity test failed: $_" -ForegroundColor Red
    Write-Host "Continuing anyway..." -ForegroundColor Yellow
}

Write-Host "`n🚀 STARTING LIVE MONITORING..." -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green
Write-Host "📡 Endpoint: ws://localhost:8080/graphql" -ForegroundColor White
Write-Host "🔐 Authentication: JWT Bearer token" -ForegroundColor White
Write-Host "📊 Monitoring: Real-time exception events" -ForegroundColor White
Write-Host "⏹️ Stop: Press Ctrl+C" -ForegroundColor White
Write-Host ""

# Start the live listener
node live-exception-listener.js $token