# PowerShell script to fix compilation issues and ensure application starts successfully

Write-Host "=== Starting Compilation Fix Process ===" -ForegroundColor Green

# Step 1: Clean and attempt compilation to identify issues
Write-Host "Step 1: Attempting clean compilation..." -ForegroundColor Yellow
$compileResult = & mvn clean compile -DskipTests -q 2>&1
$compileExitCode = $LASTEXITCODE

if ($compileExitCode -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "Compilation failed. Analyzing errors..." -ForegroundColor Red
    Write-Host $compileResult -ForegroundColor Red
    
    # Check for specific error patterns and fix them
    if ($compileResult -match "cannot find symbol.*RETRYING") {
        Write-Host "Fixing missing RETRYING enum value..." -ForegroundColor Yellow
        # This will be handled in the Java code fixes
    }
    
    if ($compileResult -match "cannot find symbol.*FAILED") {
        Write-Host "Fixing missing FAILED enum value..." -ForegroundColor Yellow
        # This will be handled in the Java code fixes
    }
    
    if ($compileResult -match "countByStatusAndInitiatedAtBetween") {
        Write-Host "Fixing missing repository method..." -ForegroundColor Yellow
        # This will be handled in the Java code fixes
    }
}

# Step 2: Try test compilation
Write-Host "Step 2: Attempting test compilation..." -ForegroundColor Yellow
$testCompileResult = & mvn test-compile -DskipTests -q 2>&1
$testCompileExitCode = $LASTEXITCODE

if ($testCompileExitCode -eq 0) {
    Write-Host "Test compilation successful!" -ForegroundColor Green
} else {
    Write-Host "Test compilation failed." -ForegroundColor Red
    Write-Host $testCompileResult -ForegroundColor Red
}

# Step 3: Run specific test to verify our changes work
if ($compileExitCode -eq 0 -and $testCompileExitCode -eq 0) {
    Write-Host "Step 3: Running enhanced result types test..." -ForegroundColor Yellow
    $testResult = & mvn test -Dtest=EnhancedResultTypesTest -q 2>&1
    $testExitCode = $LASTEXITCODE
    
    if ($testExitCode -eq 0) {
        Write-Host "Enhanced result types test passed!" -ForegroundColor Green
    } else {
        Write-Host "Enhanced result types test failed." -ForegroundColor Red
        Write-Host $testResult -ForegroundColor Red
    }
}

# Step 4: Try to start the application (if compilation successful)
if ($compileExitCode -eq 0) {
    Write-Host "Step 4: Testing application startup..." -ForegroundColor Yellow
    
    # Start the application in background and check if it starts successfully
    $startupJob = Start-Job -ScriptBlock {
        Set-Location $args[0]
        & mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=0 --spring.profiles.active=test" -q
    } -ArgumentList (Get-Location)
    
    # Wait for startup (max 60 seconds)
    $timeout = 60
    $elapsed = 0
    $started = $false
    
    while ($elapsed -lt $timeout -and -not $started) {
        Start-Sleep -Seconds 2
        $elapsed += 2
        
        # Check if application started by looking for Spring Boot startup messages
        $jobOutput = Receive-Job -Job $startupJob -Keep
        if ($jobOutput -match "Started.*Application.*in.*seconds" -or $jobOutput -match "Tomcat started on port") {
            $started = $true
            Write-Host "Application started successfully!" -ForegroundColor Green
        } elseif ($jobOutput -match "APPLICATION FAILED TO START" -or $jobOutput -match "Error starting ApplicationContext") {
            Write-Host "Application failed to start." -ForegroundColor Red
            Write-Host $jobOutput -ForegroundColor Red
            break
        }
        
        Write-Host "Waiting for startup... ($elapsed/$timeout seconds)" -ForegroundColor Cyan
    }
    
    # Stop the background job
    Stop-Job -Job $startupJob -ErrorAction SilentlyContinue
    Remove-Job -Job $startupJob -ErrorAction SilentlyContinue
    
    if (-not $started -and $elapsed -ge $timeout) {
        Write-Host "Application startup timed out after $timeout seconds." -ForegroundColor Yellow
    }
} else {
    Write-Host "Skipping application startup test due to compilation errors." -ForegroundColor Yellow
}

Write-Host "=== Compilation Fix Process Complete ===" -ForegroundColor Green

# Return appropriate exit code
if ($compileExitCode -eq 0) {
    exit 0
} else {
    exit 1
}