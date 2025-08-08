#!/bin/bash

# Interface Exception Collector - Environment Cleanup Script
# This script cleans up deployed resources and environments

set -e

# Configuration
NAMESPACE=${NAMESPACE:-"interface-exception-collector"}
HELM_RELEASE_NAME=${HELM_RELEASE_NAME:-"interface-exception-collector"}
LOCAL_REGISTRY_NAME=${LOCAL_REGISTRY_NAME:-"local-registry"}
FORCE_CLEANUP=${FORCE_CLEANUP:-false}

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

# Function to check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v helm &> /dev/null; then
        log_warning "helm is not installed. Helm-based cleanup will be skipped."
    fi
    
    if ! command -v docker &> /dev/null; then
        log_warning "docker is not installed. Docker cleanup will be skipped."
    fi
    
    log_success "Prerequisites check completed"
}

# Function to confirm cleanup action
confirm_cleanup() {
    local cleanup_type="$1"
    
    if [ "$FORCE_CLEANUP" = true ]; then
        log_info "Force cleanup enabled, skipping confirmation"
        return 0
    fi
    
    log_warning "This will perform $cleanup_type cleanup and remove resources permanently."
    read -p "Are you sure you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Cleanup cancelled by user"
        exit 0
    fi
}

# Function to cleanup Helm releases
cleanup_helm_releases() {
    log_info "Cleaning up Helm releases..."
    
    if ! command -v helm &> /dev/null; then
        log_warning "Helm not available, skipping Helm cleanup"
        return 0
    fi
    
    # List of releases to clean up
    local releases=("$HELM_RELEASE_NAME" "postgresql" "redis" "kafka" "zookeeper")
    
    for release in "${releases[@]}"; do
        if helm list -n "$NAMESPACE" | grep -q "$release"; then
            log_info "Uninstalling Helm release: $release"
            helm uninstall "$release" --namespace "$NAMESPACE" || {
                log_warning "Failed to uninstall Helm release: $release"
            }
        else
            log_info "Helm release '$release' not found in namespace '$NAMESPACE'"
        fi
    done
    
    log_success "Helm releases cleanup completed"
}

# Function to cleanup Kubernetes resources
cleanup_kubernetes_resources() {
    log_info "Cleaning up Kubernetes resources..."
    
    # Check if namespace exists
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        log_info "Namespace '$NAMESPACE' does not exist"
        return 0
    fi
    
    # Delete specific resource types first
    local resource_types=(
        "jobs"
        "cronjobs"
        "deployments"
        "statefulsets"
        "daemonsets"
        "replicasets"
        "pods"
        "services"
        "ingresses"
        "configmaps"
        "secrets"
        "persistentvolumeclaims"
        "networkpolicies"
        "serviceaccounts"
        "roles"
        "rolebindings"
    )
    
    for resource_type in "${resource_types[@]}"; do
        log_info "Deleting $resource_type in namespace '$NAMESPACE'..."
        kubectl delete "$resource_type" --all -n "$NAMESPACE" --ignore-not-found=true --timeout=60s || {
            log_warning "Failed to delete some $resource_type resources"
        }
    done
    
    # Wait for pods to terminate
    log_info "Waiting for pods to terminate..."
    kubectl wait --for=delete pods --all -n "$NAMESPACE" --timeout=300s || {
        log_warning "Some pods may still be terminating"
    }
    
    log_success "Kubernetes resources cleanup completed"
}

# Function to cleanup namespace
cleanup_namespace() {
    log_info "Cleaning up namespace '$NAMESPACE'..."
    
    if kubectl get namespace "$NAMESPACE" &> /dev/null; then
        kubectl delete namespace "$NAMESPACE" --timeout=300s || {
            log_warning "Failed to delete namespace '$NAMESPACE', it may still be terminating"
        }
        
        # Wait for namespace to be fully deleted
        log_info "Waiting for namespace to be fully deleted..."
        local timeout=300
        local elapsed=0
        while kubectl get namespace "$NAMESPACE" &> /dev/null && [ $elapsed -lt $timeout ]; do
            sleep 5
            elapsed=$((elapsed + 5))
            echo -n "."
        done
        echo
        
        if kubectl get namespace "$NAMESPACE" &> /dev/null; then
            log_warning "Namespace '$NAMESPACE' is still terminating. This may take additional time."
        else
            log_success "Namespace '$NAMESPACE' deleted successfully"
        fi
    else
        log_info "Namespace '$NAMESPACE' does not exist"
    fi
}

