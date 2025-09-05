$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODA4NDYsImV4cCI6MTc1NzA4NDQ0Nn0.7hQgoI8QEZynrMGanP2Yqd_WRtfrmP44LTiBLqEzfiI"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "Testing PARTNER_ORDER exception retry with REST client..."
Write-Host "Transaction ID: 137ed65a-ce10-4cac-84d2-4e6e08bbed40"
Write-Host ""

# Create a retry request body
$retryRequest = @{
    reason = "Testing REST-based retry for PARTNER_ORDER"
    initiatedBy = "test-user"
} | ConvertTo-Json

Write-Host "Retry Request Body:"
Write-Host $retryRequest
Write-Host ""

try {
    Write-Host "Submitting retry request..."
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/137ed65a-ce10-4cac-84d2-4e6e08bbed40/retry" -Method POST -Headers $headers -Body $retryRequest
    
    Write-Host "SUCCESS: Retry request submitted!"
    Write-Host "Response:"
    Write-Host ($response | ConvertTo-Json -Depth 3)
    
    Write-Host ""
    Write-Host "Now checking application logs for REST calls..."
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody"
    }
}

Write-Host ""
Write-Host "Checking logs for PartnerOrderServiceClient usage..."