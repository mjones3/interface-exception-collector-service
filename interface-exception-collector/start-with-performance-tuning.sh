#!/bin/bash

# GraphQL Mutation Performance Optimization Startup Script
# This script starts the Interface Exception Collector with optimized JVM parameters
# for GraphQL mutation performance.

set -e

# Configuration
APP_NAME="interface-exception-collector"
JAR_FILE="target/${APP_NAME}-*.jar"
LOG_DIR="logs"
PID_FILE="${APP_NAME}.pid"

# JVM Performance Tuning Parameters
HEAP_SIZE_INITIAL="512m"
HEAP_SIZE_MAX="2048m"
GC_MAX_PAUSE="200"

# Create logs directory if it doesn't exist
mkdir -p "$LOG_DIR"

# Function to print colored output
print_info() {
    echo -e "\033[1;34m[INFO]\033[0m $1"
}

print_success() {
    echo -e "\033[1;32m[SUCCESS]\033[0m $1"
}

print_warning() {
    echo -e "\033[1;33m[WARNING]\033[0m $1"
}

print_error() {
    echo -e "\033[1;31m[ERROR]\033[0m $1"
}

# Function to check if application is already running
check_if_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            print_error "Application is already running with PID $PID"
            exit 1
        else
            print_warning "Stale PID file found. Removing..."
            rm -f "$PID_FILE"
        fi
    fi
}

# Function to validate Java version
validate_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
    print_info "Java version: $JAVA_VERSION"
    
    # Check if Java 11 or higher
    if [[ $(echo "$JAVA_VERSION" | cut -d'.' -f1) -lt 11 ]]; then
        print_warning "Java 11 or higher is recommended for optimal performance"
    fi
}

# Function to check available memory
check_memory() {
    if command -v free &> /dev/null; then
        AVAILABLE_MEMORY=$(free -m | awk 'NR==2{printf "%.0f", $7}')
        REQUIRED_MEMORY=2560  # 2.5GB (2048MB heap + 512MB overhead)
        
        print_info "Available memory: ${AVAILABLE_MEMORY}MB"
        print_info "Required memory: ${REQUIRED_MEMORY}MB"
        
        if [ "$AVAILABLE_MEMORY" -lt "$REQUIRED_MEMORY" ]; then
            print_warning "Available memory might be insufficient for optimal performance"
            print_warning "Consider reducing heap size or adding more memory"
        fi
    fi
}

# Function to build JVM arguments
build_jvm_args() {
    JVM_ARGS=""
    
    # Memory settings
    JVM_ARGS="$JVM_ARGS -Xms$HEAP_SIZE_INITIAL"
    JVM_ARGS="$JVM_ARGS -Xmx$HEAP_SIZE_MAX"
    
    # G1GC settings for low latency
    JVM_ARGS="$JVM_ARGS -XX:+UseG1GC"
    JVM_ARGS="$JVM_ARGS -XX:MaxGCPauseMillis=$GC_MAX_PAUSE"
    JVM_ARGS="$JVM_ARGS -XX:G1HeapRegionSize=16m"
    JVM_ARGS="$JVM_ARGS -XX:+UseStringDeduplication"
    
    # Performance optimizations
    JVM_ARGS="$JVM_ARGS -XX:+UseCompressedOops"
    JVM_ARGS="$JVM_ARGS -XX:+UseCompressedClassPointers"
    JVM_ARGS="$JVM_ARGS -XX:+TieredCompilation"
    JVM_ARGS="$JVM_ARGS -XX:TieredStopAtLevel=4"
    JVM_ARGS="$JVM_ARGS -XX:+OptimizeStringConcat"
    
    # System properties
    JVM_ARGS="$JVM_ARGS -Djava.awt.headless=true"
    JVM_ARGS="$JVM_ARGS -Dfile.encoding=UTF-8"
    JVM_ARGS="$JVM_ARGS -Djava.security.egd=file:/dev/./urandom"
    
    # GraphQL-specific optimizations
    JVM_ARGS="$JVM_ARGS -Dspring.graphql.websocket.connection-init-timeout=60s"
    JVM_ARGS="$JVM_ARGS -Dspring.graphql.schema.printer.enabled=false"
    
    # JVM monitoring and debugging (optional)
    if [ "$ENABLE_JVM_MONITORING" = "true" ]; then
        JVM_ARGS="$JVM_ARGS -XX:+PrintGCDetails"
        JVM_ARGS="$JVM_ARGS -XX:+PrintGCTimeStamps"
        JVM_ARGS="$JVM_ARGS -XX:+PrintGCApplicationStoppedTime"
        JVM_ARGS="$JVM_ARGS -Xloggc:$LOG_DIR/gc.log"
        JVM_ARGS="$JVM_ARGS -XX:+UseGCLogFileRotation"
        JVM_ARGS="$JVM_ARGS -XX:NumberOfGCLogFiles=5"
        JVM_ARGS="$JVM_ARGS -XX:GCLogFileSize=10M"
    fi
    
    # Remote debugging (if enabled)
    if [ "$ENABLE_DEBUG" = "true" ]; then
        DEBUG_PORT=${DEBUG_PORT:-5005}
        JVM_ARGS="$JVM_ARGS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$DEBUG_PORT"
        print_info "Remote debugging enabled on port $DEBUG_PORT"
    fi
    
    # JMX monitoring (if enabled)
    if [ "$ENABLE_JMX" = "true" ]; then
        JMX_PORT=${JMX_PORT:-9999}
        JVM_ARGS="$JVM_ARGS -Dcom.sun.management.jmxremote"
        JVM_ARGS="$JVM_ARGS -Dcom.sun.management.jmxremote.port=$JMX_PORT"
        JVM_ARGS="$JVM_ARGS -Dcom.sun.management.jmxremote.authenticate=false"
        JVM_ARGS="$JVM_ARGS -Dcom.sun.management.jmxremote.ssl=false"
        print_info "JMX monitoring enabled on port $JMX_PORT"
    fi
    
    echo "$JVM_ARGS"
}