# Function to cleanup Docker resources
cleanup_docker_resources() {
    log_info "Cleaning up Docker resources..."
    
    if ! command -v docker &> /dev/null; then
        log_warning "Docker not available, skipping Docker cleanup"
        return 0
    fi
    
    # Stop and remove local registry
    if docker ps -a --format "table {{.Names}}" | grep -q "^$LOCAL_REGISTRY_NAME$"; then
        log_info "Stopping and removing local registry container..."
        docker stop "$LOCAL_REGISTRY_NAME" || true
        docker rm "$LOCAL_REGISTRY_NAME" || true
    fi
    
    # Remove application images
    log_info "Removing application Docker images..."
    docker images --format "table {{.Repository}}:{{.Tag}}" | grep "interface-exception-collector" | while read -r image; do
        if [ -n "$image" ] && [ "$image" != "REPOSITORY:TAG" ]; then
            log_info "Removing image: $image"
            docker rmi "$image" || {
                log_warning "Failed to remove image: $image"
            }
        fi
    done
    
    # Clean up unused Docker resources
    log_info "Cleaning up unused Docker resources..."
    docker system prune -f || {
        log_warning "Failed to prune Docker system"
    }
    
    log_success "Docker resources cleanup completed"
}

# Function to cleanup Tilt resources
cleanup_tilt_resources() {
    log_info "Cleaning up Tilt resources..."
    
    if ! command -v tilt &> /dev/null; then
        log_warning "Tilt not available, skipping Tilt cleanup"
        return 0
    fi
    
    # Stop Tilt if running
    if pgrep -f "tilt up" > /dev/null; then
        log_info "Stopping Tilt processes..."
        pkill -f "tilt up" || true
        sleep 2
    fi
    
    # Run tilt down if Tiltfile exists
    if [ -f "Tiltfile" ]; then
        log_info "Running 'tilt down' to clean up Tilt resources..."
        tilt down || {
            log_warning "Failed to run 'tilt down'"
        }
    fi
    
    log_success "Tilt resources cleanup completed"
}

# Function to cleanup persistent volumes
cleanup_persistent_volumes() {
    log_info "Cleaning up persistent volumes..."
    
    # Find PVs that were bound to PVCs in our namespace
    local pvs
    pvs=$(kubectl get pv -o jsonpath='{.items[?(@.spec.claimRef.namespace=="'$NAMESPACE'")].metadata.name}' 2>/dev/null || echo "")
    
    if [ -n "$pvs" ]; then
        for pv in $pvs; do
            log_info "Deleting persistent volume: $pv"
            kubectl delete pv "$pv" --ignore-not-found=true || {
                log_warning "Failed to delete persistent volume: $pv"
            }
        done
    else
        log_info "No persistent volumes found for namespace '$NAMESPACE'"
    fi
    
    log_success "Persistent volumes cleanup completed"
}

# Function to cleanup temporary files
cleanup_temp_files() {
    log_info "Cleaning up temporary files..."
    
    # Remove temporary configuration files
    rm -f /tmp/flyway.conf
    rm -f /tmp/postgresql-*.yaml
    rm -f /tmp/redis-*.yaml
    rm -f /tmp/kafka-*.yaml
    rm -f /tmp/zookeeper-*.yaml
    
    # Remove any .tmp files in the project directory
    find . -name "*.tmp" -type f -delete 2>/dev/null || true
    
    log_success "Temporary files cleanup completed"
}

