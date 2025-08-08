-- Database initialization script for Docker development environment
-- This script runs when the PostgreSQL container starts for the first time

-- Create additional schemas if needed
-- CREATE SCHEMA IF NOT EXISTS audit;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE exception_collector_db TO exception_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO exception_user;

-- Create sequences with proper ownership
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO exception_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO exception_user;

-- Log initialization completion
SELECT 'Database initialization completed successfully' AS status;