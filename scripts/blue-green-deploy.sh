#!/bin/bash

# Blue-Green Deployment Script for Interface Exception Collector Service
# This script performs zero-downtime deployments using blue-green strategy

set -euo pipefail

# Configuration
NAMESPACE="biopro-production"
APP_NAME="interface-exception-collector"
SERVICE_NAME="${APP_NAME}-service"
BLUE_DEPLOYMENT="${APP_NAME}-blue"
GREEN_DEPLOYMENT="${APP_NAME}-green"
BLUE_SERVICE="${APP_NAME}-blue"
GREEN_SERVICE="${APP_NAME}-green"
HEALTH_CHECK_PATH="/actuator/health"
READINESS_CHECK_PATH="/actuator/health/readiness"
TIMEOUT=300  # 5 minutes timeout
CHECK_INTERVAL=10  # 10 seconds between checks

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to get current active deployment
get_active_deployment() {
    local selector=$(kubectl get service "${SERVICE_NAME}" -n "${NAMESPACE}" -o jsonpath='{.spec.selector.version}' 2>/dev/null || echo "")
    echo "${selector}"
}

# Function to get inactive deployment
get_inactive_deployment() {
    local active=$(get_active_deployment)
    if [ "${active}" = "blue" ]; then
        echo "green"
    elif [ "${active}" = "green" ]; then
        echo "blue"
    else
        echo "blue"  # Default to blue if no active deployment
    fi
}

# Function to check if deployment exists
deployment_exists() {
    local deployment_name=$1
    kubectl get deployment "${deployment_name}" -n "${NAMESPACE}" >/dev/null 2>&1
}

