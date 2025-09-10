# PowerShell script to fix specific compilation errors

Write-Host "=== Fixing Specific Compilation Errors ===" -ForegroundColor Green

# First, let's check what enum values exist in ExceptionStatus
Write-Host "Checking ExceptionStatus enum..." -ForegroundColor Yellow

# Read the ExceptionStatus enum to see what values are available
$exceptionStatusPath = "src/main/java/com/arcone/biopro/exception/collector/domain/enums/ExceptionStatus.java"
if (Test-Path $exceptionStatusPath) {
    $enumContent = Get-Content $exceptionStatusPath -Raw
    Write-Host "ExceptionStatus enum content:" -ForegroundColor Cyan
    Write-Host $enumContent
} else {
    Write-Host "ExceptionStatus enum not found at expected path" -ForegroundColor Red
}

Write-Host "`n=== Error Analysis Complete ===" -ForegroundColor Green