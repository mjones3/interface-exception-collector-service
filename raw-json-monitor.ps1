# Raw JSON Event Monitor
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
Write-Host "Starting raw JSON monitor..." -ForegroundColor Green
node simple-raw-listener.js $token