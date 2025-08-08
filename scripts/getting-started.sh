#!/bin/bash

# Interface Exception Collector - Getting Started Script
# This script helps new developers set up their local development environment

set -e

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

# Function to display welcome message
show_welcome() {
    echo "=========================================="
    echo "Interface Exception Collector Service"
    echo "Getting Started Script"
    echo "=========================================="
    echo ""
    log_info "This script will help you set up your local development environment"
    echo ""
}

# Function to check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    local missing_tools=()
    
    # Check required tools
    if ! command -v docker &> /dev/null; then
        missing_tools+=("docker")
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        missing_tools+=("docker-compose")
    fi
    
    if ! command -v java &> /dev/null; then
        missing_tools+=("java")
    fi
    
    if ! command -v mvn &> /dev/null && [ ! -f "./mvnw" ]; then
        missing_tools+=("maven")
    fi
    
    if ! command -v curl &> /dev/null; then
        missing_tools+=("curl")
    fi
    
    if ! command -v jq &> /dev/null; then
        missing_tools+=("jq")
    fi
    
    # Report missing tools
    if [ ${#missing_tools[@]} -gt 0 ]; then
        log_error "The following required tools are missing:"
        for tool in "${missing_tools[@]}"; do
            echo "  - $tool"
        done
        echo ""
        log_info "Please install the missing tools and run this script again"
        echo ""
        log_info "Installation guides:"
        echo "  Docker: https://docs.docker.com/get-docker/"
        echo "  Java: https://adoptium.net/"
        echo "  Maven: https://maven.apache.org/install.html"
        echo "  jq: https://stedolan.github.io/jq/download/"
        exit 1
    fi
    
    # Check Docker is running
    if ! docker info &> /dev/null; then
        log_error "Docker is not running. Please start Docker Desktop or Docker daemon."
        exit 1
    fi
    
    log_success "All prerequisites are available"
}

# Function to check Java version
check_java_version() {
    log_info "Checking Java version..."
    
    local java_version
    java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
    
    if [ "$java_version" -ge 17 ]; then
        log_success "Java version is compatible ($java_version)"
    else
        log_warning "Java version $java_version detected. Java 17+ is recommended."
    fi
}

# Function to make scripts executable
setup_scripts() {
    log_info "Setting up scripts..."
    
    chmod +x scripts/*.sh
    
    log_success "Scripts are now executable"
}

# Function to start infrastructure
start_infrastructure() {
    log_info "Starting infrastructure services..."
    
    # Start Docker Compose services
    docker-compose up -d postgres kafka redis
    
    log_info "Waiting for services to be ready..."
    
    # Wait for services with timeout
    if ./scripts/validate-infrastructure.sh --timeout 120; then
        log_success "Infrastructure services are ready"
    else
        log_error "Infrastructure services failed to start properly"
        log_info "You can check the logs with: docker-compose logs"
        return 1
    fi
}

# Function to run database migrations
setup_database() {
    log_info "Setting up database..."
    
    if ./scripts/run-migrations.sh --local; then
        log_success "Database migrations completed"
    else
        log_error "Database migration failed"
        return 1
    fi
}

# Function to create Kafka topics
setup_kafka() {
    log_info "Setting up Kafka topics..."
    
    if ./scripts/create-kafka-topics.sh --local; then
        log_success "Kafka topics created"
    else
        log_warning "Kafka topic creation failed - topics will be auto-created"
    fi
}

# Function to build the application
build_application() {
    log_info "Building the application..."
    
    if ./mvnw clean compile -DskipTests; then
        log_success "Application built successfully"
    else
        log_error "Application build failed"
        return 1
    fi
}

# Function to run smoke tests
run_smoke_tests() {
    log_info "Running smoke tests..."
    
    # Start the application in background
    log_info "Starting application..."
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=local &
    local app_pid=$!
    
    # Wait for application to start
    sleep 30
    
    # Run smoke tests
    if ./scripts/smoke-tests.sh local --timeout=60; then
        log_success "Smoke tests passed"
    else
        log_warning "Smoke tests failed - application may still be starting"
    fi
    
    # Stop the application
    kill $app_pid 2>/dev/null || true
    wait $app_pid 2>/dev/null || true
}

# Function to show next steps
show_next_steps() {
    echo ""
    log_success "Setup completed successfully!"
    echo ""
    echo "=========================================="
    echo "Next Steps:"
    echo "=========================================="
    echo ""
    echo "1. Start the application:"
    echo "   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local"
    echo ""
    echo "2. Access the application:"
    echo "   - Application: http://localhost:8080"
    echo "   - Health Check: http://localhost:8080/actuator/health"
    echo "   - Swagger UI: http://localhost:8080/swagger-ui/index.html"
    echo ""
    echo "3. For development with hot reload, use Tilt:"
    echo "   ./scripts/deploy-local.sh"
    echo "   # Then access Tilt UI at http://localhost:10350"
    echo ""
    echo "4. Run tests:"
    echo "   ./mvnw test                              # Unit tests"
    echo "   ./scripts/smoke-tests.sh local          # Smoke tests"
    echo "   ./scripts/integration-tests.sh local    # Integration tests"
    echo ""
    echo "5. Stop infrastructure when done:"
    echo "   docker-compose down"
    echo ""
    echo "=========================================="
    echo "Useful Commands:"
    echo "=========================================="
    echo ""
    echo "# Check infrastructure status"
    echo "./scripts/validate-infrastructure.sh --status-only"
    echo ""
    echo "# View application logs"
    echo "docker-compose logs -f interface-exception-collector"
    echo ""
    echo "# View database logs"
    echo "docker-compose logs -f postgres"
    echo ""
    echo "# Connect to database"
    echo "docker-compose exec postgres psql -U exception_collector -d interface_exceptions"
    echo ""
    echo "# View Kafka topics"
    echo "docker-compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list"
    echo ""
    echo "For more information, see:"
    echo "- README.md"
    echo "- docs/DEPLOYMENT_GUIDE.md"
    echo "- TILT_DEVELOPMENT.md"
    echo ""
}

# Function to cleanup on failure
cleanup_on_failure() {
    log_error "Setup failed. Cleaning up..."
    
    # Stop any running processes
    pkill -f "spring-boot:run" 2>/dev/null || true
    
    # Stop Docker Compose services
    docker-compose down 2>/dev/null || true
    
    log_info "Cleanup completed. You can try running the script again."
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --skip-tests                Skip smoke tests"
    echo "  --skip-build                Skip application build"
    echo "  --infrastructure-only       Only set up infrastructure"
    echo "  -h, --help                  Show this help message"
    echo ""
    echo "This script will:"
    echo "1. Check prerequisites"
    echo "2. Start infrastructure services (PostgreSQL, Kafka, Redis)"
    echo "3. Run database migrations"
    echo "4. Create Kafka topics"
    echo "5. Build the application"
    echo "6. Run smoke tests"
    echo ""
}

# Parse command line arguments
SKIP_TESTS=false
SKIP_BUILD=false
INFRASTRUCTURE_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --infrastructure-only)
            INFRASTRUCTURE_ONLY=true
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

# Set up error handling
trap cleanup_on_failure ERR

# Main function
main() {
    show_welcome
    
    check_prerequisites
    check_java_version
    setup_scripts
    
    start_infrastructure
    setup_database
    setup_kafka
    
    if [ "$INFRASTRUCTURE_ONLY" = true ]; then
        log_success "Infrastructure setup completed!"
        log_info "You can now start the application manually with:"
        log_info "./mvnw spring-boot:run -Dspring-boot.run.profiles=local"
        exit 0
    fi
    
    if [ "$SKIP_BUILD" != true ]; then
        build_application
    fi
    
    if [ "$SKIP_TESTS" != true ]; then
        run_smoke_tests
    fi
    
    show_next_steps
}

# Run main function
main "$@"