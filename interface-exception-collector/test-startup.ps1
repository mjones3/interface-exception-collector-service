# Simple script to test application startup

Write-Host "Testing application startup..." -ForegroundColor Green

# Try to start the application with a timeout
$timeout = 30
Write-Host "Starting Spring Boot application (timeout: $timeout seconds)..." -ForegroundColor Yellow

$startupJob = Start-Job -ScriptBlock {
    Set-Location $args[0]
    & mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=0 --spring.profiles.active=test --logging.level.root=WARN" -q 2>&1
} -ArgumentList (Get-Location)

$elapsed = 0
$started = $false

while ($elapsed -lt $timeout -and -not $started) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    
    $jobOutput = Receive-Job -Job $startupJob -Keep
    if ($jobOutput -match "Started.*Application.*in.*seconds" -or $jobOutput -match "Tomcat started on port") {
        $started = $true
        Write-Host "Application started successfully!" -ForegroundColor Green
        break
    } elseif ($jobOutput -match "APPLICATION FAILED TO START" -or $jobOutput -match "Error starting ApplicationContext") {
        Write-Host "Application failed to start:" -ForegroundColor Red
        Write-Host $jobOutput -ForegroundColor Red
        break
    }
    
    Write-Host "Waiting... ($elapsed/$timeout seconds)" -ForegroundColor Cyan
}

# Stop the job
Stop-Job -Job $startupJob -ErrorAction SilentlyContinue
Remove-Job -Job $startupJob -ErrorAction SilentlyContinue

if ($started) {
    Write-Host "SUCCESS: Application compiles and starts successfully!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "Application startup timed out or failed." -ForegroundColor Yellow
    exit 1
}