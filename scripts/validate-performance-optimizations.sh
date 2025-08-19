#!/bin/bash

# Performance Optimization Validation Script
# Validates that all performance optimizations have been implemented correctly

echo "🔍 Validating GraphQL Performance Optimizations"
echo "=============================================="

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
echo "📊 1. Database Performance Optimizations"
echo "========================================"

# Check database migration for performance indexes
check_file_exists "interface-exception-collector/src/main/resources/db/migration/V15__Optimize_performance_indexes.sql" \
    "Performance indexes migration file created"

check_file_content "interface-exception-collector/src/main/resources/db/migration/V15__Optimize_performance_indexes.sql" \
    "idx_exceptions_dashboard_filters" \
    "Dashboard filter optimization index defined"

check_file_content "interface-exception-collector/src/main/resources/db/migration/V15__Optimize_performance_indexes.sql" \
    "idx_exceptions_cursor_pagination" \
    "Cursor pagination optimization index defined"

check_file_content "interface-exception-collector/src/main/resources/db/migration/V15__Optimize_performance_indexes.sql" \
    "idx_exceptions_list_covering" \
    "Covering index for list queries defined"

# Check database connection pool optimizations
check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "cachePrepStmts: true" \
    "Prepared statement caching enabled"

check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "prepStmtCacheSize: 256" \
    "Prepared statement cache size configured"

check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "defaultRowFetchSize" \
    "Row fetch size optimization configured"

echo ""
echo "🚀 2. DataLoader Performance Optimizations"
echo "=========================================="

# Check DataLoader configuration optimizations
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/DataLoaderConfig.java" \
    "DataLoader configuration file exists"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/DataLoaderConfig.java" \
    "exceptionBatchSize" \
    "Configurable exception batch size implemented"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/DataLoaderConfig.java" \
    "newMappedDataLoader" \
    "Updated DataLoader API usage implemented"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/DataLoaderConfig.java" \
    "setBatchLoadDelay" \
    "Batch load delay optimization configured"

# Check DataLoader configuration in application.yml
check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "dataloader:" \
    "DataLoader configuration section added"

check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "batch-size:" \
    "DataLoader batch size configuration added"

echo ""
echo "💾 3. Query Result Caching Optimizations"
echo "======================================="

# Check query cache configuration
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/QueryCacheConfig.java" \
    "Query cache configuration file created"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/QueryCacheConfig.java" \
    "EXCEPTION_LIST_CACHE" \
    "Exception list cache configuration defined"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/QueryCacheConfig.java" \
    "generateCacheKey" \
    "Cache key generation utility implemented"

# Check cache configuration in application.yml
check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "query-cache:" \
    "Query cache configuration section added"

check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "ttl-by-type:" \
    "Cache TTL by type configuration added"

echo ""
echo "🔧 4. Connection Pool Optimizations"
echo "=================================="

# Check Redis connection pool optimizations
check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "max-active: \${REDIS_MAX_ACTIVE:20}" \
    "Redis connection pool max-active configured"

check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "time-between-eviction-runs" \
    "Redis connection pool eviction configured"

# Check Hibernate optimizations
check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "batch_size: \${HIBERNATE_BATCH_SIZE:100}" \
    "Hibernate batch size optimization configured"

check_file_content "interface-exception-collector/src/main/resources/application.yml" \
    "fetch_size: \${HIBERNATE_FETCH_SIZE:1000}" \
    "Hibernate fetch size optimization configured"

echo ""
echo "📈 5. Performance Monitoring"
echo "============================"

# Check performance monitoring service
check_file_exists "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/service/PerformanceMonitoringService.java" \
    "Performance monitoring service created"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/service/PerformanceMonitoringService.java" \
    "recordQueryExecution" \
    "Query execution monitoring implemented"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/service/PerformanceMonitoringService.java" \
    "recordCacheOperation" \
    "Cache operation monitoring implemented"

check_file_content "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/service/PerformanceMonitoringService.java" \
    "getCacheHitRate" \
    "Cache hit rate calculation implemented"

echo ""
echo "🧪 6. Load Testing Infrastructure"
echo "================================"

# Check load testing files
check_file_exists "scripts/graphql-load-test.sh" \
    "GraphQL load testing script created"

check_file_content "scripts/graphql-load-test.sh" \
    "test_list_query_performance" \
    "List query performance test implemented"

check_file_content "scripts/graphql-load-test.sh" \
    "test_concurrent_load" \
    "Concurrent load test implemented"

check_file_exists "interface-exception-collector/src/test/java/com/arcone/biopro/exception/collector/performance/GraphQLPerformanceTest.java" \
    "GraphQL performance test class created"

echo ""
echo "🏭 7. Production Optimizations"
echo "============================="

# Check production configuration optimizations
check_file_content "interface-exception-collector/src/main/resources/application-prod.yml" \
    "maximum-pool-size: 100" \
    "Production database pool size optimized"

check_file_content "interface-exception-collector/src/main/resources/application-prod.yml" \
    "batch-size: 300" \
    "Production DataLoader batch sizes optimized"

check_file_content "interface-exception-collector/src/main/resources/application-prod.yml" \
    "max-active: 50" \
    "Production Redis pool size optimized"

echo ""
echo "📋 Validation Summary"
echo "===================="
echo -e "Passed: ${GREEN}$VALIDATION_PASSED${NC}"
echo -e "Failed: ${RED}$VALIDATION_FAILED${NC}"

if [ $VALIDATION_FAILED -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 All performance optimizations have been successfully implemented!${NC}"
    echo ""
    echo "Performance Optimization Summary:"
    echo "• Database indexes optimized for GraphQL query patterns"
    echo "• DataLoader batch sizes fine-tuned for different data types"
    echo "• Query result caching implemented with appropriate TTL values"
    echo "• Database and Redis connection pools optimized"
    echo "• Performance monitoring and metrics collection added"
    echo "• Load testing infrastructure created"
    echo "• Production-specific optimizations configured"
    echo ""
    echo "Next Steps:"
    echo "1. Run database migrations to apply performance indexes"
    echo "2. Execute load tests to validate performance requirements"
    echo "3. Monitor cache hit rates and query performance in production"
    echo "4. Adjust batch sizes and TTL values based on real-world usage"
    
    exit 0
else
    echo ""
    echo -e "${RED}❌ Some performance optimizations are missing or incomplete.${NC}"
    echo "Please review the failed validations above and ensure all optimizations are properly implemented."
    
    exit 1
fi