# Function to show cleanup status
show_cleanup_status() {
    log_info "Cleanup Status Report:"
    echo "======================"
    echo "Namespace: $NAMESPACE"
    echo "Helm Release: $HELM_RELEASE_NAME"
    echo "Local Registry: $LOCAL_REGISTRY_NAME"
    echo "======================"
    
    # Check namespace status
    if kubectl get namespace "$NAMESPACE" &> /dev/null; then
        log_warning "Namespace '$NAMESPACE' still exists"
        kubectl get all -n "$NAMESPACE" 2>/dev/null || true
    else
        log_success "Namespace '$NAMESPACE' has been removed"
    fi
    
    # Check Docker registry status
    if command -v docker &> /dev/null; then
        if docker ps --format "table {{.Names}}" | grep -q "^$LOCAL_REGISTRY_NAME$"; then
            log_warning "Local registry container '$LOCAL_REGISTRY_NAME' is still running"
        else
            log_success "Local registry container has been removed"
        fi
    fi
    
    # Check for remaining application images
    if command -v docker &> /dev/null; then
        local app_images
        app_images=$(docker images --format "table {{.Repository}}:{{.Tag}}" | grep "interface-exception-collector" | wc -l)
        if [ "$app_images" -gt 0 ]; then
            log_warning "$app_images application Docker images still exist"
        else
            log_success "All application Docker images have been removed"
        fi
    fi
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS] [SCOPE]"
    echo ""
    echo "Scopes:"
    echo "  all                          Clean up everything (default)"
    echo "  kubernetes                   Clean up Kubernetes resources only"
    echo "  helm                         Clean up Helm releases only"
    echo "  docker                       Clean up Docker resources only"
    echo "  tilt                         Clean up Tilt resources only"
    echo "  namespace                    Delete namespace only"
    echo "  temp                         Clean up temporary files only"
    echo "  status                       Show cleanup status"
    echo ""
    echo "Options:"
    echo "  -n, --namespace NAMESPACE    Kubernetes namespace (default: interface-exception-collector)"
    echo "  -r, --release RELEASE        Helm release name (default: interface-exception-collector)"
    echo "  --registry-name NAME         Local registry container name (default: local-registry)"
    echo "  --force                      Skip confirmation prompts"
    echo "  --dry-run                    Show what would be cleaned up without actually doing it"
    echo "  -h, --help                   Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  NAMESPACE                    Kubernetes namespace"
    echo "  HELM_RELEASE_NAME            Helm release name"
    echo "  LOCAL_REGISTRY_NAME          Local registry container name"
    echo "  FORCE_CLEANUP                Skip confirmation prompts (true/false)"
}

# Parse command line arguments
SCOPE="all"
DRY_RUN=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -r|--release)
            HELM_RELEASE_NAME="$2"
            shift 2
            ;;
        --registry-name)
            LOCAL_REGISTRY_NAME="$2"
            shift 2
            ;;
        --force)
            FORCE_CLEANUP=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        all|kubernetes|helm|docker|tilt|namespace|temp|status)
            SCOPE="$1"
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Main function
main() {
    if [ "$DRY_RUN" = true ]; then
        log_info "DRY RUN MODE - No actual cleanup will be performed"
        log_info "Would clean up scope: $SCOPE"
        log_info "Target namespace: $NAMESPACE"
        log_info "Helm release: $HELM_RELEASE_NAME"
        log_info "Local registry: $LOCAL_REGISTRY_NAME"
        exit 0
    fi
    
    if [ "$SCOPE" = "status" ]; then
        show_cleanup_status
        exit 0
    fi
    
    check_prerequisites
    confirm_cleanup "$SCOPE"
    
    case "$SCOPE" in
        all)
            cleanup_tilt_resources
            cleanup_helm_releases
            cleanup_kubernetes_resources
            cleanup_persistent_volumes
            cleanup_namespace
            cleanup_docker_resources
            cleanup_temp_files
            ;;
        kubernetes)
            cleanup_kubernetes_resources
            cleanup_persistent_volumes
            ;;
        helm)
            cleanup_helm_releases
            ;;
        docker)
            cleanup_docker_resources
            ;;
        tilt)
            cleanup_tilt_resources
            ;;
        namespace)
            cleanup_namespace
            ;;
        temp)
            cleanup_temp_files
            ;;
        *)
            log_error "Unknown scope: $SCOPE"
            usage
            exit 1
            ;;
    esac
    
    show_cleanup_status
    log_success "Cleanup completed for scope: $SCOPE"
}

# Run main function
main "$@"