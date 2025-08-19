#!/bin/bash

# GraphQL API Load Testing Script
# Tests performance requirements for the Interface Exception GraphQL API

set -e

# Configuration
GRAPHQL_ENDPOINT="${GRAPHQL_ENDPOINT:-http://localhost:8080/graphql}"
CONCURRENT_USERS="${CONCURRENT_USERS:-50}"
DURATION="${DURATION:-60}"
RAMP_UP="${RAMP_UP:-10}"
JWT_TOKEN="${JWT_TOKEN:-}"

# Performance thresholds
LIST_QUERY_THRESHOLD_MS=500
DETAIL_QUERY_THRESHOLD_MS=1000
SUMMARY_QUERY_THRESHOLD_MS=200
MIN_THROUGHPUT_QPS=50

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🚀 Starting GraphQL API Load Testing"
echo "======================================"
echo "Endpoint: $GRAPHQL_ENDPOINT"
echo "Concurrent Users: $CONCURRENT_USERS"
echo "Duration: ${DURATION}s"
echo "Ramp-up: ${RAMP_UP}s"
echo ""

# Check if required tools are installed
check_dependencies() {
    echo "📋 Checking dependencies..."
    
    if ! command -v curl &> /dev/null; then
        echo -e "${RED}❌ curl is required but not installed${NC}"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}❌ jq is required but not installed${NC}"
        exit 1
    fi
    
    if ! command -v bc &> /dev/null; then
        echo -e "${RED}❌ bc is required but not installed${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ All dependencies available${NC}"
}

# Test GraphQL endpoint availability
test_endpoint() {
    echo "🔍 Testing endpoint availability..."
    
    local health_query='{"query": "{ __typename }"}'
    local response=$(curl -s -w "%{http_code}" -o /tmp/graphql_health_response \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$health_query" \
        "$GRAPHQL_ENDPOINT")
    
    if [ "$response" != "200" ]; then
        echo -e "${RED}❌ GraphQL endpoint not available (HTTP $response)${NC}"
        cat /tmp/graphql_health_response
        exit 1
    fi
    
    echo -e "${GREEN}✅ GraphQL endpoint is available${NC}"
}

# Execute a single GraphQL query and measure response time
execute_query() {
    local query="$1"
    local query_name="$2"
    
    local start_time=$(date +%s%3N)
    local response=$(curl -s -w "%{http_code}" -o /tmp/graphql_response \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$query" \
        "$GRAPHQL_ENDPOINT")
    local end_time=$(date +%s%3N)
    
    local response_time=$((end_time - start_time))
    local http_code="$response"
    
    # Check for GraphQL errors
    local has_errors=$(jq -r '.errors // empty' /tmp/graphql_response 2>/dev/null || echo "")
    
    if [ "$http_code" != "200" ] || [ -n "$has_errors" ]; then
        echo "ERROR,$query_name,$response_time,$http_code"
    else
        echo "SUCCESS,$query_name,$response_time,$http_code"
    fi
}

# Test exception list query performance
test_list_query_performance() {
    echo "📊 Testing exception list query performance..."
    
    local query='{
        "query": "{ exceptions(pagination: { first: 20 }, sorting: { field: \"timestamp\", direction: DESC }) { edges { node { transactionId interfaceType status severity timestamp exceptionReason } } pageInfo { hasNextPage endCursor } } }"
    }'
    
    local results_file="/tmp/list_query_results.csv"
    echo "result,query_name,response_time_ms,http_code" > "$results_file"
    
    echo "Executing 100 list queries..."
    for i in $(seq 1 100); do
        execute_query "$query" "list_query" >> "$results_file"
        if [ $((i % 20)) -eq 0 ]; then
            echo "  Progress: $i/100"
        fi
    done
    
    # Calculate statistics
    local avg_time=$(awk -F',' '$1=="SUCCESS" {sum+=$3; count++} END {if(count>0) print sum/count; else print 0}' "$results_file")
    local p95_time=$(awk -F',' '$1=="SUCCESS" {print $3}' "$results_file" | sort -n | awk '{a[NR]=$1} END {print a[int(NR*0.95)]}')
    local success_rate=$(awk -F',' 'NR>1 {total++; if($1=="SUCCESS") success++} END {if(total>0) print (success/total)*100; else print 0}' "$results_file")
    
    echo "📈 List Query Results:"
    echo "  Average Response Time: ${avg_time}ms"
    echo "  P95 Response Time: ${p95_time}ms"
    echo "  Success Rate: ${success_rate}%"
    
    # Validate against requirements
    if (( $(echo "$p95_time <= $LIST_QUERY_THRESHOLD_MS" | bc -l) )); then
        echo -e "  ${GREEN}✅ P95 response time meets requirement (≤${LIST_QUERY_THRESHOLD_MS}ms)${NC}"
    else
        echo -e "  ${RED}❌ P95 response time exceeds requirement (${p95_time}ms > ${LIST_QUERY_THRESHOLD_MS}ms)${NC}"
    fi
}

# Test exception detail query performance
test_detail_query_performance() {
    echo "📊 Testing exception detail query performance..."
    
    local query='{
        "query": "{ exception(transactionId: \"test-transaction-001\") { transactionId interfaceType status severity exceptionReason originalPayload { content contentType } retryHistory { attemptNumber status initiatedAt } } }"
    }'
    
    local results_file="/tmp/detail_query_results.csv"
    echo "result,query_name,response_time_ms,http_code" > "$results_file"
    
    echo "Executing 50 detail queries..."
    for i in $(seq 1 50); do
        execute_query "$query" "detail_query" >> "$results_file"
        if [ $((i % 10)) -eq 0 ]; then
            echo "  Progress: $i/50"
        fi
    done
    
    # Calculate statistics
    local avg_time=$(awk -F',' '$1=="SUCCESS" {sum+=$3; count++} END {if(count>0) print sum/count; else print 0}' "$results_file")
    local p95_time=$(awk -F',' '$1=="SUCCESS" {print $3}' "$results_file" | sort -n | awk '{a[NR]=$1} END {print a[int(NR*0.95)]}')
    local success_rate=$(awk -F',' 'NR>1 {total++; if($1=="SUCCESS") success++} END {if(total>0) print (success/total)*100; else print 0}' "$results_file")
    
    echo "📈 Detail Query Results:"
    echo "  Average Response Time: ${avg_time}ms"
    echo "  P95 Response Time: ${p95_time}ms"
    echo "  Success Rate: ${success_rate}%"
    
    # Validate against requirements
    if (( $(echo "$p95_time <= $DETAIL_QUERY_THRESHOLD_MS" | bc -l) )); then
        echo -e "  ${GREEN}✅ P95 response time meets requirement (≤${DETAIL_QUERY_THRESHOLD_MS}ms)${NC}"
    else
        echo -e "  ${RED}❌ P95 response time exceeds requirement (${p95_time}ms > ${DETAIL_QUERY_THRESHOLD_MS}ms)${NC}"
    fi
}

