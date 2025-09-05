Write-Host "Testing SourceServiceClientRegistry to verify PartnerOrderServiceClient is registered..."

# Let's check the application logs to see if our client was registered
Write-Host "Checking application logs for PartnerOrderServiceClient registration..."

# Use kubectl to get the logs and filter for our client
$podName = "interface-exception-collector-7f4b5f9f68-cgkzn"
$logs = kubectl logs $podName | Select-String -Pattern "PartnerOrderServiceClient|RestTemplate|SourceServiceClientRegistry" -CaseSensitive:$false

if ($logs) {
    Write-Host "Found relevant log entries:"
    $logs | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host "No relevant log entries found"
}

Write-Host "`nChecking if the application started successfully..."
$startupLogs = kubectl logs $podName | Select-String -Pattern "Started.*InterfaceExceptionCollectorApplication" -CaseSensitive:$false
if ($startupLogs) {
    Write-Host "✅ Application started successfully"
    $startupLogs | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host "❌ Application startup not confirmed"
}