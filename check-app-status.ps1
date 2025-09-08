# Check application status
Write-Host "Checking application status..." -ForegroundColor Cyan

try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3
    Write-Host "Application is running" -ForegroundColor Green
    Write-Host "Status: $($health.status)" -ForegroundColor Gray
} catch {
    Write-Host "Application appears to be down: $($_.Exception.Message)" -ForegroundColor Red
    
    Write-Host "Checking if Java processes are running..." -ForegroundColor Yellow
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        Write-Host "Java processes found:" -ForegroundColor Yellow
        foreach ($proc in $javaProcesses) {
            $memoryMB = [math]::Round($proc.WorkingSet64/1MB, 2)
            Write-Host "  PID: $($proc.Id) Memory: $memoryMB MB" -ForegroundColor Gray
        }
    } else {
        Write-Host "No Java processes running" -ForegroundColor Red
    }
}