# Function to start the application
start_application() {
    print_info "Starting $APP_NAME with performance optimizations..."
    
    # Find the JAR file
    if ! ls $JAR_FILE 1> /dev/null 2>&1; then
        print_error "JAR file not found: $JAR_FILE"
        print_info "Please run 'mvn clean package' first"
        exit 1
    fi
    
    JAR_PATH=$(ls $JAR_FILE | head -n 1)
    print_info "Using JAR: $JAR_PATH"
    
    # Build JVM arguments
    JVM_ARGS=$(build_jvm_args)
    print_info "JVM Arguments: $JVM_ARGS"
    
    # Start the application
    nohup java $JVM_ARGS -jar "$JAR_PATH" \
        > "$LOG_DIR/application.log" 2>&1 &
    
    APP_PID=$!
    echo $APP_PID > "$PID_FILE"
    
    print_success "Application started with PID $APP_PID"
    print_info "Logs are available in $LOG_DIR/application.log"
    
    # Wait a moment and check if the process is still running
    sleep 3
    if ps -p $APP_PID > /dev/null 2>&1; then
        print_success "Application is running successfully"
        print_info "GraphQL endpoint: http://localhost:8080/graphql"
        print_info "GraphiQL interface: http://localhost:8080/graphiql"
        print_info "Health check: http://localhost:8080/actuator/health"
    else
        print_error "Application failed to start. Check logs for details."
        rm -f "$PID_FILE"
        exit 1
    fi
}

# Function to stop the application
stop_application() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            print_info "Stopping application with PID $PID..."
            kill "$PID"
            
            # Wait for graceful shutdown
            for i in {1..30}; do
                if ! ps -p "$PID" > /dev/null 2>&1; then
                    print_success "Application stopped successfully"
                    rm -f "$PID_FILE"
                    return 0
                fi
                sleep 1
            done
            
            # Force kill if graceful shutdown failed
            print_warning "Graceful shutdown failed. Force killing..."
            kill -9 "$PID" 2>/dev/null || true
            rm -f "$PID_FILE"
            print_success "Application force stopped"
        else
            print_warning "PID file exists but process is not running"
            rm -f "$PID_FILE"
        fi
    else
        print_warning "Application is not running (no PID file found)"
    fi
}

# Function to show application status
show_status() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            print_success "Application is running with PID $PID"
            
            # Show memory usage if available
            if command -v ps &> /dev/null; then
                MEMORY_USAGE=$(ps -p "$PID" -o rss= | awk '{print int($1/1024)"MB"}')
                print_info "Memory usage: $MEMORY_USAGE"
            fi
            
            # Show CPU usage if available
            if command -v top &> /dev/null; then
                CPU_USAGE=$(top -p "$PID" -n 1 -b | tail -n 1 | awk '{print $9"%"}')
                print_info "CPU usage: $CPU_USAGE"
            fi
        else
            print_error "PID file exists but process is not running"
            rm -f "$PID_FILE"
        fi
    else
        print_warning "Application is not running"
    fi
}

# Function to show help
show_help() {
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  start     Start the application with performance optimizations"
    echo "  stop      Stop the application"
    echo "  restart   Restart the application"
    echo "  status    Show application status"
    echo "  help      Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  HEAP_SIZE_INITIAL     Initial heap size (default: 512m)"
    echo "  HEAP_SIZE_MAX         Maximum heap size (default: 2048m)"
    echo "  GC_MAX_PAUSE          Maximum GC pause time in ms (default: 200)"
    echo "  ENABLE_JVM_MONITORING Enable JVM monitoring and GC logging (default: false)"
    echo "  ENABLE_DEBUG          Enable remote debugging (default: false)"
    echo "  DEBUG_PORT            Remote debugging port (default: 5005)"
    echo "  ENABLE_JMX            Enable JMX monitoring (default: false)"
    echo "  JMX_PORT              JMX monitoring port (default: 9999)"
    echo ""
    echo "Examples:"
    echo "  $0 start"
    echo "  HEAP_SIZE_MAX=4096m $0 start"
    echo "  ENABLE_DEBUG=true DEBUG_PORT=5005 $0 start"
    echo "  ENABLE_JMX=true JMX_PORT=9999 $0 start"
}

# Main script logic
case "${1:-start}" in
    start)
        validate_java
        check_memory
        check_if_running
        start_application
        ;;
    stop)
        stop_application
        ;;
    restart)
        stop_application
        sleep 2
        validate_java
        check_memory
        start_application
        ;;
    status)
        show_status
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac