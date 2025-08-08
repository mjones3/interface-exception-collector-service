#!/bin/bash

# Interface Exception Collector - Database Migration Script
# This script runs Flyway database migrations

set -e

# Configuration
NAMESPACE=${NAMESPACE:-"interface-exception-collector"}
POSTGRES_SERVICE=${POSTGRES_SERVICE:-"postgresql"}
POSTGRES_PORT=${POSTGRES_PORT:-"5432"}
POSTGRES_DB=${POSTGRES_DB:-"exception_collector_db"}
POSTGRES_USER=${POSTGRES_USER:-"exception_user"}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-"exception_pass"}
MIGRATION_PATH=${MIGRATION_PATH:-"src/main/resources/db/migration"}
FLYWAY_VERSION=${FLYWAY_VERSION:-"9.22.0"}

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
    
    if [ ! -d "$MIGRATION_PATH" ]; then
        log_error "Migration path not found: $MIGRATION_PATH"
        log_info "Please ensure you're running this script from the project root directory"
        exit 1
    fi
    
    log_success "Prerequisites check completed"
}

# Function to wait for PostgreSQL to be ready
wait_for_postgresql() {
    log_info "Waiting for PostgreSQL to be ready..."
    
    if [ "$RUN_LOCAL" = true ]; then
        # Wait for Docker Compose PostgreSQL to be ready
        local retries=0
        local max_retries=30
        
        while [ $retries -lt $max_retries ]; do
            if docker-compose exec -T postgres pg_isready -U exception_user -d exception_collector_db > /dev/null 2>&1; then
                log_success "PostgreSQL is ready"
                return 0
            fi
            
            log_info "PostgreSQL not ready yet, waiting... (attempt $((retries + 1))/$max_retries)"
            sleep 5
            retries=$((retries + 1))
        done
        
        log_error "PostgreSQL failed to become ready within timeout"
        return 1
    else
        # Wait for PostgreSQL pods to be ready
        kubectl wait --for=condition=ready pod \
            --selector=app.kubernetes.io/name=postgresql \
            --namespace="$NAMESPACE" \
            --timeout=300s
        
        # Additional wait to ensure PostgreSQL is fully initialized
        sleep 5
        
        log_success "PostgreSQL is ready"
    fi
}

# Function to get PostgreSQL connection details
get_postgres_connection() {
    local postgres_url
    
    if [ "$RUN_LOCAL" = true ]; then
        postgres_url="jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}"
    else
        local postgres_host="${POSTGRES_SERVICE}.${NAMESPACE}.svc.cluster.local"
        postgres_url="jdbc:postgresql://${postgres_host}:${POSTGRES_PORT}/${POSTGRES_DB}"
    fi
    
    echo "$postgres_url"
}

# Function to test database connection
test_database_connection() {
    log_info "Testing database connection..."
    
    if [ "$RUN_LOCAL" = true ]; then
        # Test Docker Compose PostgreSQL connection
        if docker-compose exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT 1;" > /dev/null 2>&1; then
            log_success "Database connection successful"
        else
            log_error "Database connection failed"
            return 1
        fi
    else
        # Test Kubernetes PostgreSQL connection
        local postgres_pod
        postgres_pod=$(kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=postgresql -o jsonpath='{.items[0].metadata.name}')
        
        if [ -z "$postgres_pod" ]; then
            log_error "No PostgreSQL pods found in namespace $NAMESPACE"
            return 1
        fi
        
        # Test connection using psql
        if kubectl exec -n "$NAMESPACE" "$postgres_pod" -- psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT 1;" > /dev/null 2>&1; then
            log_success "Database connection successful"
        else
            log_error "Database connection failed"
            return 1
        fi
    fi
}

# Function to create Flyway configuration
create_flyway_config() {
    local config_file="/tmp/flyway.conf"
    local postgres_url
    postgres_url=$(get_postgres_connection)
    
    cat > "$config_file" << EOF
flyway.url=$postgres_url
flyway.user=$POSTGRES_USER
flyway.password=$POSTGRES_PASSWORD
flyway.locations=filesystem:$MIGRATION_PATH
flyway.schemas=public
flyway.table=flyway_schema_history
flyway.validateOnMigrate=true
flyway.outOfOrder=false
flyway.ignoreMissingMigrations=false
flyway.ignoreIgnoredMigrations=false
flyway.ignorePendingMigrations=false
flyway.ignoreFutureMigrations=true
flyway.cleanDisabled=true
flyway.baselineOnMigrate=false
flyway.skipDefaultResolvers=false
flyway.skipDefaultCallbacks=false
flyway.connectRetries=10
flyway.connectRetriesInterval=60
EOF

    echo "$config_file"
}

# Function to run migrations using Flyway in Kubernetes
run_migrations_k8s() {
    log_info "Running database migrations in Kubernetes..."
    
    local flyway_config
    flyway_config=$(create_flyway_config)
    
    # Create ConfigMap with Flyway configuration
    kubectl create configmap flyway-config \
        --from-file="$flyway_config" \
        --namespace="$NAMESPACE" \
        --dry-run=client -o yaml | kubectl apply -f -
    
    # Create ConfigMap with migration files
    kubectl create configmap flyway-migrations \
        --from-file="$MIGRATION_PATH" \
        --namespace="$NAMESPACE" \
        --dry-run=client -o yaml | kubectl apply -f -
    
    # Create and run migration job
    cat << EOF | kubectl apply -f -
apiVersion: batch/v1
kind: Job
metadata:
  name: flyway-migration-$(date +%s)
  namespace: $NAMESPACE
  labels:
    app: flyway-migration
spec:
  ttlSecondsAfterFinished: 300
  template:
    metadata:
      labels:
        app: flyway-migration
    spec:
      restartPolicy: Never
      containers:
      - name: flyway
        image: flyway/flyway:$FLYWAY_VERSION
        command: ["/bin/bash"]
        args:
        - -c
        - |
          echo "Starting Flyway migration..."
          flyway -configFiles=/flyway/conf/flyway.conf migrate
          echo "Migration completed successfully"
        volumeMounts:
        - name: flyway-config
          mountPath: /flyway/conf
        - name: flyway-migrations
          mountPath: /flyway/sql
        env:
        - name: FLYWAY_URL
          value: "jdbc:postgresql://$POSTGRES_SERVICE.$NAMESPACE.svc.cluster.local:$POSTGRES_PORT/$POSTGRES_DB"
        - name: FLYWAY_USER
          value: "$POSTGRES_USER"
        - name: FLYWAY_PASSWORD
          value: "$POSTGRES_PASSWORD"
        - name: FLYWAY_LOCATIONS
          value: "filesystem:/flyway/sql"
      volumes:
      - name: flyway-config
        configMap:
          name: flyway-config
      - name: flyway-migrations
        configMap:
          name: flyway-migrations
EOF

    # Wait for job to complete
    local job_name
    job_name=$(kubectl get jobs -n "$NAMESPACE" -l app=flyway-migration --sort-by=.metadata.creationTimestamp -o jsonpath='{.items[-1].metadata.name}')
    
    log_info "Waiting for migration job '$job_name' to complete..."
    kubectl wait --for=condition=complete job/"$job_name" --namespace="$NAMESPACE" --timeout=600s
    
    # Show job logs
    kubectl logs job/"$job_name" --namespace="$NAMESPACE"
    
    # Cleanup ConfigMaps
    kubectl delete configmap flyway-config flyway-migrations --namespace="$NAMESPACE" --ignore-not-found=true
    
    # Clean up temporary config file
    rm -f "$flyway_config"
    
    log_success "Database migrations completed successfully"
}

# Function to run migrations locally (for development)
run_migrations_local() {
    log_info "Running database migrations locally..."
    
    # Check if Flyway is available locally
    if command -v flyway &> /dev/null; then
        log_info "Using local Flyway installation"
        run_migrations_flyway_local
    elif command -v mvn &> /dev/null; then
        log_info "Using Maven Flyway plugin"
        run_migrations_maven
    else
        log_error "Neither Flyway nor Maven is available locally"
        log_info "Please install Flyway or Maven, or use --kubernetes option"
        exit 1
    fi
}

