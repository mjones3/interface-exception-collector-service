#!/bin/bash

# Database Restore Script for Interface Exception Collector
# This script restores a database backup from S3 storage

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Default values
S3_BUCKET="${S3_BUCKET:-biopro-database-backups}"
AWS_REGION="${AWS_REGION:-us-west-2}"
BACKUP_FILE=""
RESTORE_MODE="full"  # full, data-only, schema-only
TARGET_DATABASE=""
CONFIRM_RESTORE="false"

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

# Help function
show_help() {
    cat << EOF
Database Restore Script for Interface Exception Collector

Usage: $0 [OPTIONS]

Options:
    -f, --backup-file FILENAME    Backup file to restore (required)
    -d, --database DATABASE       Target database name (required)
    -m, --mode MODE              Restore mode: full, data-only, schema-only (default: full)
    -b, --bucket BUCKET          S3 bucket name (default: biopro-database-backups)
    -r, --region REGION          AWS region (default: us-west-2)
    -c, --confirm                Confirm restore operation (required for safety)
    -h, --help                   Show this help message

Environment Variables:
    PGHOST                       PostgreSQL host
    PGPORT                       PostgreSQL port (default: 5432)
    PGUSER                       PostgreSQL username
    PGPASSWORD                   PostgreSQL password
    AWS_ACCESS_KEY_ID            AWS access key
    AWS_SECRET_ACCESS_KEY        AWS secret key

Examples:
    # List available backups
    $0 --list-backups

    # Restore full backup
    $0 -f exception_collector_backup_20250805_020000.sql -d exception_collector_db --confirm

    # Restore data only
    $0 -f exception_collector_backup_20250805_020000.sql -d exception_collector_db -m data-only --confirm

    # Restore to different database
    $0 -f exception_collector_backup_20250805_020000.sql -d exception_collector_test --confirm

EOF
}

# List available backups
list_backups() {
    log_info "Listing available backups from s3://${S3_BUCKET}/database-backups/"
    
    if ! aws s3 ls "s3://${S3_BUCKET}/database-backups/" --region "$AWS_REGION" 2>/dev/null; then
        log_error "Failed to list backups. Check AWS credentials and bucket access."
        exit 1
    fi
    
    echo
    log_info "To restore a backup, use:"
    echo "  $0 -f <backup-filename> -d <target-database> --confirm"
}

# Validate prerequisites
validate_prerequisites() {
    log_info "Validating prerequisites..."
    
    # Check required commands
    for cmd in aws pg_restore psql; do
        if ! command -v "$cmd" &> /dev/null; then
            log_error "Required command '$cmd' not found"
            exit 1
        fi
    done
    
    # Check AWS credentials
    if ! aws sts get-caller-identity --region "$AWS_REGION" &> /dev/null; then
        log_error "AWS credentials not configured or invalid"
        exit 1
    fi
    
    # Check PostgreSQL connection
    if ! psql --no-password --command "SELECT 1;" &> /dev/null; then
        log_error "Cannot connect to PostgreSQL. Check connection parameters."
        exit 1
    fi
    
    log_success "Prerequisites validated"
}

# Download backup from S3
download_backup() {
    local backup_file="$1"
    local local_path="/tmp/${backup_file}"
    
    log_info "Downloading backup: ${backup_file}"
    
    if ! aws s3 cp "s3://${S3_BUCKET}/database-backups/${backup_file}" "$local_path" --region "$AWS_REGION"; then
        log_error "Failed to download backup file"
        exit 1
    fi
    
    # Verify file was downloaded
    if [ ! -f "$local_path" ]; then
        log_error "Backup file not found after download"
        exit 1
    fi
    
    # Get file size
    local file_size=$(du -h "$local_path" | cut -f1)
    log_success "Backup downloaded: ${local_path} (${file_size})"
    
    echo "$local_path"
}

# Create target database if it doesn't exist
create_target_database() {
    local database="$1"
    
    log_info "Checking if target database exists: ${database}"
    
    if psql --no-password --tuples-only --command "SELECT 1 FROM pg_database WHERE datname='${database}';" | grep -q 1; then
        log_warning "Database '${database}' already exists"
        
        if [ "$CONFIRM_RESTORE" != "true" ]; then
            log_error "Restoring to existing database requires --confirm flag"
            exit 1
        fi
        
        read -p "Database '${database}' exists. Continue with restore? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Restore cancelled by user"
            exit 0
        fi
    else
        log_info "Creating target database: ${database}"
        createdb --no-password "$database"
        log_success "Database created: ${database}"
    fi
}

