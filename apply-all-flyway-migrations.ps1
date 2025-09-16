#!/usr/bin/env pwsh

# Apply All Flyway Migrations Script
Write-Host "=== APPLYING ALL FLYWAY MIGRATIONS ===" -ForegroundColor Green

$ErrorActionPreference = "Continue"

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

# Database connection details from application.yml
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_NAME = "exception_collector_db"
$DB_USER = "exception_user"
$DB_PASSWORD = "exception_pass"

Write-Log "Step 1: Checking database connectivity" "Magenta"

# Set PostgreSQL password environment variable
$env:PGPASSWORD = $DB_PASSWORD

# Test database connection
try {
    $connectionTest = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT 1;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Log "✓ Database connection successful" "Green"
    } else {
        Write-Log "✗ Database connection failed: $connectionTest" "Red"
        Write-Log "Waiting for database to be ready..." "Yellow"
        Start-Sleep -Seconds 30
        
        # Retry connection
        $connectionTest = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT 1;" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Log "✗ Database still not ready. Please ensure PostgreSQL is running." "Red"
            exit 1
        }
    }
} catch {
    Write-Log "✗ Error testing database connection: $_" "Red"
    exit 1
}

Write-Log "Step 2: Checking current Flyway schema history" "Magenta"

# Check current migration status
try {
    $currentMigrations = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Log "Current migration status:" "Cyan"
        Write-Host $currentMigrations
    } else {
        Write-Log "Flyway schema history table doesn't exist yet - this is normal for first run" "Yellow"
    }
} catch {
    Write-Log "Could not check migration status: $_" "Yellow"
}

Write-Log "Step 3: Running Flyway migrations using Maven" "Magenta"

# Change to the interface-exception-collector directory
Set-Location "interface-exception-collector"

# Run Flyway migrations using Maven
try {
    Write-Log "Executing: mvn flyway:migrate" "Cyan"
    mvn flyway:migrate -Dflyway.url="jdbc:postgresql://$DB_HOST`:$DB_PORT/$DB_NAME" -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD -Dflyway.locations="classpath:db/migration" -Dflyway.schemas="public" -Dflyway.table="flyway_schema_history" -Dflyway.baselineOnMigrate=true -Dflyway.validateOnMigrate=true -Dflyway.outOfOrder=false -Dflyway.cleanDisabled=true
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "✓ Flyway migrations completed successfully" "Green"
    } else {
        Write-Log "✗ Flyway migrations failed with exit code: $LASTEXITCODE" "Red"
        Set-Location ".."
        exit 1
    }
} catch {
    Write-Log "✗ Error running Flyway migrations: $_" "Red"
    Set-Location ".."
    exit 1
}

# Return to parent directory
Set-Location ".."

Write-Log "Step 4: Verifying migration results" "Magenta"

# Check final migration status
try {
    $finalMigrations = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT version, description, success, installed_on FROM flyway_schema_history ORDER BY installed_rank;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Log "Final migration status:" "Cyan"
        Write-Host $finalMigrations
    }
} catch {
    Write-Log "Could not verify final migration status: $_" "Yellow"
}

Write-Log "Step 5: Checking for acknowledgment_notes column" "Magenta"

# Verify the acknowledgment_notes column exists
try {
    $columnCheck = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT column_name, data_type, character_maximum_length FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name IN ('acknowledgment_notes', 'resolution_method', 'resolution_notes', 'acknowledged_at', 'acknowledged_by', 'resolved_at', 'resolved_by') ORDER BY column_name;" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "✓ Column verification successful:" "Green"
        Write-Host $columnCheck
        
        # Check if acknowledgment_notes specifically exists
        $ackNotesCheck = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name = 'acknowledgment_notes';" -t 2>&1
        
        if ($ackNotesCheck -match "1") {
            Write-Log "✓ acknowledgment_notes column exists!" "Green"
        } else {
            Write-Log "✗ acknowledgment_notes column is still missing" "Red"
        }
    } else {
        Write-Log "✗ Column verification failed: $columnCheck" "Red"
    }
} catch {
    Write-Log "✗ Error verifying columns: $_" "Red"
}

Write-Log "Step 6: Testing application startup (optional)" "Magenta"

# Check if services are running and test the API
try {
    $healthCheck = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction Stop
    if ($healthCheck.StatusCode -eq 200) {
        Write-Log "✓ Application is running and healthy" "Green"
        
        # Test the exceptions endpoint
        $token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2OTI4OTEsImV4cCI6MTc1NzY5NjQ5MX0.vuuVBRAlIWgzfHHNtQ1HC_dmYAHJqoBY9zfx_7wjkVQ"
        $headers = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        $apiTest = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 10 -ErrorAction Stop
        if ($apiTest.StatusCode -eq 200) {
            Write-Log "✓ API endpoint test successful - no acknowledgment_notes error!" "Green"
            $content = $apiTest.Content | ConvertFrom-Json
            Write-Log "Found $($content.totalElements) exceptions in system" "Cyan"
        }
    }
} catch {
    Write-Log "Application not running or not ready yet: $_" "Yellow"
    Write-Log "This is normal if services are still starting up" "Yellow"
}

Write-Log "=== FLYWAY MIGRATIONS COMPLETE ===" "Green"
Write-Log "Summary:" "Yellow"
Write-Log "- All Flyway migrations have been applied" "White"
Write-Log "- Database schema should now include acknowledgment_notes column" "White"
Write-Log "- Application should start without column errors" "White"
Write-Log "" "White"
Write-Log "If the application is not running, start it with: tilt up" "Cyan"

# Clean up environment variable
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue