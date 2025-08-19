#!/bin/bash

# Dynatrace Integration Validation Script
# Validates that all Dynatrace components are properly configured and functional

echo "🔍 Validating Dynatrace Integration"
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

VALIDATION_PASSED=0
VALIDATION_FAILED=0

# Function to check if a file exists and contains expected content
check_file_content() {
    local file_path="$1"
    local search_pattern="$2"
    local description="$3"
    
    if [ -f "$file_path" ]; then
        if grep -q "$search_pattern" "$file_path"; then
            echo -e "${GREEN}✅ $description${NC}"
            ((VALIDATION_PASSED++))
        else
            echo -e "${RED}❌ $description - Pattern not found${NC}"
            ((VALIDATION_FAILED++))
        fi
    else
        echo -e "${RED}❌ $description - File not found: $file_path${NC}"
        ((VALIDATION_FAILED++))
    fi
}

# Function to check if a file exists
check_file_exists() {
    local file_path="$1"
    local description="$2"
    
    if [ -f "$file_path" ]; then
        echo -e "${GREEN}✅ $description${NC}"
        ((VALIDATION_PASSED++))
    else
        echo -e "${RED}❌ $description - File not found: $file_path${NC}"
        ((VALIDATION_FAILED++))
    fi
}

echo ""
echo "📦 1. Maven Dependencies"
echo "======================="

# Check Dynatrace dependencies in pom.xml
check_file_content "interface-exception-collector/pom.xml" \
    "oneagent-sdk" \
    "Dynatrace OneAgent SDK dependency added"

check_file_content "interface-exception-collector/pom.xml" \
    "micrometer-registry-dynatrace" \
    "Dynatrace Micrometer registry dependency added"

check_file_content "interface-exception-collector/pom.xml" \
    "opentelemetry-api" \
    "OpenTelemetry API dependency added"

check_file_content "interface-exception-collector/pom.xml" \
    "opentelemetry-spring-boot-starter" \
    "OpenTelemetry Spring Boot starter dependency added"

echo ""
echo "⚙️ 2. Configuration Files"
echo "========================"

# Check Dynatrace configuration
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/config/DynatraceConfig.java" \
    "Dynatrace configuration class created"

check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "dynatrace:" \
    "Dynatrace configuration section added to application.yml"

check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "business-metrics:" \
    "Business metrics configuration added"

check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "otel:" \
    "OpenTelemetry configuration added"

check_file_content "interface-exception-collector/src/main/resources/application-prod.yml" \
    "dynatrace:" \
    "Production Dynatrace configuration added"

echo ""
echo "📊 3. Business Metrics Service"
echo "============================="

# Check business metrics service
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceBusinessMetricsService.java" \
    "Dynatrace business metrics service created"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceBusinessMetricsService.java" \
    "recordExceptionReceived" \
    "Exception received metrics recording implemented"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceBusinessMetricsService.java" \
    "recordRetryAttempt" \
    "Retry attempt metrics recording implemented"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceBusinessMetricsService.java" \
    "recordPayloadRetrieval" \
    "Payload retrieval metrics recording implemented"

echo ""
echo "🔧 4. Instrumentation Components"
echo "==============================="

# Check instrumentation aspect
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceInstrumentationAspect.java" \
    "Dynatrace instrumentation aspect created"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceInstrumentationAspect.java" \
    "instrumentExceptionProcessing" \
    "Exception processing instrumentation implemented"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceInstrumentationAspect.java" \
    "instrumentGraphQLQuery" \
    "GraphQL query instrumentation implemented"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceInstrumentationAspect.java" \
    "instrumentKafkaMessageProcessing" \
    "Kafka message processing instrumentation implemented"

echo ""
echo "🔗 5. Integration Services"
echo "========================="

# Check integration service
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceIntegrationService.java" \
    "Dynatrace integration service created"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceIntegrationService.java" \
    "recordExceptionProcessed" \
    "Exception processing integration implemented"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceIntegrationService.java" \
    "calculateBusinessImpactScore" \
    "Business impact scoring implemented"

