$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODA4NDYsImV4cCI6MTc1NzA4NDQ0Nn0.7hQgoI8QEZynrMGanP2Yqd_WRtfrmP44LTiBLqEzfiI"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "Testing if PartnerOrderServiceClient is working by checking exception details..."
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/b36e43a5-4367-4f22-8718-5f8efd1fc4ef" -Method GET -Headers $headers
    Write-Host "SUCCESS: Exception found"
    Write-Host "Interface Type: $($response.interfaceType)"
    Write-Host "Status: $($response.status)"
    Write-Host "Transaction ID: $($response.transactionId)"
    
    # Now let's test if we can trigger the PayloadRetrievalService indirectly
    Write-Host "`nTesting payload retrieval (this should use our new PartnerOrderServiceClient)..."
    
    # Check if there's a payload endpoint
    try {
        $payloadResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/b36e43a5-4367-4f22-8718-5f8efd1fc4ef/payload" -Method GET -Headers $headers
        Write-Host "SUCCESS: Payload retrieved using REST client"
        Write-Host "Payload Response: $($payloadResponse | ConvertTo-Json -Depth 2)"
    } catch {
        Write-Host "Payload endpoint not available or failed: $($_.Exception.Message)"
    }
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody"
    }
}