# Function to check deployment readiness
check_deployment_ready() {
    local deployment_name=$1
    local replicas=$2
    
    log_info "Checking readiness of deployment: ${deployment_name}"
    
    local start_time=$(date +%s)
    local end_time=$((start_time + TIMEOUT))
    
    while [ $(date +%s) -lt ${end_time} ]; do
        local ready_replicas=$(kubectl get deployment "${deployment_name}" -n "${NAMESPACE}" -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
        local available_replicas=$(kubectl get deployment "${deployment_name}" -n "${NAMESPACE}" -o jsonpath='{.status.availableReplicas}' 2>/dev/null || echo "0")
        
        if [ "${ready_replicas}" = "${replicas}" ] && [ "${available_replicas}" = "${replicas}" ]; then
            log_success "Deployment ${deployment_name} is ready with ${replicas} replicas"
            return 0
        fi
        
        log_info "Waiting for deployment ${deployment_name}: ${ready_replicas}/${replicas} ready, ${available_replicas}/${replicas} available"
        sleep ${CHECK_INTERVAL}
    done
    
    log_error "Deployment ${deployment_name} failed to become ready within ${TIMEOUT} seconds"
    return 1
}

# Function to check application health
check_application_health() {
    local service_name=$1
    local version=$2
    
    log_info "Checking application health for ${service_name} (${version})"
    
    local start_time=$(date +%s)
    local end_time=$((start_time + TIMEOUT))
    
    while [ $(date +%s) -lt ${end_time} ]; do
        # Port forward to check health
        local port_forward_pid=""
        kubectl port-forward "service/${service_name}" 8080:8080 -n "${NAMESPACE}" >/dev/null 2>&1 &
        port_forward_pid=$!
        
        sleep 5  # Wait for port forward to establish
        
        # Check health endpoint
        if curl -f -s "http://localhost:8080${HEALTH_CHECK_PATH}" >/dev/null 2>&1; then
            # Check readiness endpoint
            if curl -f -s "http://localhost:8080${READINESS_CHECK_PATH}" >/dev/null 2>&1; then
                kill ${port_forward_pid} 2>/dev/null || true
                log_success "Application health check passed for ${service_name} (${version})"
                return 0
            fi
        fi
        
        kill ${port_forward_pid} 2>/dev/null || true
        log_info "Health check failed, retrying in ${CHECK_INTERVAL} seconds..."
        sleep ${CHECK_INTERVAL}
    done
    
    log_error "Application health check failed for ${service_name} (${version}) within ${TIMEOUT} seconds"
    return 1
}

# Function to perform smoke tests
run_smoke_tests() {
    local service_name=$1
    local version=$2
    
    log_info "Running smoke tests for ${service_name} (${version})"
    
    # Port forward for testing
    kubectl port-forward "service/${service_name}" 8080:8080 -n "${NAMESPACE}" >/dev/null 2>&1 &
    local port_forward_pid=$!
    
    sleep 5  # Wait for port forward to establish
    
    local test_passed=true
    
    # Test 1: Health endpoint
    if ! curl -f -s "http://localhost:8080/actuator/health" >/dev/null; then
        log_error "Smoke test failed: Health endpoint not responding"
        test_passed=false
    fi
    
    # Test 2: GraphQL endpoint (basic introspection should be disabled in prod)
    if curl -f -s -X POST "http://localhost:8080/graphql" \
        -H "Content-Type: application/json" \
        -d '{"query": "{ __schema { types { name } } }"}' | grep -q "errors"; then
        log_success "Smoke test passed: GraphQL introspection properly disabled"
    else
        log_warning "Smoke test warning: GraphQL introspection might not be properly disabled"
    fi
    
    # Test 3: Metrics endpoint
    if ! curl -f -s "http://localhost:8080/actuator/prometheus" >/dev/null; then
        log_error "Smoke test failed: Metrics endpoint not responding"
        test_passed=false
    fi
    
    # Test 4: Basic GraphQL query (requires authentication in production)
    local auth_response=$(curl -s -X POST "http://localhost:8080/graphql" \
        -H "Content-Type: application/json" \
        -d '{"query": "{ exceptions { totalCount } }"}')
    
    if echo "${auth_response}" | grep -q "Unauthorized\|Authentication"; then
        log_success "Smoke test passed: GraphQL properly requires authentication"
    else
        log_warning "Smoke test warning: GraphQL authentication might not be properly configured"
    fi
    
    kill ${port_forward_pid} 2>/dev/null || true
    
    if [ "${test_passed}" = true ]; then
        log_success "All smoke tests passed for ${service_name} (${version})"
        return 0
    else
        log_error "Some smoke tests failed for ${service_name} (${version})"
        return 1
    fi
}

# Function to switch traffic
switch_traffic() {
    local target_version=$1
    
    log_info "Switching traffic to ${target_version} deployment"
    
    # Update service selector to point to new deployment
    kubectl patch service "${SERVICE_NAME}" -n "${NAMESPACE}" \
        -p '{"spec":{"selector":{"version":"'${target_version}'"}}}'
    
    # Wait a moment for the change to propagate
    sleep 5
    
    # Verify the switch
    local current_version=$(get_active_deployment)
    if [ "${current_version}" = "${target_version}" ]; then
        log_success "Traffic successfully switched to ${target_version} deployment"
        return 0
    else
        log_error "Failed to switch traffic to ${target_version} deployment"
        return 1
    fi
}

# Function to rollback deployment
rollback_deployment() {
    local rollback_version=$1
    
    log_warning "Rolling back to ${rollback_version} deployment"
    
    if switch_traffic "${rollback_version}"; then
        log_success "Rollback to ${rollback_version} completed successfully"
    else
        log_error "Rollback to ${rollback_version} failed"
        exit 1
    fi
}

# Function to scale deployment
scale_deployment() {
    local deployment_name=$1
    local replicas=$2
    
    log_info "Scaling ${deployment_name} to ${replicas} replicas"
    
    kubectl scale deployment "${deployment_name}" -n "${NAMESPACE}" --replicas=${replicas}
    
    if [ ${replicas} -gt 0 ]; then
        check_deployment_ready "${deployment_name}" "${replicas}"
    else
        log_info "Deployment ${deployment_name} scaled down to 0 replicas"
    fi
}

# Function to deploy new version
deploy_new_version() {
    local image_tag=$1
    local target_replicas=${2:-3}
    
    log_info "Starting blue-green deployment with image tag: ${image_tag}"
    
    # Get current active and inactive deployments
    local active_version=$(get_active_deployment)
    local inactive_version=$(get_inactive_deployment)
    local inactive_deployment="${APP_NAME}-${inactive_version}"
    local inactive_service="${APP_NAME}-${inactive_version}"
    
    log_info "Active deployment: ${active_version}"
    log_info "Deploying to inactive deployment: ${inactive_version}"
    
    # Update inactive deployment with new image
    log_info "Updating ${inactive_deployment} with new image: ${image_tag}"
    kubectl set image deployment/"${inactive_deployment}" \
        interface-exception-collector="biopro/interface-exception-collector:${image_tag}" \
        -n "${NAMESPACE}"
    
    # Scale up inactive deployment
    scale_deployment "${inactive_deployment}" "${target_replicas}"
    
    # Check application health
    if ! check_application_health "${inactive_service}" "${inactive_version}"; then
        log_error "Health check failed for new deployment"
        log_info "Scaling down failed deployment"
        scale_deployment "${inactive_deployment}" 0
        exit 1
    fi
    
    # Run smoke tests
    if ! run_smoke_tests "${inactive_service}" "${inactive_version}"; then
        log_error "Smoke tests failed for new deployment"
        log_info "Scaling down failed deployment"
        scale_deployment "${inactive_deployment}" 0
        exit 1
    fi
    
    # Switch traffic to new deployment
    if ! switch_traffic "${inactive_version}"; then
        log_error "Failed to switch traffic to new deployment"
        rollback_deployment "${active_version}"
        scale_deployment "${inactive_deployment}" 0
        exit 1
    fi
    
    # Wait for traffic to stabilize
    log_info "Waiting for traffic to stabilize..."
    sleep 30
    
    # Final health check on the new active deployment
    if ! check_application_health "${SERVICE_NAME}" "${inactive_version}"; then
        log_error "Final health check failed after traffic switch"
        rollback_deployment "${active_version}"
        scale_deployment "${inactive_deployment}" 0
        exit 1
    fi
    
    # Scale down old deployment
    local old_deployment="${APP_NAME}-${active_version}"
    log_info "Scaling down old deployment: ${old_deployment}"
    scale_deployment "${old_deployment}" 0
    
    log_success "Blue-green deployment completed successfully!"
    log_success "New active deployment: ${inactive_version}"
    log_success "Image: biopro/interface-exception-collector:${image_tag}"
}

# Function to show current status
show_status() {
    local active_version=$(get_active_deployment)
    
    echo "=== Blue-Green Deployment Status ==="
    echo "Active deployment: ${active_version}"
    echo ""
    
    echo "Blue deployment:"
    kubectl get deployment "${BLUE_DEPLOYMENT}" -n "${NAMESPACE}" -o wide 2>/dev/null || echo "  Not found"
    echo ""
    
    echo "Green deployment:"
    kubectl get deployment "${GREEN_DEPLOYMENT}" -n "${NAMESPACE}" -o wide 2>/dev/null || echo "  Not found"
    echo ""
    
    echo "Service configuration:"
    kubectl get service "${SERVICE_NAME}" -n "${NAMESPACE}" -o wide 2>/dev/null || echo "  Not found"
}

# Main script logic
case "${1:-help}" in
    deploy)
        if [ $# -lt 2 ]; then
            log_error "Usage: $0 deploy <image_tag> [replicas]"
            exit 1
        fi
        deploy_new_version "$2" "${3:-3}"
        ;;
    rollback)
        if [ $# -lt 2 ]; then
            log_error "Usage: $0 rollback <version>"
            log_error "Version should be 'blue' or 'green'"
            exit 1
        fi
        rollback_deployment "$2"
        ;;
    switch)
        if [ $# -lt 2 ]; then
            log_error "Usage: $0 switch <version>"
            log_error "Version should be 'blue' or 'green'"
            exit 1
        fi
        switch_traffic "$2"
        ;;
    status)
        show_status
        ;;
    help|*)
        echo "Blue-Green Deployment Script for Interface Exception Collector Service"
        echo ""
        echo "Usage: $0 <command> [options]"
        echo ""
        echo "Commands:"
        echo "  deploy <image_tag> [replicas]  Deploy new version using blue-green strategy"
        echo "  rollback <version>             Rollback to specified version (blue|green)"
        echo "  switch <version>               Switch traffic to specified version (blue|green)"
        echo "  status                         Show current deployment status"
        echo "  help                           Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0 deploy v1.2.3               Deploy version v1.2.3 with 3 replicas"
        echo "  $0 deploy v1.2.3 5             Deploy version v1.2.3 with 5 replicas"
        echo "  $0 rollback blue               Rollback to blue deployment"
        echo "  $0 switch green                Switch traffic to green deployment"
        echo "  $0 status                      Show current status"
        ;;
esac