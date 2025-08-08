#!/bin/bash

# Interface Exception Collector - Infrastructure Setup Script
# This script sets up the required infrastructure components

set -e

# Configuration
NAMESPACE=${NAMESPACE:-"interface-exception-collector"}
POSTGRES_VERSION=${POSTGRES_VERSION:-"15"}
KAFKA_VERSION=${KAFKA_VERSION:-"3.5.0"}
REDIS_VERSION=${REDIS_VERSION:-"7.0"}
ZOOKEEPER_VERSION=${ZOOKEEPER_VERSION:-"3.8.0"}
LOCAL_MODE=${LOCAL_MODE:-false}

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
        log_error "helm is not installed or not in PATH"
        exit 1
    fi
    
    log_success "Prerequisites check completed"
}

# Function to add Helm repositories
add_helm_repositories() {
    log_info "Adding required Helm repositories..."
    
    # Add Bitnami repository for PostgreSQL and Redis
    helm repo add bitnami https://charts.bitnami.com/bitnami
    
    # Add Confluent repository for Kafka
    helm repo add confluentinc https://confluentinc.github.io/cp-helm-charts/
    
    # Update repositories
    helm repo update
    
    log_success "Helm repositories added and updated"
}

# Function to create namespace
create_namespace() {
    log_info "Creating namespace '$NAMESPACE'..."
    
    kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
    
    log_success "Namespace '$NAMESPACE' is ready"
}

# Function to deploy PostgreSQL
deploy_postgresql() {
    log_info "Deploying PostgreSQL..."
    
    # Create PostgreSQL values file
    cat > /tmp/postgresql-values.yaml << EOF
auth:
  postgresPassword: "postgres"
  username: "exception_collector"
  password: "exception_collector_pass"
  database: "interface_exceptions"

primary:
  persistence:
    enabled: true
    size: 8Gi
  resources:
    limits:
      memory: 512Mi
      cpu: 500m
    requests:
      memory: 256Mi
      cpu: 250m

metrics:
  enabled: true
  serviceMonitor:
    enabled: false

image:
  tag: "$POSTGRES_VERSION"
EOF

    # Deploy PostgreSQL
    helm upgrade --install postgresql bitnami/postgresql \
        --namespace "$NAMESPACE" \
        --values /tmp/postgresql-values.yaml \
        --wait \
        --timeout=10m
    
    # Clean up temporary file
    rm -f /tmp/postgresql-values.yaml
    
    log_success "PostgreSQL deployed successfully"
}

# Function to deploy Redis
deploy_redis() {
    log_info "Deploying Redis..."
    
    # Create Redis values file
    cat > /tmp/redis-values.yaml << EOF
auth:
  enabled: true
  password: "redis_pass"

master:
  persistence:
    enabled: true
    size: 2Gi
  resources:
    limits:
      memory: 256Mi
      cpu: 250m
    requests:
      memory: 128Mi
      cpu: 100m

replica:
  replicaCount: 1
  persistence:
    enabled: true
    size: 2Gi
  resources:
    limits:
      memory: 256Mi
      cpu: 250m
    requests:
      memory: 128Mi
      cpu: 100m

metrics:
  enabled: true
  serviceMonitor:
    enabled: false

image:
  tag: "$REDIS_VERSION"
EOF

    # Deploy Redis
    helm upgrade --install redis bitnami/redis \
        --namespace "$NAMESPACE" \
        --values /tmp/redis-values.yaml \
        --wait \
        --timeout=10m
    
    # Clean up temporary file
    rm -f /tmp/redis-values.yaml
    
    log_success "Redis deployed successfully"
}

# Function to deploy Zookeeper
deploy_zookeeper() {
    log_info "Deploying Zookeeper..."
    
    # Create Zookeeper values file
    cat > /tmp/zookeeper-values.yaml << EOF
replicaCount: 1

persistence:
  enabled: true
  size: 2Gi

resources:
  limits:
    memory: 512Mi
    cpu: 500m
  requests:
    memory: 256Mi
    cpu: 250m

image:
  tag: "$ZOOKEEPER_VERSION"

metrics:
  enabled: true
  serviceMonitor:
    enabled: false
EOF

    # Deploy Zookeeper
    helm upgrade --install zookeeper bitnami/zookeeper \
        --namespace "$NAMESPACE" \
        --values /tmp/zookeeper-values.yaml \
        --wait \
        --timeout=10m
    
    # Clean up temporary file
    rm -f /tmp/zookeeper-values.yaml
    
    log_success "Zookeeper deployed successfully"
}

