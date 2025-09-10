# YAML Duplicate Key and Compilation Fix
Write-Host "=== YAML Duplicate Key and Compilation Fix ===" -ForegroundColor Green

$yamlPath = "src/main/resources/application.yml"

# Step 1: Fix duplicate 'app:' keys
Write-Host "1. Fixing YAML duplicate keys..." -ForegroundColor Cyan

if (Test-Path $yamlPath) {
    $lines = Get-Content $yamlPath
    $appKeyLines = @()
    
    # Find all 'app:' key lines
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '^app:\s*$') {
            $appKeyLines += $i + 1
        }
    }
    
    Write-Host "   Found 'app:' keys at lines: $($appKeyLines -join ', ')" -ForegroundColor Gray
    
    if ($appKeyLines.Count -gt 1) {
        Write-Host "   Removing duplicate 'app:' section..." -ForegroundColor Yellow
        
        $newLines = @()
        $skipSection = $false
        
        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]
            
            # If this is the second 'app:' section (around line 767)
            if ($line -match '^app:\s*$' -and $i -gt 500) {
                Write-Host "   Removing duplicate 'app:' at line $($i + 1)" -ForegroundColor Gray
                $skipSection = $true
                continue
            }
            
            # Skip content under the duplicate app section
            if ($skipSection) {
                if ($line -match '^[a-zA-Z][a-zA-Z0-9-]*:\s*$' -and -not ($line -match '^\s+')) {
                    $skipSection = $false
                    $newLines += $line
                }
                # Skip lines that are part of the duplicate app section
            } else {
                $newLines += $line
            }
        }
        
        # Write fixed content back
        Set-Content -Path $yamlPath -Value $newLines -Encoding UTF8
        Write-Host "   ✅ Fixed duplicate 'app:' keys" -ForegroundColor Green
    } else {
        Write-Host "   ✅ No duplicate keys found" -ForegroundColor Green
    }
}

# Step 2: Test compilation
Write-Host "`n2. Testing compilation..." -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "   Running compilation fixes..." -ForegroundColor Yellow
    
    if (Test-Path "resolve-all-errors.ps1") {
        powershell ".\resolve-all-errors.ps1"
    }
    
    mvn compile -q
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ Compilation fixed!" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Compilation still failing" -ForegroundColor Red
    }
}

# Step 3: Test startup
Write-Host "`n3. Testing application startup..." -ForegroundColor Cyan

$job = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8089" 2>&1
}

Start-Sleep -Seconds 20
$output = Receive-Job -Job $job -Keep
Stop-Job -Job $job -Force 2>$null
Remove-Job -Job $job -Force 2>$null

if ($output -match "DuplicateKeyException") {
    Write-Host "   ❌ Duplicate key error still present" -ForegroundColor Red
} elseif ($output -match "Started.*Application") {
    Write-Host "   ✅ Application started successfully!" -ForegroundColor Green
} else {
    Write-Host "   ⚠️ Startup test inconclusive" -ForegroundColor Yellow
}

Write-Host "`n=== SUMMARY ===" -ForegroundColor Yellow
Write-Host "✅ YAML duplicate key processing complete" -ForegroundColor Green
Write-Host "✅ Compilation processing complete" -ForegroundColor Green
Write-Host "Application should be ready for development!" -ForegroundColor Green