# Function to run migrations using local Flyway
run_migrations_flyway_local() {
    local postgres_url
    postgres_url=$(get_postgres_connection)
    
    if [ "$RUN_LOCAL" = true ]; then
        # For local mode, PostgreSQL is already accessible on localhost
        log_info "Using direct connection to local PostgreSQL..."
        
        # Run Flyway migration
        flyway \
            -url="$postgres_url" \
            -user="$POSTGRES_USER" \
            -password="$POSTGRES_PASSWORD" \
            -locations="filesystem:$MIGRATION_PATH" \
            migrate
    else
        # Port forward to access PostgreSQL locally
        log_info "Setting up port forwarding to PostgreSQL..."
        kubectl port-forward -n "$NAMESPACE" service/"$POSTGRES_SERVICE" 5432:5432 &
        local port_forward_pid=$!
        
        # Wait for port forward to be ready
        sleep 3
        
        # Run Flyway migration
        flyway \
            -url="jdbc:postgresql://localhost:5432/$POSTGRES_DB" \
            -user="$POSTGRES_USER" \
            -password="$POSTGRES_PASSWORD" \
            -locations="filesystem:$MIGRATION_PATH" \
            migrate
        
        # Clean up port forward
        kill $port_forward_pid 2>/dev/null || true
    fi
    
    log_success "Local Flyway migration completed"
}

# Function to run migrations using Maven
run_migrations_maven() {
    log_info "Running migrations using Maven Flyway plugin..."
    
    if [ "$RUN_LOCAL" = true ]; then
        # For local mode, PostgreSQL is already accessible on localhost
        log_info "Using direct connection to local PostgreSQL..."
        
        # Run Maven Flyway migration
        ./mvnw flyway:migrate \
            -Dflyway.url="jdbc:postgresql://localhost:5432/$POSTGRES_DB" \
            -Dflyway.user="$POSTGRES_USER" \
            -Dflyway.password="$POSTGRES_PASSWORD" \
            -Dflyway.baselineOnMigrate=true
    else
        # Port forward to access PostgreSQL locally
        log_info "Setting up port forwarding to PostgreSQL..."
        kubectl port-forward -n "$NAMESPACE" service/"$POSTGRES_SERVICE" 5432:5432 &
        local port_forward_pid=$!
        
        # Wait for port forward to be ready
        sleep 3
        
        # Run Maven Flyway migration
        ./mvnw flyway:migrate \
            -Dflyway.url="jdbc:postgresql://localhost:5432/$POSTGRES_DB" \
            -Dflyway.user="$POSTGRES_USER" \
            -Dflyway.password="$POSTGRES_PASSWORD" \
            -Dflyway.baselineOnMigrate=true
        
        # Clean up port forward
        kill $port_forward_pid 2>/dev/null || true
    fi
    
    log_success "Maven Flyway migration completed"
}

# Function to show migration info
show_migration_info() {
    log_info "Running Flyway info command..."
    
    local postgres_url
    postgres_url=$(get_postgres_connection)
    
    # Create and run info job
    cat << EOF | kubectl apply -f -
apiVersion: batch/v1
kind: Job
metadata:
  name: flyway-info-$(date +%s)
  namespace: $NAMESPACE
  labels:
    app: flyway-info
spec:
  ttlSecondsAfterFinished: 60
  template:
    metadata:
      labels:
        app: flyway-info
    spec:
      restartPolicy: Never
      containers:
      - name: flyway
        image: flyway/flyway:$FLYWAY_VERSION
        command: ["flyway"]
        args: ["info"]
        env:
        - name: FLYWAY_URL
          value: "$postgres_url"
        - name: FLYWAY_USER
          value: "$POSTGRES_USER"
        - name: FLYWAY_PASSWORD
          value: "$POSTGRES_PASSWORD"
EOF

    # Wait for job to complete and show logs
    local job_name
    job_name=$(kubectl get jobs -n "$NAMESPACE" -l app=flyway-info --sort-by=.metadata.creationTimestamp -o jsonpath='{.items[-1].metadata.name}')
    
    kubectl wait --for=condition=complete job/"$job_name" --namespace="$NAMESPACE" --timeout=300s
    kubectl logs job/"$job_name" --namespace="$NAMESPACE"
}