# Test summary query performance
test_summary_query_performance() {
    echo "📊 Testing summary query performance..."
    
    local query='{
        "query": "{ exceptionSummary(timeRange: LAST_24_HOURS) { totalExceptions byInterfaceType { interfaceType count percentage } bySeverity { severity count percentage } keyMetrics { retrySuccessRate avgResolutionTime } } }"
    }'
    
    local results_file="/tmp/summary_query_results.csv"
    echo "result,query_name,response_time_ms,http_code" > "$results_file"
    
    echo "Executing 50 summary queries..."
    for i in $(seq 1 50); do
        execute_query "$query" "summary_query" >> "$results_file"
        if [ $((i % 10)) -eq 0 ]; then
            echo "  Progress: $i/50"
        fi
    done
    
    # Calculate statistics
    local avg_time=$(awk -F',' '$1=="SUCCESS" {sum+=$3; count++} END {if(count>0) print sum/count; else print 0}' "$results_file")
    local p95_time=$(awk -F',' '$1=="SUCCESS" {print $3}' "$results_file" | sort -n | awk '{a[NR]=$1} END {print a[int(NR*0.95)]}')
    local success_rate=$(awk -F',' 'NR>1 {total++; if($1=="SUCCESS") success++} END {if(total>0) print (success/total)*100; else print 0}' "$results_file")
    
    echo "📈 Summary Query Results:"
    echo "  Average Response Time: ${avg_time}ms"
    echo "  P95 Response Time: ${p95_time}ms"
    echo "  Success Rate: ${success_rate}%"
    
    # Validate against requirements
    if (( $(echo "$p95_time <= $SUMMARY_QUERY_THRESHOLD_MS" | bc -l) )); then
        echo -e "  ${GREEN}✅ P95 response time meets requirement (≤${SUMMARY_QUERY_THRESHOLD_MS}ms)${NC}"
    else
        echo -e "  ${RED}❌ P95 response time exceeds requirement (${p95_time}ms > ${SUMMARY_QUERY_THRESHOLD_MS}ms)${NC}"
    fi
}

