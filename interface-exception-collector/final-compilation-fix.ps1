# Final comprehensive compilation fix
Write-Host "=== Final Compilation Fix ===" -ForegroundColor Green

# 1. Fix ValidationResult import in MetricsService
Write-Host "1. Fixing ValidationResult import in MetricsService..." -ForegroundColor Cyan
$metricsServicePath = "src/main/java/com/arcone/biopro/exception/collector/application/service/MetricsService.java"
if (Test-Path $metricsServicePath) {
    $content = Get-Content $metricsServicePath -Raw
    
    # Add ValidationResult import
    if (-not ($content -match "import.*ValidationResult")) {
        $content = $content -replace "(package com\.arcone\.biopro\.exception\.collector\.application\.service;)", "`$1`n`nimport com.arcone.biopro.exception.collector.application.service.ValidationResult;"
    }
    
    Set-Content -Path $metricsServicePath -Value $content
    Write-Host "   ✅ Fixed MetricsService ValidationResult import" -ForegroundColor Green
}

# 2. Create missing enum values if needed
Write-Host "2. Checking enum values..." -ForegroundColor Cyan

# Check if RetryStatus enum exists and has needed values
$retryStatusPath = "src/main/java/com/arcone/biopro/exception/collector/domain/enums/RetryStatus.java"
if (-not (Test-Path $retryStatusPath)) {
    Write-Host "   Creating RetryStatus enum..." -ForegroundColor Yellow
    $retryStatusContent = @"
package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the status of a retry attempt.
 */
public enum RetryStatus {
    PENDING,
    SUCCESS,
    FAILED,
    COMPLETED
}
"@
    New-Item -ItemType Directory -Path (Split-Path $retryStatusPath -Parent) -Force | Out-Null
    Set-Content -Path $retryStatusPath -Value $retryStatusContent
    Write-Host "   ✅ Created RetryStatus enum" -ForegroundColor Green
}

# Check if InterfaceType enum exists and has ORDER value
$interfaceTypePath = "src/main/java/com/arcone/biopro/exception/collector/domain/enums/InterfaceType.java"
if (-not (Test-Path $interfaceTypePath)) {
    Write-Host "   Creating InterfaceType enum..." -ForegroundColor Yellow
    $interfaceTypeContent = @"
package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the type of interface.
 */
public enum InterfaceType {
    ORDER,
    INVENTORY,
    BILLING,
    SHIPPING,
    CUSTOMER
}
"@
    Set-Content -Path $interfaceTypePath -Value $interfaceTypeContent
    Write-Host "   ✅ Created InterfaceType enum" -ForegroundColor Green
}

# Check if ExceptionSeverity enum exists
$exceptionSeverityPath = "src/main/java/com/arcone/biopro/exception/collector/domain/enums/ExceptionSeverity.java"
if (-not (Test-Path $exceptionSeverityPath)) {
    Write-Host "   Creating ExceptionSeverity enum..." -ForegroundColor Yellow
    $exceptionSeverityContent = @"
package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the severity of an exception.
 */
public enum ExceptionSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
"@
    Set-Content -Path $exceptionSeverityPath -Value $exceptionSeverityContent
    Write-Host "   ✅ Created ExceptionSeverity enum" -ForegroundColor Green
}

# Check if ResolutionMethod enum exists
$resolutionMethodPath = "src/main/java/com/arcone/biopro/exception/collector/domain/enums/ResolutionMethod.java"
if (-not (Test-Path $resolutionMethodPath)) {
    Write-Host "   Creating ResolutionMethod enum..." -ForegroundColor Yellow
    $resolutionMethodContent = @"
package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the method used to resolve an exception.
 */
public enum ResolutionMethod {
    MANUAL,
    AUTOMATIC,
    RETRY,
    ESCALATION
}
"@
    Set-Content -Path $resolutionMethodPath -Value $resolutionMethodContent
    Write-Host "   ✅ Created ResolutionMethod enum" -ForegroundColor Green
}

# 3. Create missing OrderItem entity if needed
Write-Host "3. Checking for OrderItem entity..." -ForegroundColor Cyan
$orderItemPath = "src/main/java/com/arcone/biopro/exception/collector/domain/entity/OrderItem.java"
if (-not (Test-Path $orderItemPath)) {
    Write-Host "   Creating OrderItem entity..." -ForegroundColor Yellow
    $orderItemContent = @"
package com.arcone.biopro.exception.collector.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity representing an order item associated with an interface exception.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "product_family")
    private String productFamily;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "comments")
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exception_id")
    @JsonBackReference("exception-orderItems")
    private InterfaceException interfaceException;
}
"@
    Set-Content -Path $orderItemPath -Value $orderItemContent
    Write-Host "   ✅ Created OrderItem entity" -ForegroundColor Green
}

Write-Host "=== Final Compilation Fix Complete ===" -ForegroundColor Green
Write-Host "Running final test compilation..." -ForegroundColor Cyan

mvn test-compile -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "🎉 SUCCESS: All compilation issues resolved!" -ForegroundColor Green
    
    # Try a quick test run to verify everything works
    Write-Host "`nRunning a quick test to verify..." -ForegroundColor Cyan
    mvn test -Dtest=*Test -q | Select-Object -First 10
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Tests are also running successfully!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Compilation works but some tests may need runtime fixes" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ Still have compilation issues" -ForegroundColor Red
    mvn test-compile 2>&1 | Select-String "ERROR" | Select-Object -First 3
}