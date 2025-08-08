# Database Troubleshooting Runbook

## Overview

This runbook covers common database-related issues and their resolution procedures for the Interface Exception Collector Service.

## Quick Diagnostics

### Connection Health Check

```bash
# Basic connectivity test
pg_isready -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER

# Test with credentials
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME -c "SELECT 1"

# Check from application pod
kubectl exec -it <pod-name> -- pg_isready -h $DATABASE_HOST -p $DATABASE_PORT
```

### Database Status

```bash
# Check database size
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME \
  -c "SELECT pg_size_pretty(pg_database_size('$DATABASE_NAME'))"

# Check active connections
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME \
  -c "SELECT count(*) FROM pg_stat_activity WHERE state = 'active'"

# Check table sizes
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME \
  -c "SELECT schemaname,tablename,pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size 
      FROM pg_tables WHERE schemaname = 'public' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC"
```

## Common Issues and Solutions

### Issue 1: Connection Pool Exhausted

**Symptoms:**
- Application logs show "Connection pool exhausted" errors
- High response times for database operations
- HTTP 500 errors from API endpoints

**Diagnosis:**

```bash
# Check connection pool metrics
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
curl http://localhost:8080/actuator/metrics/hikaricp.connections.pending

# Check database connections
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME \
  -c "SELECT count(*), state FROM pg_stat_activity WHERE usename = '$DATABASE_USER' GROUP BY state"
```

**Resolution:**

1. **Immediate fix - Restart application**:
   ```bash
   kubectl rollout restart deployment/interface-exception-collector
   ```

2. **Increase connection pool size**:
   ```yaml
   # In application.yml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20  # Increase from default 10
         minimum-idle: 5
         connection-timeout: 30000
   ```

3. **Optimize long-running queries**:
   ```sql
   -- Find long-running queries
   SELECT pid, now() - pg_stat_activity.query_start AS duration, query 
   FROM pg_stat_activity 
   WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes';
   ```

### Issue 2: Slow Query Performance

**Symptoms:**
- API endpoints timing out
- High database CPU usage
- Slow response times

**Diagnosis:**

```sql
-- Enable query logging (if not already enabled)
ALTER SYSTEM SET log_min_duration_statement = 1000; -- Log queries > 1 second
SELECT pg_reload_conf();

-- Check slow queries
SELECT query, mean_exec_time, calls, total_exec_time
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;

-- Check missing indexes
SELECT schemaname, tablename, attname, n_distinct, correlation 
FROM pg_stats 
WHERE schemaname = 'public' 
  AND n_distinct > 100 
  AND correlation < 0.1;
```

**Resolution:**

1. **Add missing indexes**:
   ```sql
   -- Common indexes for exception queries
   CREATE INDEX CONCURRENTLY idx_interface_exceptions_customer_timestamp 
   ON interface_exceptions(customer_id, timestamp DESC);
   
   CREATE INDEX CONCURRENTLY idx_interface_exceptions_status_severity 
   ON interface_exceptions(status, severity) WHERE status IN ('NEW', 'ACKNOWLEDGED');
   ```

2. **Optimize queries**:
   ```sql
   -- Use EXPLAIN ANALYZE to understand query plans
   EXPLAIN (ANALYZE, BUFFERS) 
   SELECT * FROM interface_exceptions 
   WHERE customer_id = 'CUST001' 
   ORDER BY timestamp DESC 
   LIMIT 20;
   ```

3. **Update table statistics**:
   ```sql
   ANALYZE interface_exceptions;
   ANALYZE retry_attempts;
   ```

### Issue 3: Database Disk Space Full

**Symptoms:**
- "No space left on device" errors
- Database write operations failing
- Application unable to store new exceptions

**Diagnosis:**

```bash
# Check disk usage
df -h

# Check database size
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME \
  -c "SELECT pg_size_pretty(pg_database_size('$DATABASE_NAME'))"

# Check largest tables
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME \
  -c "SELECT schemaname,tablename,pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size 
      FROM pg_tables WHERE schemaname = 'public' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC"
```

**Resolution:**

1. **Immediate cleanup**:
   ```sql
   -- Clean up old exceptions (older than 90 days)
   DELETE FROM retry_attempts 
   WHERE exception_id IN (
     SELECT id FROM interface_exceptions 
     WHERE created_at < NOW() - INTERVAL '90 days'
   );
   
   DELETE FROM interface_exceptions 
   WHERE created_at < NOW() - INTERVAL '90 days';
   
   -- Vacuum to reclaim space
   VACUUM FULL interface_exceptions;
   VACUUM FULL retry_attempts;
   ```

2. **Archive old data**:
   ```bash
   # Export old data before deletion
   pg_dump -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER \
     --table=interface_exceptions \
     --where="created_at < '$(date -d '90 days ago' '+%Y-%m-%d')'" \
     $DATABASE_NAME > exceptions_archive_$(date +%Y%m%d).sql
   ```

3. **Increase disk space**:
   ```bash
   # For cloud environments, resize the volume
   kubectl patch pvc postgres-pvc -p '{"spec":{"resources":{"requests":{"storage":"200Gi"}}}}'
   ```

### Issue 4: Migration Failures

**Symptoms:**
- Application fails to start
- Migration errors in logs
- Database schema version mismatch

**Diagnosis:**

```sql
-- Check migration status
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

-- Check for failed migrations
SELECT * FROM flyway_schema_history WHERE success = false;

-- Check current schema version
SELECT version FROM flyway_schema_history 
WHERE success = true 
ORDER BY installed_rank DESC 
LIMIT 1;
```

**Resolution:**

1. **Manual migration repair**:
   ```sql
   -- Mark failed migration as successful (if manually fixed)
   UPDATE flyway_schema_history 
   SET success = true 
   WHERE version = 'X.X' AND success = false;
   ```

2. **Rollback and retry**:
   ```bash
   # Rollback to previous version
   ./mvnw flyway:undo -Dflyway.target=X.X
   
   # Retry migration
   ./mvnw flyway:migrate
   ```

3. **Manual schema fix**:
   ```sql
   -- Apply missing schema changes manually
   -- (Based on the specific migration that failed)
   ```

### Issue 5: Deadlocks

**Symptoms:**
- "Deadlock detected" errors in logs
- Transactions timing out
- Inconsistent data states

**Diagnosis:**

```sql
-- Check for deadlocks in logs
SELECT * FROM pg_stat_database_conflicts WHERE datname = '$DATABASE_NAME';

-- Monitor lock waits
SELECT blocked_locks.pid AS blocked_pid,
       blocked_activity.usename AS blocked_user,
       blocking_locks.pid AS blocking_pid,
       blocking_activity.usename AS blocking_user,
       blocked_activity.query AS blocked_statement,
       blocking_activity.query AS current_statement_in_blocking_process
FROM pg_catalog.pg_locks blocked_locks
JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
JOIN pg_catalog.pg_locks blocking_locks ON blocking_locks.locktype = blocked_locks.locktype
JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.granted;
```

**Resolution:**

1. **Identify and optimize conflicting queries**:
   ```sql
   -- Add explicit ordering to prevent deadlocks
   -- Always acquire locks in the same order
   ```

2. **Reduce transaction scope**:
   ```java
   // Split large transactions into smaller ones
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public void processInSmallBatches() {
       // Process in smaller chunks
   }
   ```

3. **Add retry logic**:
   ```java
   @Retryable(value = {DeadlockLoserDataAccessException.class}, maxAttempts = 3)
   public void retryableOperation() {
       // Database operation that might deadlock
   }
   ```

## Performance Monitoring

### Key Metrics to Monitor

```bash
# Connection pool metrics
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
curl http://localhost:8080/actuator/metrics/hikaricp.connections.idle
curl http://localhost:8080/actuator/metrics/hikaricp.connections.pending

# Query performance metrics
curl http://localhost:8080/actuator/metrics/spring.data.repository.invocations

# Database-specific metrics
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME \
  -c "SELECT * FROM pg_stat_database WHERE datname = '$DATABASE_NAME'"
```

### Automated Monitoring Script

```bash
#!/bin/bash
# db-monitor.sh

DB_HOST=$1
DB_PORT=$2
DB_USER=$3
DB_NAME=$4

while true; do
  # Check connection count
  CONN_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t \
    -c "SELECT count(*) FROM pg_stat_activity WHERE state = 'active'")
  
  if [ $CONN_COUNT -gt 15 ]; then
    echo "$(date): High connection count: $CONN_COUNT"
  fi
  
  # Check for long-running queries
  LONG_QUERIES=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t \
    -c "SELECT count(*) FROM pg_stat_activity WHERE (now() - query_start) > interval '5 minutes'")
  
  if [ $LONG_QUERIES -gt 0 ]; then
    echo "$(date): Long-running queries detected: $LONG_QUERIES"
  fi
  
  sleep 60
done
```

## Backup and Recovery

### Create Backup

```bash
# Full database backup
pg_dump -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER \
  --format=custom --compress=9 --verbose \
  $DATABASE_NAME > backup_$(date +%Y%m%d_%H%M%S).dump

# Schema-only backup
pg_dump -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER \
  --schema-only $DATABASE_NAME > schema_backup_$(date +%Y%m%d).sql

# Data-only backup
pg_dump -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER \
  --data-only $DATABASE_NAME > data_backup_$(date +%Y%m%d).sql
```

### Restore from Backup

```bash
# Restore full backup
pg_restore -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER \
  --dbname=$DATABASE_NAME --verbose backup_20250805_120000.dump

# Restore specific table
pg_restore -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER \
  --dbname=$DATABASE_NAME --table=interface_exceptions backup_20250805_120000.dump
```

## Maintenance Tasks

### Regular Maintenance

```sql
-- Weekly maintenance script
-- Update table statistics
ANALYZE;

-- Vacuum to reclaim space
VACUUM (ANALYZE, VERBOSE);

-- Reindex if needed
REINDEX DATABASE $DATABASE_NAME;

-- Check for bloated tables
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
       pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) as index_size
FROM pg_tables 
WHERE schemaname = 'public' 
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Index Maintenance

```sql
-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan ASC;

-- Find unused indexes
SELECT schemaname, tablename, indexname
FROM pg_stat_user_indexes
WHERE idx_scan = 0 AND schemaname = 'public';

-- Rebuild fragmented indexes
REINDEX INDEX CONCURRENTLY idx_interface_exceptions_timestamp;
```

## Emergency Procedures

### Database Corruption

1. **Stop the application**:
   ```bash
   kubectl scale deployment interface-exception-collector --replicas=0
   ```

2. **Check corruption**:
   ```sql
   -- Check for corruption
   SELECT * FROM pg_stat_database WHERE datname = '$DATABASE_NAME';
   ```

3. **Restore from backup**:
   ```bash
   # Restore from latest backup
   pg_restore -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER \
     --dbname=$DATABASE_NAME --clean --if-exists backup_latest.dump
   ```

### Complete Database Failure

1. **Provision new database instance**
2. **Restore from latest backup**
3. **Update application configuration**
4. **Restart application**

## Contact Information

- **Database Team**: db-team@biopro.com
- **On-call DBA**: +1-555-0124
- **Infrastructure Team**: infra-team@biopro.com

## Related Documentation

- [Service Lifecycle Management](service-lifecycle.md)
- [Performance Tuning](performance-tuning.md)
- [Disaster Recovery](disaster-recovery.md)