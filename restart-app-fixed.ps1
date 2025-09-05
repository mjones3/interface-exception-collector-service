#!/usr/bin/env powershell
# Restart the application with fixed database schema
Write-Host "🚀 Restarting Application with Fixed Database" -ForegroundColor Green
Write-Host ""

Write-Host "✅ Database columns added:" -ForegroundColor Green
Write-Host "   - acknowledgment_notes" -ForegroundColor White
Write-Host "   - resolution_method" -ForegroundColor White  
Write-Host "   - resolution_notes" -ForegroundColor White
Write-Host ""

Write-Host "Restarting Tilt..." -ForegroundColor Yellow
tilt down
Start-Sleep -Seconds 3
tilt up

Write-Host ""
Write-Host "🎯 Application should now start without schema errors!" -ForegroundColor Green