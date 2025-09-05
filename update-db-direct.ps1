#!/usr/bin/env pwsh
# Direct database update using kubectl exec

Write-Host "=== Direct Database Update ===" -ForegroundColor Green

# Get pod name
$podName = kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
if (-not $podName -or $LASTEXITCODE -ne 0) {
    Write-Host "Could not find interface-exception-collector pod" -ForegroundColor Red
    exit 1
}

Write-Host "Using pod: $podName" -ForegroundColor Cyan

# Create a simple Java program to update the database
$javaCode = @'
import java.sql.*;

public class UpdateDB {
    public static void main(String[] args) {
        try {
            // Database connection details (assuming standard PostgreSQL setup)
            String url = "jdbc:postgresql://localhost:5432/interface_exceptions";
            String user = "postgres";
            String password = "postgres";
            
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            
            // Update the interface type
            String updateSql = "UPDATE interface_exceptions SET interface_type = ? WHERE transaction_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, "PARTNER_ORDER");
            updateStmt.setString(2, "137ed65a-ce10-4cac-84d2-4e6e08bbed40");
            
            int rowsUpdated = updateStmt.executeUpdate();
            System.out.println("Updated " + rowsUpdated + " rows");
            
            // Verify the update
            String selectSql = "SELECT transaction_id, interface_type, external_id FROM interface_exceptions WHERE transaction_id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, "137ed65a-ce10-4cac-84d2-4e6e08bbed40");
            ResultSet rs = selectStmt.executeQuery();
            
            while (rs.next()) {
                System.out.println("Transaction: " + rs.getString("transaction_id"));
                System.out.println("Interface Type: " + rs.getString("interface_type"));
                System.out.println("External ID: " + rs.getString("external_id"));
            }
            
            conn.close();
            System.out.println("Database update completed successfully");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
'@

Write-Host "Creating database update utility..." -ForegroundColor Yellow

# Write the Java code to the pod
kubectl exec $podName -- sh -c "cat > /tmp/UpdateDB.java << 'EOF'
$javaCode
EOF"

# Compile and run the Java program
Write-Host "Compiling Java program..." -ForegroundColor Yellow
$compileResult = kubectl exec $podName -- javac -cp "/app/lib/*" /tmp/UpdateDB.java 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful" -ForegroundColor Green
    
    Write-Host "Running database update..." -ForegroundColor Yellow
    $runResult = kubectl exec $podName -- java -cp "/app/lib/*:/tmp" UpdateDB 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database update result:" -ForegroundColor Green
        Write-Host $runResult
    } else {
        Write-Host "Database update failed:" -ForegroundColor Red
        Write-Host $runResult
    }
} else {
    Write-Host "Compilation failed:" -ForegroundColor Red
    Write-Host $compileResult
}

# Alternative approach - try to use environment variables to get DB connection info
Write-Host "`nTrying alternative approach with environment variables..." -ForegroundColor Yellow

$envVars = kubectl exec $podName -- env | Where-Object { $_ -match "DATABASE|POSTGRES|JDBC" }
if ($envVars) {
    Write-Host "Database-related environment variables:" -ForegroundColor Cyan
    $envVars | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
}

# Try a simple approach - create a SQL file and see if we can execute it
Write-Host "`nTrying SQL file approach..." -ForegroundColor Yellow

$sqlContent = @"
UPDATE interface_exceptions 
SET interface_type = 'PARTNER_ORDER' 
WHERE transaction_id = '137ed65a-ce10-4cac-84d2-4e6e08bbed40';

SELECT transaction_id, interface_type, external_id 
FROM interface_exceptions 
WHERE transaction_id = '137ed65a-ce10-4cac-84d2-4e6e08bbed40';
"@

kubectl exec $podName -- sh -c "echo '$sqlContent' > /tmp/update.sql"

# Check if psql is available
$psqlCheck = kubectl exec $podName -- which psql 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "psql found, executing SQL..." -ForegroundColor Green
    $sqlResult = kubectl exec $podName -- psql -h localhost -U postgres -d interface_exceptions -f /tmp/update.sql 2>&1
    Write-Host $sqlResult
} else {
    Write-Host "psql not available in pod" -ForegroundColor Yellow
}

Write-Host "`n=== Database Update Attempt Complete ===" -ForegroundColor Green