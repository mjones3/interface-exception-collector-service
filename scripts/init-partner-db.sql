-- Partner Order Service Database Initialization Script
-- This script creates the necessary database and user for the Partner Order Service

-- Create database if it doesn't exist (this is handled by POSTGRES_DB environment variable)
-- CREATE DATABASE IF NOT EXISTS partner_order_db;

-- Grant necessary permissions to the partner_order_user
GRANT ALL PRIVILEGES ON DATABASE partner_order_db TO partner_order_user;

-- Create schema if needed (using public schema by default)
-- CREATE SCHEMA IF NOT EXISTS public;

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO partner_order_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO partner_order_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO partner_order_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO partner_order_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO partner_order_user;

-- Log initialization completion
SELECT 'Partner Order Service database initialized successfully' AS status;