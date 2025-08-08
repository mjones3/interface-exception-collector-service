#!/bin/bash

# Interface Exception Collector - Kafka Topics Creation Script
# This script creates all required Kafka topics for the application

set -e

# Configuration
NAMESPACE=${NAMESPACE:-"interface-exception-collector"}
KAFKA_SERVICE=${KAFKA_SERVICE:-"kafka"}
KAFKA_PORT=${KAFKA_PORT:-"9092"}
REPLICATION_FACTOR=${REPLICATION_FACTOR:-1}
PARTITIONS=${PARTITIONS:-3}
RETENTION_MS=${RETENTION_MS:-604800000}  # 7 days in milliseconds

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

# Define topics as arrays
TOPIC_NAMES=(
    "OrderRejected"
    "OrderCancelled"
    "CollectionRejected"
    "DistributionFailed"
    "ValidationError"
    "ExceptionCaptured"
    "ExceptionResolved"
    "ExceptionRetryCompleted"
    "CriticalExceptionAlert"
    "OrderRejected-DLQ"
    "OrderCancelled-DLQ"
    "CollectionRejected-DLQ"
    "DistributionFailed-DLQ"
    "ValidationError-DLQ"
)

# Function to get topic configuration
get_topic_config() {
    local topic_name="$1"
    
    if [[ "$topic_name" == *"-DLQ" ]]; then
        echo "partitions=1,replication-factor=$REPLICATION_FACTOR,retention.ms=2592000000"
    else
        echo "partitions=$PARTITIONS,replication-factor=$REPLICATION_FACTOR,retention.ms=$RETENTION_MS"
    fi
}

# Function to check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    log_success "Prerequisites check completed"
}

# Function to wait for Kafka to be ready
wait_for_kafka() {
    log_info "Waiting for Kafka to be ready..."
    
    # Wait for Kafka pods to be ready
    kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=kafka \
        --namespace="$NAMESPACE" \
        --timeout=300s
    
    # Additional wait to ensure Kafka is fully initialized
    sleep 10
    
    log_success "Kafka is ready"
}

# Function to execute Kafka command in pod
execute_kafka_command() {
    local command="$1"
    local kafka_pod
    
    # Get the first Kafka pod
    kafka_pod=$(kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=kafka -o jsonpath='{.items[0].metadata.name}')
    
    if [ -z "$kafka_pod" ]; then
        log_error "No Kafka pods found in namespace $NAMESPACE"
        return 1
    fi
    
    # Execute the command in the Kafka pod
    kubectl exec -n "$NAMESPACE" "$kafka_pod" -- bash -c "$command"
}

# Function to check if topic exists
topic_exists() {
    local topic_name="$1"
    local kafka_server="${KAFKA_SERVICE}.${NAMESPACE}.svc.cluster.local:${KAFKA_PORT}"
    
    execute_kafka_command "kafka-topics.sh --bootstrap-server $kafka_server --list" | grep -q "^${topic_name}$"
}

# Function to create a single topic
create_topic() {
    local topic_name="$1"
    local topic_config
    topic_config=$(get_topic_config "$topic_name")
    local kafka_server="${KAFKA_SERVICE}.${NAMESPACE}.svc.cluster.local:${KAFKA_PORT}"
    
    if topic_exists "$topic_name"; then
        log_info "Topic '$topic_name' already exists"
        return 0
    fi
    
    log_info "Creating topic '$topic_name' with config: $topic_config"
    
    # Parse configuration
    local partitions_count=$(echo "$topic_config" | grep -o 'partitions=[0-9]*' | cut -d'=' -f2)
    local replication_factor=$(echo "$topic_config" | grep -o 'replication-factor=[0-9]*' | cut -d'=' -f2)
    local retention_ms=$(echo "$topic_config" | grep -o 'retention.ms=[0-9]*' | cut -d'=' -f2)
    
    # Create topic command
    local create_command="kafka-topics.sh --bootstrap-server $kafka_server --create --topic $topic_name"
    create_command="$create_command --partitions $partitions_count --replication-factor $replication_factor"
    create_command="$create_command --config retention.ms=$retention_ms"
    
    # Add additional configurations for specific topics
    case "$topic_name" in
        *"-DLQ")
            create_command="$create_command --config cleanup.policy=delete"
            create_command="$create_command --config max.message.bytes=10485760"  # 10MB
            ;;
        "CriticalExceptionAlert")
            create_command="$create_command --config cleanup.policy=delete"
            create_command="$create_command --config min.insync.replicas=1"
            ;;
        *)
            create_command="$create_command --config cleanup.policy=delete"
            create_command="$create_command --config compression.type=snappy"
            ;;
    esac
    
    if execute_kafka_command "$create_command"; then
        log_success "Topic '$topic_name' created successfully"
    else
        log_error "Failed to create topic '$topic_name'"
        return 1
    fi
}

