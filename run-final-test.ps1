# Run Final Working Subscription Test
Write-Host "🎯 RUNNING FINAL SUBSCRIPTION TEST" -ForegroundColor Blue

# Generate JWT
$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
Write-Host "✅ JWT token generated" -ForegroundColor Green

Write-Host "`nStarting comprehensive subscription test..." -ForegroundColor Cyan
node working-subscription-test.js $token

Write-Host "`n🎉 FINAL TEST COMPLETE!" -ForegroundColor Blue