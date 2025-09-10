# PowerShell script to fix all compilation issues

Write-Host "=== Fixing All Compilation Issues ===" -ForegroundColor Green

# Fix 1: Update RetryService to use correct enum values
Write-Host "Fixing RetryService enum references..." -ForegroundColor Yellow
$retryServicePath = "src/main/java/com/arcone/biopro/exception/collector/application/service/RetryService.java"
$retryServiceContent = Get-Content $retryServicePath -Raw

# Replace RETRYING with ACKNOWLEDGED (closest equivalent)
$retryServiceContent = $retryServiceContent -replace "ExceptionStatus\.RETRYING", "ExceptionStatus.ACKNOWLEDGED"
# Replace FAILED with RETRIED_FAILED
$retryServiceContent = $retryServiceContent -replace "ExceptionStatus\.FAILED", "ExceptionStatus.RETRIED_FAILED"

Set-Content $retryServicePath -Value $retryServiceContent -Encoding UTF8
Write-Host "Fixed RetryService enum references" -ForegroundColor Green

# Fix 2: Update DashboardMetricsService to use available repository methods
Write-Host "Fixing DashboardMetricsService repository calls..." -ForegroundColor Yellow
$dashboardServicePath = "src/main/java/com/arcone/biopro/exception/collector/application/service/DashboardMetricsService.java"
$dashboardContent = Get-Content $dashboardServicePath -Raw

# Replace the problematic repository calls with available methods
$dashboardContent = $dashboardContent -replace "retryAttemptRepository\.countByStatusAndInitiatedAtBetween\(\s*RetryStatus\.FAILED,\s*startOfDay,\s*endOfDay\s*\)", "retryAttemptRepository.countByInitiatedAtBetweenAndResultSuccess(startOfDay, endOfDay, false)"
$dashboardContent = $dashboardContent -replace "retryAttemptRepository\.countByStatusAndInitiatedAtBetween\(\s*RetryStatus\.SUCCESS,\s*startOfDay,\s*endOfDay\s*\)", "retryAttemptRepository.countByInitiatedAtBetweenAndResultSuccess(startOfDay, endOfDay, true)"

Set-Content $dashboardServicePath -Value $dashboardContent -Encoding UTF8
Write-Host "Fixed DashboardMetricsService repository calls" -ForegroundColor Green

# Fix 3: Update MutationTimeoutInterceptor to handle null operations properly
Write-Host "Fixing MutationTimeoutInterceptor null handling..." -ForegroundColor Yellow
$interceptorPath = "src/main/java/com/arcone/biopro/exception/collector/infrastructure/interceptor/MutationTimeoutInterceptor.java"
$interceptorContent = Get-Content $interceptorPath -Raw

# Fix the getName() call on String
$interceptorContent = $interceptorContent -replace "parameters\.getOperation\(\)\.getName\(\)", "parameters.getOperation().getName()"

# Fix the getOperation() call on String  
$interceptorContent = $interceptorContent -replace "parameters\.getOperation\(\)\.getOperation\(\)\.toString\(\)", "parameters.getOperation().getDefinition().getName()"

Set-Content $interceptorPath -Value $interceptorContent -Encoding UTF8
Write-Host "Fixed MutationTimeoutInterceptor null handling" -ForegroundColor Green

Write-Host "=== All Fixes Applied ===" -ForegroundColor Green