# Function to validate migrations
validate_migrations() {
    log_info "Validating database migrations..."
    
    local postgres_url
    postgres_url=$(get_postgres_connection)
    
    # Create and run validation job
    cat << EOF | kubectl apply -f -
apiVersion: batch/v1
kind: Job
metadata:
  name: flyway-validate-$(date +%s)
  namespace: $NAMESPACE
  labels:
    app: flyway-validate
spec:
  ttlSecondsAfterFinished: 60
  template:
    metadata:
      labels:
        app: flyway-validate
    spec:
      restartPolicy: Never
      containers:
      - name: flyway
        image: flyway/flyway:$FLYWAY_VERSION
        command: ["flyway"]
        args: ["validate"]
        env:
        - name: FLYWAY_URL
          value: "$postgres_url"
        - name: FLYWAY_USER
          value: "$POSTGRES_USER"
        - name: FLYWAY_PASSWORD
          value: "$POSTGRES_PASSWORD"
EOF

    # Wait for job to complete and show logs
    local job_name
    job_name=$(kubectl get jobs -n "$NAMESPACE" -l app=flyway-validate --sort-by=.metadata.creationTimestamp -o jsonpath='{.items[-1].metadata.name}')
    
    kubectl wait --for=condition=complete job/"$job_name" --namespace="$NAMESPACE" --timeout=300s
    kubectl logs job/"$job_name" --namespace="$NAMESPACE"
    
    log_success "Migration validation completed"
}

# Function to show migration status
show_migration_status() {
    log_info "Database Migration Status:"
    echo "=========================="
    echo "Namespace: $NAMESPACE"
    echo "PostgreSQL Service: $POSTGRES_SERVICE"
    echo "Database: $POSTGRES_DB"
    echo "User: $POSTGRES_USER"
    echo "Migration Path: $MIGRATION_PATH"
    echo "=========================="
    
    # List migration files
    log_info "Available migration files:"
    if [ -d "$MIGRATION_PATH" ]; then
        ls -la "$MIGRATION_PATH"/*.sql 2>/dev/null || log_info "No migration files found"
    fi
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS] [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  migrate                      Run database migrations (default)"
    echo "  info                         Show migration information"
    echo "  validate                     Validate applied migrations"
    echo "  status                       Show migration status"
    echo ""
    echo "Options:"
    echo "  -n, --namespace NAMESPACE    Kubernetes namespace (default: interface-exception-collector)"
    echo "  -s, --service SERVICE        PostgreSQL service name (default: postgresql)"
    echo "  -p, --port PORT              PostgreSQL port (default: 5432)"
    echo "  -d, --database DATABASE      Database name (default: interface_exceptions)"
    echo "  -u, --user USER              Database user (default: exception_collector)"
    echo "  --password PASSWORD          Database password (default: exception_collector_pass)"
    echo "  --migration-path PATH        Migration files path (default: src/main/resources/db/migration)"
    echo "  --local                      Run migrations locally with port forwarding"
    echo "  --kubernetes                 Run migrations in Kubernetes (default)"
    echo "  -h, --help                   Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  NAMESPACE                    Kubernetes namespace"
    echo "  POSTGRES_SERVICE             PostgreSQL service name"
    echo "  POSTGRES_PORT                PostgreSQL port"
    echo "  POSTGRES_DB                  Database name"
    echo "  POSTGRES_USER                Database user"
    echo "  POSTGRES_PASSWORD            Database password"
    echo "  MIGRATION_PATH               Migration files path"
}

# Parse command line arguments
COMMAND="migrate"
RUN_LOCAL=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -s|--service)
            POSTGRES_SERVICE="$2"
            shift 2
            ;;
        -p|--port)
            POSTGRES_PORT="$2"
            shift 2
            ;;
        -d|--database)
            POSTGRES_DB="$2"
            shift 2
            ;;
        -u|--user)
            POSTGRES_USER="$2"
            shift 2
            ;;
        --password)
            POSTGRES_PASSWORD="$2"
            shift 2
            ;;
        --migration-path)
            MIGRATION_PATH="$2"
            shift 2
            ;;
        --local)
            RUN_LOCAL=true
            shift
            ;;
        --kubernetes)
            RUN_LOCAL=false
            shift
            ;;
        migrate|info|validate|status)
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
        migrate)
            check_prerequisites
            wait_for_postgresql
            test_database_connection
            if [ "$RUN_LOCAL" = true ]; then
                run_migrations_local
            else
                run_migrations_k8s
            fi
            ;;
        info)
            check_prerequisites
            wait_for_postgresql
            show_migration_info
            ;;
        validate)
            check_prerequisites
            wait_for_postgresql
            validate_migrations
            ;;
        status)
            show_migration_status
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