# Perform database restore
perform_restore() {
    local backup_path="$1"
    local database="$2"
    local mode="$3"
    
    log_info "Starting database restore..."
    log_info "  Backup: ${backup_path}"
    log_info "  Database: ${database}"
    log_info "  Mode: ${mode}"
    
    # Build pg_restore command
    local restore_cmd="pg_restore --no-password --verbose --dbname=${database}"
    
    case "$mode" in
        "data-only")
            restore_cmd="$restore_cmd --data-only"
            ;;
        "schema-only")
            restore_cmd="$restore_cmd --schema-only"
            ;;
        "full")
            restore_cmd="$restore_cmd --clean --if-exists"
            ;;
        *)
            log_error "Invalid restore mode: $mode"
            exit 1
            ;;
    esac
    
    # Add backup file
    restore_cmd="$restore_cmd $backup_path"
    
    log_info "Executing: $restore_cmd"
    
    # Execute restore
    if eval "$restore_cmd"; then
        log_success "Database restore completed successfully"
    else
        log_error "Database restore failed"
        exit 1
    fi
    
    # Validate restore
    log_info "Validating restored data..."
    
    local table_count=$(psql --no-password --tuples-only --dbname="$database" \
        --command "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';" | tr -d ' ')
    
    local exception_count=$(psql --no-password --tuples-only --dbname="$database" \
        --command "SELECT COUNT(*) FROM interface_exceptions;" 2>/dev/null | tr -d ' ' || echo "0")
    
    log_info "Validation results:"
    log_info "  Tables: ${table_count}"
    log_info "  Exceptions: ${exception_count}"
    
    # Run data integrity validation if functions exist
    if psql --no-password --tuples-only --dbname="$database" \
        --command "SELECT 1 FROM pg_proc WHERE proname='validate_migrated_data';" | grep -q 1; then
        
        log_info "Running data integrity validation..."
        psql --no-password --dbname="$database" --command "SELECT * FROM validate_migrated_data();"
    fi
}

# Cleanup temporary files
cleanup() {
    if [ -n "$BACKUP_PATH" ] && [ -f "$BACKUP_PATH" ]; then
        log_info "Cleaning up temporary files..."
        rm -f "$BACKUP_PATH"
    fi
}

# Trap cleanup on exit
trap cleanup EXIT

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--backup-file)
            BACKUP_FILE="$2"
            shift 2
            ;;
        -d|--database)
            TARGET_DATABASE="$2"
            shift 2
            ;;
        -m|--mode)
            RESTORE_MODE="$2"
            shift 2
            ;;
        -b|--bucket)
            S3_BUCKET="$2"
            shift 2
            ;;
        -r|--region)
            AWS_REGION="$2"
            shift 2
            ;;
        -c|--confirm)
            CONFIRM_RESTORE="true"
            shift
            ;;
        --list-backups)
            list_backups
            exit 0
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Main execution
main() {
    log_info "Interface Exception Collector - Database Restore"
    log_info "=============================================="
    
    # Handle list backups
    if [ -z "$BACKUP_FILE" ] && [ -z "$TARGET_DATABASE" ]; then
        list_backups
        exit 0
    fi
    
    # Validate required parameters
    if [ -z "$BACKUP_FILE" ]; then
        log_error "Backup file is required. Use -f or --backup-file"
        show_help
        exit 1
    fi
    
    if [ -z "$TARGET_DATABASE" ]; then
        log_error "Target database is required. Use -d or --database"
        show_help
        exit 1
    fi
    
    if [ "$CONFIRM_RESTORE" != "true" ]; then
        log_error "Restore operation requires confirmation. Use --confirm flag"
        exit 1
    fi
    
    # Validate prerequisites
    validate_prerequisites
    
    # Download backup
    BACKUP_PATH=$(download_backup "$BACKUP_FILE")
    
    # Create target database if needed
    create_target_database "$TARGET_DATABASE"
    
    # Perform restore
    perform_restore "$BACKUP_PATH" "$TARGET_DATABASE" "$RESTORE_MODE"
    
    log_success "Database restore completed successfully!"
    log_info "Database: ${TARGET_DATABASE}"
    log_info "Backup: ${BACKUP_FILE}"
    log_info "Mode: ${RESTORE_MODE}"
}

# Run main function
main "$@"