# Function to deploy Kafka
deploy_kafka() {
    log_info "Deploying Kafka..."
    
    # Wait for Zookeeper to be ready
    kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=zookeeper \
        --namespace="$NAMESPACE" \
        --timeout=300s
    
    # Create Kafka values file
    cat > /tmp/kafka-values.yaml << EOF
replicaCount: 1

persistence:
  enabled: true
  size: 4Gi

zookeeper:
  enabled: false

externalZookeeper:
  servers: "zookeeper:2181"

resources:
  limits:
    memory: 1Gi
    cpu: 1000m
  requests:
    memory: 512Mi
    cpu: 500m

listeners:
  client:
    protocol: PLAINTEXT
  controller:
    protocol: PLAINTEXT
  interbroker:
    protocol: PLAINTEXT
  external:
    protocol: PLAINTEXT

service:
  type: ClusterIP
  ports:
    client: 9092

metrics:
  kafka:
    enabled: true
  jmx:
    enabled: true

image:
  tag: "$KAFKA_VERSION"

# Auto-create topics
autoCreateTopicsEnable: true
defaultReplicationFactor: 1
offsetsTopicReplicationFactor: 1
transactionStateLogReplicationFactor: 1
transactionStateLogMinIsr: 1

# Log retention
logRetentionHours: 168
logRetentionBytes: 1073741824
EOF

    # Deploy Kafka
    helm upgrade --install kafka bitnami/kafka \
        --namespace "$NAMESPACE" \
        --values /tmp/kafka-values.yaml \
        --wait \
        --timeout=15m
    
    # Clean up temporary file
    rm -f /tmp/kafka-values.yaml
    
    log_success "Kafka deployed successfully"
}

# Function to create Kafka topics
create_kafka_topics() {
    log_info "Creating Kafka topics..."
    
    # Wait for Kafka to be ready
    kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=kafka \
        --namespace="$NAMESPACE" \
        --timeout=300s
    
    # Run the topic creation script
    if [ -f "scripts/create-kafka-topics.sh" ]; then
        bash scripts/create-kafka-topics.sh --namespace="$NAMESPACE"
    else
        log_warning "Kafka topics creation script not found. Topics will be auto-created on first use."
    fi
    
    log_success "Kafka topics setup completed"
}

# Function to setup local development infrastructure
setup_local_infrastructure() {
    log_info "Setting up local development infrastructure..."
    
    # For local development, we use simpler configurations
    NAMESPACE="interface-exception-collector-dev"
    
    create_namespace
    
    # Deploy lightweight versions for local development
    deploy_postgresql_local
    deploy_redis_local
    deploy_kafka_local
    
    log_success "Local development infrastructure setup completed"
}

# Function to deploy PostgreSQL for local development
deploy_postgresql_local() {
    log_info "Deploying PostgreSQL for local development..."
    
    cat > /tmp/postgresql-local-values.yaml << EOF
auth:
  postgresPassword: "postgres"
  username: "exception_collector"
  password: "exception_collector_pass"
  database: "interface_exceptions"

primary:
  persistence:
    enabled: false
  resources:
    limits:
      memory: 256Mi
      cpu: 250m
    requests:
      memory: 128Mi
      cpu: 100m

metrics:
  enabled: false

image:
  tag: "$POSTGRES_VERSION"
EOF

    helm upgrade --install postgresql bitnami/postgresql \
        --namespace "$NAMESPACE" \
        --values /tmp/postgresql-local-values.yaml \
        --wait \
        --timeout=5m
    
    rm -f /tmp/postgresql-local-values.yaml
    log_success "PostgreSQL (local) deployed successfully"
}

# Function to deploy Redis for local development
deploy_redis_local() {
    log_info "Deploying Redis for local development..."
    
    cat > /tmp/redis-local-values.yaml << EOF
auth:
  enabled: false

master:
  persistence:
    enabled: false
  resources:
    limits:
      memory: 128Mi
      cpu: 100m
    requests:
      memory: 64Mi
      cpu: 50m

replica:
  replicaCount: 0

metrics:
  enabled: false

image:
  tag: "$REDIS_VERSION"
EOF

    helm upgrade --install redis bitnami/redis \
        --namespace "$NAMESPACE" \
        --values /tmp/redis-local-values.yaml \
        --wait \
        --timeout=5m
    
    rm -f /tmp/redis-local-values.yaml
    log_success "Redis (local) deployed successfully"
}

# Function to deploy Kafka for local development
deploy_kafka_local() {
    log_info "Deploying Kafka for local development..."
    
    # Deploy Zookeeper first
    cat > /tmp/zookeeper-local-values.yaml << EOF
replicaCount: 1
persistence:
  enabled: false
resources:
  limits:
    memory: 256Mi
    cpu: 250m
  requests:
    memory: 128Mi
    cpu: 100m
metrics:
  enabled: false
EOF

    helm upgrade --install zookeeper bitnami/zookeeper \
        --namespace "$NAMESPACE" \
        --values /tmp/zookeeper-local-values.yaml \
        --wait \
        --timeout=5m
    
    # Wait for Zookeeper
    kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=zookeeper \
        --namespace="$NAMESPACE" \
        --timeout=300s
    
    # Deploy Kafka
    cat > /tmp/kafka-local-values.yaml << EOF
replicaCount: 1
persistence:
  enabled: false
zookeeper:
  enabled: false
externalZookeeper:
  servers: "zookeeper:2181"
resources:
  limits:
    memory: 512Mi
    cpu: 500m
  requests:
    memory: 256Mi
    cpu: 250m
metrics:
  kafka:
    enabled: false
  jmx:
    enabled: false
autoCreateTopicsEnable: true
defaultReplicationFactor: 1
EOF

    helm upgrade --install kafka bitnami/kafka \
        --namespace "$NAMESPACE" \
        --values /tmp/kafka-local-values.yaml \
        --wait \
        --timeout=10m
    
    rm -f /tmp/zookeeper-local-values.yaml /tmp/kafka-local-values.yaml
    log_success "Kafka (local) deployed successfully"
}

# Function to verify infrastructure deployment
verify_infrastructure() {
    log_info "Verifying infrastructure deployment..."
    
    # Check PostgreSQL
    kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=postgresql \
        --namespace="$NAMESPACE" \
        --timeout=300s
    
    # Check Redis
    kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=redis \
        --namespace="$NAMESPACE" \
        --timeout=300s
    
    # Check Kafka
    kubectl wait --for=condition=ready pod \
        --selector=app.kubernetes.io/name=kafka \
        --namespace="$NAMESPACE" \
        --timeout=300s
    
    # Show deployment status
    kubectl get pods -n "$NAMESPACE"
    kubectl get services -n "$NAMESPACE"
    
    log_success "Infrastructure verification completed"
}

# Function to show infrastructure information
show_infrastructure_info() {
    log_info "Infrastructure Information:"
    echo "=========================="
    echo "Namespace: $NAMESPACE"
    echo "PostgreSQL Version: $POSTGRES_VERSION"
    echo "Redis Version: $REDIS_VERSION"
    echo "Kafka Version: $KAFKA_VERSION"
    echo "Zookeeper Version: $ZOOKEEPER_VERSION"
    echo "=========================="
    
    log_info "Connection Information:"
    echo "PostgreSQL: postgresql.$NAMESPACE.svc.cluster.local:5432"
    echo "Redis: redis-master.$NAMESPACE.svc.cluster.local:6379"
    echo "Kafka: kafka.$NAMESPACE.svc.cluster.local:9092"
    echo "Zookeeper: zookeeper.$NAMESPACE.svc.cluster.local:2181"
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -n, --namespace NAMESPACE    Set Kubernetes namespace (default: interface-exception-collector)"
    echo "  --postgres-version VERSION  PostgreSQL version (default: 15)"
    echo "  --kafka-version VERSION     Kafka version (default: 3.5.0)"
    echo "  --redis-version VERSION     Redis version (default: 7.0)"
    echo "  --local                      Setup for local development"
    echo "  --verify-only                Only verify existing infrastructure"
    echo "  --info                       Show infrastructure information"
    echo "  -h, --help                   Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  NAMESPACE                    Kubernetes namespace"
    echo "  POSTGRES_VERSION             PostgreSQL version"
    echo "  KAFKA_VERSION                Kafka version"
    echo "  REDIS_VERSION                Redis version"
    echo "  ZOOKEEPER_VERSION            Zookeeper version"
}

# Parse command line arguments
VERIFY_ONLY=false
SHOW_INFO=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        --postgres-version)
            POSTGRES_VERSION="$2"
            shift 2
            ;;
        --kafka-version)
            KAFKA_VERSION="$2"
            shift 2
            ;;
        --redis-version)
            REDIS_VERSION="$2"
            shift 2
            ;;
        --local)
            LOCAL_MODE=true
            shift
            ;;
        --verify-only)
            VERIFY_ONLY=true
            shift
            ;;
        --info)
            SHOW_INFO=true
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
    if [ "$SHOW_INFO" = true ]; then
        show_infrastructure_info
        exit 0
    fi
    
    if [ "$VERIFY_ONLY" = true ]; then
        verify_infrastructure
        exit 0
    fi
    
    log_info "Starting infrastructure setup..."
    
    check_prerequisites
    add_helm_repositories
    
    if [ "$LOCAL_MODE" = true ]; then
        setup_local_infrastructure
    else
        create_namespace
        deploy_postgresql
        deploy_redis
        deploy_zookeeper
        deploy_kafka
        create_kafka_topics
    fi
    
    verify_infrastructure
    show_infrastructure_info
    
    log_success "Infrastructure setup completed successfully!"
}

# Run main function
main "$@"