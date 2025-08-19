#!/bin/bash

# Production startup script for Interface Exception Collector Service
# This script configures JVM parameters optimized for GraphQL performance

set -euo pipefail

# Configuration
APP_NAME="interface-exception-collector"
APP_VERSION="${BUILD_VERSION:-1.0.0-SNAPSHOT}"
JAR_FILE="target/${APP_NAME}-${APP_VERSION}.jar"
JVM_CONFIG_FILE="jvm-production.conf"
LOG_DIR="/var/log/${APP_NAME}"
PID_FILE="/var/run/${APP_NAME}.pid"

# Create necessary directories
mkdir -p "${LOG_DIR}"
mkdir -p "/var/log/gc"
mkdir -p "/var/log/jfr"
mkdir -p "/var/log/heapdumps"

# Set permissions
chmod 755 "${LOG_DIR}"
chmod 755 "/var/log/gc"
chmod 755 "/var/log/jfr"
chmod 755 "/var/log/heapdumps"

# Function to check if application is already running
check_running() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        if ps -p "${pid}" > /dev/null 2>&1; then
            echo "Application is already running with PID: ${pid}"
            exit 1
        else
            echo "Removing stale PID file"
            rm -f "${PID_FILE}"
        fi
    fi
}

# Function to wait for application readiness
wait_for_readiness() {
    local max_attempts=60
    local attempt=1
    local health_url="${HEALTH_CHECK_URL:-http://localhost:8080/actuator/health}"
    
    echo "Waiting for application to be ready..."
    
    while [ ${attempt} -le ${max_attempts} ]; do
        if curl -f -s "${health_url}" > /dev/null 2>&1; then
            echo "Application is ready!"
            return 0
        fi
        
        echo "Attempt ${attempt}/${max_attempts}: Application not ready yet..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo "Application failed to become ready within timeout"
    return 1
}

# Function to perform graceful shutdown
graceful_shutdown() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        echo "Performing graceful shutdown of PID: ${pid}"
        
        # Send SIGTERM for graceful shutdown
        kill -TERM "${pid}" 2>/dev/null || true
        
        # Wait for graceful shutdown (max 30 seconds)
        local count=0
        while [ ${count} -lt 30 ] && ps -p "${pid}" > /dev/null 2>&1; do
            sleep 1
            count=$((count + 1))
        done
        
        # Force kill if still running
        if ps -p "${pid}" > /dev/null 2>&1; then
            echo "Force killing application"
            kill -KILL "${pid}" 2>/dev/null || true
        fi
        
        rm -f "${PID_FILE}"
        echo "Application stopped"
    fi
}

# Function to start application
start_application() {
    echo "Starting ${APP_NAME} version ${APP_VERSION}"
    
    # Check if JAR file exists
    if [ ! -f "${JAR_FILE}" ]; then
        echo "Error: JAR file not found: ${JAR_FILE}"
        exit 1
    fi
    
    # Check if JVM config file exists
    if [ ! -f "${JVM_CONFIG_FILE}" ]; then
        echo "Error: JVM config file not found: ${JVM_CONFIG_FILE}"
        exit 1
    fi
    
    # Load JVM parameters from config file
    JVM_OPTS=$(grep -v '^#' "${JVM_CONFIG_FILE}" | grep -v '^$' | tr '\n' ' ')
    
    # Additional environment-specific JVM options
    ADDITIONAL_OPTS=""
    
    # Add memory settings based on available memory
    if [ -n "${JAVA_MEMORY_LIMIT:-}" ]; then
        ADDITIONAL_OPTS="${ADDITIONAL_OPTS} -Xmx${JAVA_MEMORY_LIMIT}"
    fi
    
    # Add GC logging with timestamp
    GC_LOG_FILE="/var/log/gc/gc-$(date +%Y%m%d-%H%M%S).log"
    ADDITIONAL_OPTS="${ADDITIONAL_OPTS} -Xloggc:${GC_LOG_FILE}"
    
    # Add JFR recording with timestamp
    JFR_FILE="/var/log/jfr/app-recording-$(date +%Y%m%d-%H%M%S).jfr"
    ADDITIONAL_OPTS="${ADDITIONAL_OPTS} -XX:StartFlightRecording=duration=0s,filename=${JFR_FILE},settings=profile"
    
    # Combine all JVM options
    ALL_JVM_OPTS="${JVM_OPTS} ${ADDITIONAL_OPTS}"
    
    echo "JVM Options: ${ALL_JVM_OPTS}"
    
    # Start the application
    nohup java ${ALL_JVM_OPTS} -jar "${JAR_FILE}" \
        > "${LOG_DIR}/application.log" 2>&1 &
    
    local pid=$!
    echo ${pid} > "${PID_FILE}"
    
    echo "Application started with PID: ${pid}"
    echo "Log file: ${LOG_DIR}/application.log"
    echo "GC log file: ${GC_LOG_FILE}"
    echo "JFR recording: ${JFR_FILE}"
    
    # Wait for application to be ready
    if wait_for_readiness; then
        echo "Application startup completed successfully"
    else
        echo "Application startup failed"
        graceful_shutdown
        exit 1
    fi
}

# Function to show application status
show_status() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        if ps -p "${pid}" > /dev/null 2>&1; then
            echo "Application is running with PID: ${pid}"
            
            # Show memory usage
            echo "Memory usage:"
            ps -p "${pid}" -o pid,ppid,rss,vsz,pcpu,pmem,cmd --no-headers
            
            # Show health status
            local health_url="${HEALTH_CHECK_URL:-http://localhost:8080/actuator/health}"
            echo "Health status:"
            curl -s "${health_url}" | jq '.' 2>/dev/null || echo "Health check failed"
        else
            echo "Application is not running (stale PID file)"
            rm -f "${PID_FILE}"
        fi
    else
        echo "Application is not running"
    fi
}

# Main script logic
case "${1:-start}" in
    start)
        check_running
        start_application
        ;;
    stop)
        graceful_shutdown
        ;;
    restart)
        graceful_shutdown
        sleep 2
        start_application
        ;;
    status)
        show_status
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac