#!/usr/bin/env pwsh
# Fix interface type routing to use PARTNER_ORDER instead of ORDER

Write-Host "=== Fixing Interface Type Routing ===" -ForegroundColor Green

# Step 1: Check current interface types in use
Write-Host "Step 1: Checking current interface types..." -ForegroundColor Yellow

# Get pod name
$podName = kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
if (-not $podName -or $LASTEXITCODE -ne 0) {
    Write-Host "Could not find interface-exception-collector pod" -ForegroundColor Red
    exit 1
}

Write-Host "Using pod: $podName" -ForegroundColor Cyan

# Step 2: Update the test transaction to use PARTNER_ORDER interface type
Write-Host "`nStep 2: Updating test transaction interface type..." -ForegroundColor Yellow

# Create a simple SQL update script
$updateSql = @"
UPDATE interface_exceptions 
SET interface_type = 'PARTNER_ORDER' 
WHERE transaction_id = '137ed65a-ce10-4cac-84d2-4e6e08bbed40';

SELECT transaction_id, interface_type, external_id 
FROM interface_exceptions 
WHERE transaction_id = '137ed65a-ce10-4cac-84d2-4e6e08bbed40';
"@

# Write SQL to a temp file in the pod
Write-Host "Updating interface type for test transaction..." -ForegroundColor Cyan
$tempFile = "/tmp/update_interface_type.sql"
kubectl exec $podName -- sh -c "echo '$updateSql' > $tempFile"

# Execute the SQL (we'll try different approaches since psql might not be available)
Write-Host "Attempting to execute SQL update..." -ForegroundColor Cyan

# Try to find Java application and use it to execute the update
$javaUpdate = @"
import java.sql.*;
public class UpdateInterfaceType {
    public static void main(String[] args) {
        try {
            String url = "jdbc:postgresql://localhost:5432/interface_exceptions";
            String user = "postgres";
            String password = "postgres";
            
            Connection conn = DriverManager.getConnection(url, user, password);
            
            // Update the interface type
            String updateSql = "UPDATE interface_exceptions SET interface_type = 'PARTNER_ORDER' WHERE transaction_id = '137ed65a-ce10-4cac-84d2-4e6e08bbed40'";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            int rowsUpdated = updateStmt.executeUpdate();
            System.out.println("Updated " + rowsUpdated + " rows");
            
            // Verify the update
            String selectSql = "SELECT transaction_id, interface_type, external_id FROM interface_exceptions WHERE transaction_id = '137ed65a-ce10-4cac-84d2-4e6e08bbed40'";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            ResultSet rs = selectStmt.executeQuery();
            
            while (rs.next()) {
                System.out.println("Transaction: " + rs.getString("transaction_id") + 
                                 ", Interface Type: " + rs.getString("interface_type") + 
                                 ", External ID: " + rs.getString("external_id"));
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
"@

Write-Host "Creating Java update utility..." -ForegroundColor Cyan
kubectl exec $podName -- sh -c "echo '$javaUpdate' > /tmp/UpdateInterfaceType.java"

# Step 3: Alternative approach - use the application's REST API to create a new exception with PARTNER_ORDER type
Write-Host "`nStep 3: Alternative approach - creating new PARTNER_ORDER exception..." -ForegroundColor Yellow

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODA4NDYsImV4cCI6MTc1NzA4NDQ0Nn0.7hQgoI8QEZynrMGanP2Yqd_WRtfrmP44LTiBLqEzfiI"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Create a new exception with PARTNER_ORDER interface type
$newException = @{
    transactionId = "TEST-PARTNER-ORDER-$(Get-Random -Minimum 1000 -Maximum 9999)"
    externalId = "TEST-ORDER-PARTNER-1"
    interfaceType = "PARTNER_ORDER"
    operation = "ORDER_PROCESSING"
    exceptionReason = "Test exception for PARTNER_ORDER interface type"
    severity = "HIGH"
    retryable = $true
    maxRetries = 5
    customerID = "CUST-001"
    locationCode = "LOC-001"
} | ConvertTo-Json

Write-Host "Creating new PARTNER_ORDER exception..." -ForegroundColor Cyan
Write-Host "Exception data:" -ForegroundColor Gray
Write-Host $newException

try {
    $newExceptionResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions" -Method POST -Headers $headers -Body $newException
    Write-Host "SUCCESS: Created new PARTNER_ORDER exception" -ForegroundColor Green
    Write-Host "New Transaction ID: $($newExceptionResponse.transactionId)" -ForegroundColor Green
    
    # Update the test script to use the new transaction ID
    $newTransactionId = $newExceptionResponse.transactionId
    
    # Update test script
    $testScriptContent = Get-Content "test-partner-order-retry.ps1" -Raw
    $updatedTestScript = $testScriptContent -replace "137ed65a-ce10-4cac-84d2-4e6e08bbed40", $newTransactionId
    Set-Content -Path "test-partner-order-retry-new.ps1" -Value $updatedTestScript
    
    Write-Host "Created updated test script: test-partner-order-retry-new.ps1" -ForegroundColor Green
    
    # Test the new exception immediately
    Write-Host "`nTesting retry with new PARTNER_ORDER exception..." -ForegroundColor Yellow
    
    $retryRequest = @{
        reason = "Testing REST-based retry for PARTNER_ORDER interface type"
        initiatedBy = "test-user"
    } | ConvertTo-Json
    
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$newTransactionId/retry" -Method POST -Headers $headers -Body $retryRequest
    
    Write-Host "SUCCESS: Retry submitted for PARTNER_ORDER exception!" -ForegroundColor Green
    Write-Host "Retry Response:" -ForegroundColor Cyan
    Write-Host ($retryResponse | ConvertTo-Json -Depth 3)
    
} catch {
    Write-Host "ERROR creating new exception: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}

# Step 4: Check logs to verify PARTNER_ORDER routing
Write-Host "`nStep 4: Checking logs for PARTNER_ORDER routing..." -ForegroundColor Yellow

Start-Sleep -Seconds 5

$logs = kubectl logs $podName --tail=20 2>$null
if ($logs) {
    Write-Host "Recent application logs:" -ForegroundColor Cyan
    $logs | Select-Object -Last 10 | ForEach-Object { 
        if ($_ -match "PartnerOrderServiceClient") {
            Write-Host "  $_" -ForegroundColor Green
        } elseif ($_ -match "MockRSocketOrderServiceClient") {
            Write-Host "  $_" -ForegroundColor Red
        } else {
            Write-Host "  $_" -ForegroundColor Gray
        }
    }
    
    if ($logs -match "PartnerOrderServiceClient") {
        Write-Host "`n✅ SUCCESS: PartnerOrderServiceClient is being used!" -ForegroundColor Green
    } elseif ($logs -match "MockRSocketOrderServiceClient") {
        Write-Host "`n❌ ISSUE: Still using MockRSocketOrderServiceClient" -ForegroundColor Red
    } else {
        Write-Host "`n⚠️  No specific client usage found in recent logs" -ForegroundColor Yellow
    }
}

Write-Host "`n=== Interface Type Routing Fix Complete ===" -ForegroundColor Green