# Test concurrent load performance
test_concurrent_load() {
    echo "🔥 Testing concurrent load performance..."
    
    local query='{
        "query": "{ exceptions(pagination: { first: 10 }) { edges { node { transactionId status timestamp } } } }"
    }'
    
    local results_file="/tmp/concurrent_load_results.csv"
    echo "result,query_name,response_time_ms,http_code" > "$results_file"
    
    echo "Starting $CONCURRENT_USERS concurrent users for ${DURATION}s..."
    
    # Start concurrent processes
    local pids=()
    local start_time=$(date +%s)
    local end_time=$((start_time + DURATION))
    
    for i in $(seq 1 $CONCURRENT_USERS); do
        (
            while [ $(date +%s) -lt $end_time ]; do
                execute_query "$query" "concurrent_query" >> "$results_file"
                sleep 0.1  # Small delay between requests
            done
        ) &
        pids+=($!)
        
        # Ramp up gradually
        if [ $((i % (CONCURRENT_USERS / RAMP_UP))) -eq 0 ]; then
            sleep 1
        fi
    done
    
    # Wait for all processes to complete
    for pid in "${pids[@]}"; do
        wait $pid
    done
    
    # Calculate statistics
    local total_requests=$(awk 'NR>1 {count++} END {print count}' "$results_file")
    local successful_requests=$(awk -F',' '$1=="SUCCESS" {count++} END {print count}' "$results_file")
    local throughput=$(echo "scale=2; $successful_requests / $DURATION" | bc)
    local success_rate=$(echo "scale=2; ($successful_requests / $total_requests) * 100" | bc)
    local avg_time=$(awk -F',' '$1=="SUCCESS" {sum+=$3; count++} END {if(count>0) print sum/count; else print 0}' "$results_file")
    
    echo "📈 Concurrent Load Results:"
    echo "  Total Requests: $total_requests"
    echo "  Successful Requests: $successful_requests"
    echo "  Throughput: ${throughput} qps"
    echo "  Success Rate: ${success_rate}%"
    echo "  Average Response Time: ${avg_time}ms"
    
    # Validate against requirements
    if (( $(echo "$throughput >= $MIN_THROUGHPUT_QPS" | bc -l) )); then
        echo -e "  ${GREEN}✅ Throughput meets requirement (≥${MIN_THROUGHPUT_QPS} qps)${NC}"
    else
        echo -e "  ${RED}❌ Throughput below requirement (${throughput} qps < ${MIN_THROUGHPUT_QPS} qps)${NC}"
    fi
}

# Generate performance report
generate_report() {
    echo ""
    echo "📋 Performance Test Summary"
    echo "=========================="
    
    local report_file="/tmp/graphql_performance_report.txt"
    {
        echo "GraphQL API Performance Test Report"
        echo "Generated: $(date)"
        echo "Endpoint: $GRAPHQL_ENDPOINT"
        echo "Concurrent Users: $CONCURRENT_USERS"
        echo "Test Duration: ${DURATION}s"
        echo ""
        
        echo "=== List Query Performance ==="
        if [ -f "/tmp/list_query_results.csv" ]; then
            awk -F',' 'NR>1 && $1=="SUCCESS" {sum+=$3; count++} END {
                if(count>0) {
                    avg=sum/count;
                    print "Average Response Time: " avg "ms";
                    print "Total Successful Queries: " count;
                }
            }' /tmp/list_query_results.csv
        fi
        echo ""
        
        echo "=== Detail Query Performance ==="
        if [ -f "/tmp/detail_query_results.csv" ]; then
            awk -F',' 'NR>1 && $1=="SUCCESS" {sum+=$3; count++} END {
                if(count>0) {
                    avg=sum/count;
                    print "Average Response Time: " avg "ms";
                    print "Total Successful Queries: " count;
                }
            }' /tmp/detail_query_results.csv
        fi
        echo ""
        
        echo "=== Summary Query Performance ==="
        if [ -f "/tmp/summary_query_results.csv" ]; then
            awk -F',' 'NR>1 && $1=="SUCCESS" {sum+=$3; count++} END {
                if(count>0) {
                    avg=sum/count;
                    print "Average Response Time: " avg "ms";
                    print "Total Successful Queries: " count;
                }
            }' /tmp/summary_query_results.csv
        fi
        echo ""
        
        echo "=== Concurrent Load Performance ==="
        if [ -f "/tmp/concurrent_load_results.csv" ]; then
            awk -F',' 'NR>1 {total++; if($1=="SUCCESS") {success++; sum+=$3}} END {
                if(total>0) {
                    throughput=success/'$DURATION';
                    success_rate=(success/total)*100;
                    avg_time=sum/success;
                    print "Total Requests: " total;
                    print "Successful Requests: " success;
                    print "Throughput: " throughput " qps";
                    print "Success Rate: " success_rate "%";
                    print "Average Response Time: " avg_time "ms";
                }
            }' /tmp/concurrent_load_results.csv
        fi
        
    } > "$report_file"
    
    echo "📄 Full report saved to: $report_file"
    cat "$report_file"
}

# Cleanup function
cleanup() {
    echo "🧹 Cleaning up temporary files..."
    rm -f /tmp/graphql_*_results.csv
    rm -f /tmp/graphql_response
    rm -f /tmp/graphql_health_response
}

# Main execution
main() {
    trap cleanup EXIT
    
    check_dependencies
    test_endpoint
    
    echo ""
    echo "🎯 Starting performance tests..."
    echo ""
    
    test_list_query_performance
    echo ""
    
    test_detail_query_performance
    echo ""
    
    test_summary_query_performance
    echo ""
    
    test_concurrent_load
    echo ""
    
    generate_report
    
    echo ""
    echo -e "${GREEN}🎉 Performance testing completed!${NC}"
}

# Execute main function
main "$@"