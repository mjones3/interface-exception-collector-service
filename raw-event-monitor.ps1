# Raw Event Monitor - Simple and Direct
Write-Host "RAW EVENT MONITOR" -ForegroundColor Green
Write-Host "=================" -ForegroundColor Green

# Generate JWT
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
Write-Host "Token generated, starting monitor..." -ForegroundColor Cyan

# Run the raw event listener
node live-exception-listener.js $token