# Function to create all topics
create_all_topics() {
    log_info "Creating Kafka topics..."
    
    local failed_topics=()
    
    for topic_name in "${TOPIC_NAMES[@]}"; do
        if ! create_topic "$topic_name"; then
            failed_topics+=("$topic_name")
        fi
    done
    
    if [ ${#failed_topics[@]} -eq 0 ]; then
        log_success "All topics created successfully"
    else
        log_error "Failed to create topics: ${failed_topics[*]}"
        return 1
    fi
}

# Function to list all topics
list_topics() {
    log_info "Listing all Kafka topics..."
    
    local kafka_server="${KAFKA_SERVICE}.${NAMESPACE}.svc.cluster.local:${KAFKA_PORT}"
    
    echo "Topics in Kafka cluster:"
    echo "========================"
    execute_kafka_command "kafka-topics.sh --bootstrap-server $kafka_server --list" | sort
    echo "========================"
}

# Function to describe topics
describe_topics() {
    log_info "Describing application topics..."
    
    local kafka_server="${KAFKA_SERVICE}.${NAMESPACE}.svc.cluster.local:${KAFKA_PORT}"
    
    for topic_name in "${TOPIC_NAMES[@]}"; do
        if topic_exists "$topic_name"; then
            echo ""
            echo "Topic: $topic_name"
            echo "=================="
            execute_kafka_command "kafka-topics.sh --bootstrap-server $kafka_server --describe --topic $topic_name"
        fi
    done
}

# Function to delete all application topics
delete_topics() {
    log_warning "Deleting all application topics..."
    
    local kafka_server="${KAFKA_SERVICE}.${NAMESPACE}.svc.cluster.local:${KAFKA_PORT}"
    
    read -p "Are you sure you want to delete all application topics? This action cannot be undone. (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Topic deletion cancelled"
        return 0
    fi
    
    for topic_name in "${TOPIC_NAMES[@]}"; do
        if topic_exists "$topic_name"; then
            log_info "Deleting topic '$topic_name'"
            execute_kafka_command "kafka-topics.sh --bootstrap-server $kafka_server --delete --topic $topic_name"
        fi
    done
    
    log_success "All application topics deleted"
}

# Function to verify topic configuration
verify_topics() {
    log_info "Verifying topic configuration..."
    
    local verification_failed=false
    
    for topic_name in "${TOPIC_NAMES[@]}"; do
        if ! topic_exists "$topic_name"; then
            log_error "Topic '$topic_name' does not exist"
            verification_failed=true
        else
            log_success "Topic '$topic_name' exists"
        fi
    done
    
    if [ "$verification_failed" = true ]; then
        log_error "Topic verification failed"
        return 1
    else
        log_success "All topics verified successfully"
    fi
}

# Function to show topic information
show_topic_info() {
    log_info "Kafka Topics Information:"
    echo "========================="
    echo "Namespace: $NAMESPACE"
    echo "Kafka Service: $KAFKA_SERVICE"
    echo "Kafka Port: $KAFKA_PORT"
    echo "Default Partitions: $PARTITIONS"
    echo "Default Replication Factor: $REPLICATION_FACTOR"
    echo "Default Retention: $RETENTION_MS ms ($(($RETENTION_MS / 86400000)) days)"
    echo "========================="
    echo ""
    echo "Application Topics:"
    for topic_name in "${TOPIC_NAMES[@]}"; do
        echo "  - $topic_name"
    done
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS] [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  create                       Create all topics (default)"
    echo "  list                         List all topics"
    echo "  describe                     Describe application topics"
    echo "  verify                       Verify topics exist"
    echo "  delete                       Delete all application topics"
    echo "  info                         Show topic configuration information"
    echo ""
    echo "Options:"
    echo "  -n, --namespace NAMESPACE    Kubernetes namespace (default: interface-exception-collector)"
    echo "  -s, --service SERVICE        Kafka service name (default: kafka)"
    echo "  -p, --port PORT              Kafka port (default: 9092)"
    echo "  --partitions COUNT           Default partitions per topic (default: 3)"
    echo "  --replication-factor COUNT   Default replication factor (default: 1)"
    echo "  --retention-ms MS            Default retention in milliseconds (default: 604800000)"
    echo "  -h, --help                   Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  NAMESPACE                    Kubernetes namespace"
    echo "  KAFKA_SERVICE                Kafka service name"
    echo "  KAFKA_PORT                   Kafka port"
    echo "  PARTITIONS                   Default partitions per topic"
    echo "  REPLICATION_FACTOR           Default replication factor"
    echo "  RETENTION_MS                 Default retention in milliseconds"
}

# Parse command line arguments
COMMAND="create"

while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -s|--service)
            KAFKA_SERVICE="$2"
            shift 2
            ;;
        -p|--port)
            KAFKA_PORT="$2"
            shift 2
            ;;
        --partitions)
            PARTITIONS="$2"
            shift 2
            ;;
        --replication-factor)
            REPLICATION_FACTOR="$2"
            shift 2
            ;;
        --retention-ms)
            RETENTION_MS="$2"
            shift 2
            ;;
        create|list|describe|verify|delete|info)
            COMMAND="$1"
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
    case "$COMMAND" in
        create)
            check_prerequisites
            wait_for_kafka
            create_all_topics
            verify_topics
            ;;
        list)
            check_prerequisites
            wait_for_kafka
            list_topics
            ;;
        describe)
            check_prerequisites
            wait_for_kafka
            describe_topics
            ;;
        verify)
            check_prerequisites
            wait_for_kafka
            verify_topics
            ;;
        delete)
            check_prerequisites
            wait_for_kafka
            delete_topics
            ;;
        info)
            show_topic_info
            ;;
        *)
            log_error "Unknown command: $COMMAND"
            usage
            exit 1
            ;;
    esac
}

# Run main function
main "$@"