# Fix YAML Duplicate Keys
Write-Host "=== Fixing YAML Duplicate Keys ===" -ForegroundColor Green

$yamlPath = "src/main/resources/application.yml"

if (Test-Path $yamlPath) {
    Write-Host "Analyzing $yamlPath for duplicate keys..." -ForegroundColor Cyan
    
    $lines = Get-Content $yamlPath
    $keyOccurrences = @{}
    
    # Find all top-level keys
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        if ($line -match '^([a-zA-Z][a-zA-Z0-9-]*):(\s|$)') {
            $key = $matches[1]
            if (-not $keyOccurrences.ContainsKey($key)) {
                $keyOccurrences[$key] = @()
            }
            $keyOccurrences[$key] += $i + 1
        }
    }
    
    # Find duplicates
    $duplicates = @{}
    foreach ($key in $keyOccurrences.Keys) {
        if ($keyOccurrences[$key].Count -gt 1) {
            $duplicates[$key] = $keyOccurrences[$key]
            Write-Host "Found duplicate key '$key' at lines: $($keyOccurrences[$key] -join ', ')" -ForegroundColor Red
        }
    }
    
    if ($duplicates.Count -eq 0) {
        Write-Host "No duplicate keys found" -ForegroundColor Green
    } else {
        Write-Host "Fixing $($duplicates.Count) duplicate keys..." -ForegroundColor Yellow
        
        $newLines = @()
        $skipLines = @()
        
        # Mark lines to skip (keep first occurrence, remove others)
        foreach ($key in $duplicates.Keys) {
            $occurrences = $duplicates[$key]
            for ($j = 1; $j -lt $occurrences.Count; $j++) {
                $startLine = $occurrences[$j] - 1  # Convert to 0-based
                
                # Find the end of this section
                $endLine = $startLine
                for ($k = $startLine + 1; $k -lt $lines.Count; $k++) {
                    if ($lines[$k] -match '^[a-zA-Z][a-zA-Z0-9-]*:\s*$' -and -not ($lines[$k] -match '^\s+')) {
                        break
                    }
                    $endLine = $k
                }
                
                # Mark all lines in this section for skipping
                for ($l = $startLine; $l -le $endLine; $l++) {
                    $skipLines += $l
                }
                
                Write-Host "Removing duplicate '$key' section from line $($startLine + 1) to $($endLine + 1)" -ForegroundColor Gray
            }
        }
        
        # Build new content without skipped lines
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if ($skipLines -notcontains $i) {
                $newLines += $lines[$i]
            }
        }
        
        # Write fixed content
        Set-Content -Path $yamlPath -Value $newLines -Encoding UTF8
        Write-Host "Fixed duplicate keys in $yamlPath" -ForegroundColor Green
    }
} else {
    Write-Host "YAML file not found: $yamlPath" -ForegroundColor Red
}

# Test compilation
Write-Host "`nTesting compilation..." -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "Compilation failed" -ForegroundColor Red
}

# Quick startup test
Write-Host "`nTesting for duplicate key errors..." -ForegroundColor Cyan
$job = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8092" 2>&1
}

Start-Sleep -Seconds 20
$output = Receive-Job -Job $job -Keep
Stop-Job -Job $job 2>$null
Remove-Job -Job $job 2>$null

if ($output -match "DuplicateKeyException") {
    Write-Host "Still has duplicate key errors:" -ForegroundColor Red
    $output | Where-Object { $_ -match "DuplicateKeyException|duplicate key" } | ForEach-Object {
        Write-Host "  $_" -ForegroundColor Red
    }
} elseif ($output -match "Started.*Application") {
    Write-Host "Application started successfully - no duplicate key errors!" -ForegroundColor Green
} else {
    Write-Host "No duplicate key errors detected" -ForegroundColor Green
}

Write-Host "`nYAML duplicate key fix complete!" -ForegroundColor Green