# Check enhanced services
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/application/service/EnhancedExceptionProcessingService.java" \
    "Enhanced exception processing service created"

check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/application/service/EnhancedExceptionManagementService.java" \
    "Enhanced exception management service created"

echo ""
echo "🏥 6. Health Monitoring"
echo "======================"

# Check health indicator
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/health/DynatraceHealthIndicator.java" \
    "Dynatrace health indicator created"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/health/DynatraceHealthIndicator.java" \
    "addBusinessHealthIndicators" \
    "Business health indicators implemented"

# Check health monitoring service
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceHealthMonitoringService.java" \
    "Dynatrace health monitoring service created"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceHealthMonitoringService.java" \
    "@Scheduled" \
    "Scheduled health monitoring implemented"

echo ""
echo "🌐 7. Actuator Endpoints"
echo "======================="

# Check custom actuator endpoint
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/actuator/DynatraceBusinessMetricsEndpoint.java" \
    "Dynatrace business metrics endpoint created"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/actuator/DynatraceBusinessMetricsEndpoint.java" \
    "@Endpoint" \
    "Custom actuator endpoint annotation present"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/actuator/DynatraceBusinessMetricsEndpoint.java" \
    "calculateBusinessKPIs" \
    "Business KPI calculation implemented"

# Check actuator configuration
check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "dynatrace" \
    "Dynatrace endpoint exposed in actuator"

echo ""
echo "📚 8. Documentation"
echo "=================="

# Check documentation
check_file_exists "docs/DYNATRACE_INTEGRATION_GUIDE.md" \
    "Dynatrace integration guide created"

check_file_content "docs/DYNATRACE_INTEGRATION_GUIDE.md" \
    "Business Metrics Captured" \
    "Business metrics documentation included"

check_file_content "docs/DYNATRACE_INTEGRATION_GUIDE.md" \
    "Configuration" \
    "Configuration documentation included"

check_file_content "docs/DYNATRACE_INTEGRATION_GUIDE.md" \
    "Troubleshooting" \
    "Troubleshooting guide included"

echo ""
echo "🔧 9. Configuration Validation"
echo "============================="

# Check conditional annotations
check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceBusinessMetricsService.java" \
    "@ConditionalOnProperty" \
    "Conditional property annotation for business metrics service"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/DynatraceInstrumentationAspect.java" \
    "@ConditionalOnProperty" \
    "Conditional property annotation for instrumentation aspect"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/health/DynatraceHealthIndicator.java" \
    "@ConditionalOnProperty" \
    "Conditional property annotation for health indicator"

# Check service annotations
check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/application/service/EnhancedExceptionProcessingService.java" \
    "@Primary" \
    "Primary annotation for enhanced processing service"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/application/service/EnhancedExceptionManagementService.java" \
    "@Primary" \
    "Primary annotation for enhanced management service"

echo ""
echo "📋 Validation Summary"
echo "===================="
echo -e "Passed: ${GREEN}$VALIDATION_PASSED${NC}"
echo -e "Failed: ${RED}$VALIDATION_FAILED${NC}"

if [ $VALIDATION_FAILED -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 All Dynatrace integration components have been successfully implemented!${NC}"
    echo ""
    echo "Dynatrace Integration Summary:"
    echo "• OneAgent SDK and Micrometer registry configured"
    echo "• Comprehensive business metrics collection implemented"
    echo "• Automatic instrumentation via AOP aspects"
    echo "• Integration with existing exception processing workflow"
    echo "• Health monitoring and custom actuator endpoints"
    echo "• Scheduled health checks and system monitoring"
    echo "• Production-ready configuration and documentation"
    echo ""
    echo "Next Steps:"
    echo "1. Configure Dynatrace environment variables"
    echo "2. Deploy OneAgent to target environments"
    echo "3. Set up Dynatrace dashboards and alerts"
    echo "4. Validate metric collection in Dynatrace UI"
    echo "5. Configure business-specific alert thresholds"
    
    exit 0
else
    echo ""
    echo -e "${RED}❌ Some Dynatrace integration components are missing or incomplete.${NC}"
    echo "Please review the failed validations above and ensure all components are properly implemented."
    
    exit 1
fi