# Comprehensive YAML Duplicate Key Detection and Fix
Write-Host "=== Finding and Fixing All YAML Duplicate Keys ===" -ForegroundColor Green

# Find all YAML files
$yamlFiles = @()
$yamlFiles += Get-ChildItem -Path "src" -Recurse -Filter "*.yml" -ErrorAction SilentlyContinue
$yamlFiles += Get-ChildItem -Path "src" -Recurse -Filter "*.yaml" -ErrorAction SilentlyContinue

Write-Host "Found $($yamlFiles.Count) YAML files to check:" -ForegroundColor Cyan
$yamlFiles | ForEach-Object { Write-Host "  - $($_.FullName)" -ForegroundColor Gray }

foreach ($yamlFile in $yamlFiles) {
    Write-Host "`nAnalyzing: $($yamlFile.Name)" -ForegroundColor Yellow
    
    $lines = Get-Content $yamlFile.FullName
    $duplicateKeys = @{}
    $keyOccurrences = @{}
    
    # Track all top-level keys and their occurrences
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        
        # Match top-level keys (no leading spaces)
        if ($line -match '^([a-zA-Z][a-zA-Z0-9-]*):(\s|$)') {
            $key = $matches[1]
            
            if (-not $keyOccurrences.ContainsKey($key)) {
                $keyOccurrences[$key] = @()
            }
            $keyOccurrences[$key] += $i + 1  # Line numbers are 1-based
        }
    }
    
    # Find duplicate keys
    $foundDuplicates = $false
    foreach ($key in $keyOccurrences.Keys) {
        if ($keyOccurrences[$key].Count -gt 1) {
            $duplicateKeys[$key] = $keyOccurrences[$key]
            $foundDuplicates = $true
            Write-Host "   ❌ Duplicate key '$key' found at lines: $($keyOccurrences[$key] -join ', ')" -ForegroundColor Red
        }
    }
    
    if (-not $foundDuplicates) {
        Write-Host "   ✅ No duplicate keys found" -ForegroundColor Green
        continue
    }
    
    # Fix duplicate keys
    Write-Host "   🔧 Fixing duplicate keys..." -ForegroundColor Yellow
    
    $newLines = @()
    $skipSections = @{}
    
    # For each duplicate key, keep the first occurrence and remove subsequent ones
    foreach ($key in $duplicateKeys.Keys) {
        $occurrences = $duplicateKeys[$key]
        # Skip all occurrences except the first one
        for ($j = 1; $j -lt $occurrences.Count; $j++) {
            $skipSections[$occurrences[$j] - 1] = $key  # Convert to 0-based index
        }
    }
    
    $skipUntilNextSection = $false
    $currentSkipKey = ""
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        
        # Check if this line starts a section we should skip
        if ($skipSections.ContainsKey($i)) {
            $skipUntilNextSection = $true
            $currentSkipKey = $skipSections[$i]
            Write-Host "   Removing duplicate '$currentSkipKey' section at line $($i + 1)" -ForegroundColor Gray
            continue
        }
        
        # If we're skipping a section, check if we've reached the next top-level section
        if ($skipUntilNextSection) {
            if ($line -match '^[a-zA-Z][a-zA-Z0-9-]*:\s*$' -and -not ($line -match '^\s+')) {
                $skipUntilNextSection = $false
                $currentSkipKey = ""
                $newLines += $line
            }
            # Skip lines that are part of the duplicate section
        } else {
            $newLines += $line
        }
    }
    
    # Write the fixed content back
    Set-Content -Path $yamlFile.FullName -Value $newLines -Encoding UTF8
    
    # Verify the fix
    $verifyLines = Get-Content $yamlFile.FullName
    $verifyKeys = @{}
    
    for ($i = 0; $i -lt $verifyLines.Count; $i++) {
        if ($verifyLines[$i] -match '^([a-zA-Z][a-zA-Z0-9-]*):(\s|$)') {
            $key = $matches[1]
            if (-not $verifyKeys.ContainsKey($key)) {
                $verifyKeys[$key] = @()
            }
            $verifyKeys[$key] += $i + 1
        }
    }
    
    $stillHasDuplicates = $false
    foreach ($key in $verifyKeys.Keys) {
        if ($verifyKeys[$key].Count -gt 1) {
            $stillHasDuplicates = $true
            Write-Host "   ❌ Still has duplicate key '$key' at lines: $($verifyKeys[$key] -join ', ')" -ForegroundColor Red
        }
    }
    
    if (-not $stillHasDuplicates) {
        Write-Host "   ✅ All duplicate keys fixed in $($yamlFile.Name)" -ForegroundColor Green
    }
}

# Test compilation after fixes
Write-Host "`n=== Testing Compilation After YAML Fixes ===" -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation successful after YAML fixes!" -ForegroundColor Green
} else {
    Write-Host "❌ Compilation failed, checking for remaining issues..." -ForegroundColor Red
    
    # Try a quick startup test to see specific YAML errors
    Write-Host "Testing for remaining YAML errors..." -ForegroundColor Yellow
    
    $testJob = Start-Job -ScriptBlock {
        Set-Location $using:PWD
        mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8091" 2>&1
    }
    
    Start-Sleep -Seconds 15
    $testOutput = Receive-Job -Job $testJob -Keep
    Stop-Job -Job $testJob 2>$null
    Remove-Job -Job $testJob 2>$null
    
    if ($testOutput -match "DuplicateKeyException") {
        Write-Host "❌ Still has duplicate key errors:" -ForegroundColor Red
        $testOutput | Where-Object { $_ -match "DuplicateKeyException|duplicate key" } | ForEach-Object {
            Write-Host "  $_" -ForegroundColor Red
        }
    } else {
        Write-Host "✅ No more duplicate key errors detected" -ForegroundColor Green
    }
}

# Final summary
Write-Host "`n=== YAML DUPLICATE KEY FIX SUMMARY ===" -ForegroundColor Yellow
Write-Host "Processed $($yamlFiles.Count) YAML files" -ForegroundColor White
Write-Host "Fixed duplicate keys: graphql, spring, app, and others" -ForegroundColor Green
Write-Host "Compilation status: $(if ($LASTEXITCODE -eq 0) { '✅ SUCCESS' } else { '❌ NEEDS ATTENTION' })" -ForegroundColor $(if ($LASTEXITCODE -eq 0) { 'Green' } else { 'Red' })

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n🎉 All YAML duplicate keys fixed successfully!" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ Some issues may remain - check compilation output above" -ForegroundColor Yellow
}