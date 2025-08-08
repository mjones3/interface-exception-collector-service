# Disaster Recovery and Backup Procedures

## Overview

This document outlines comprehensive disaster recovery and backup procedures for the Interface Exception Collector Service, covering data protection, system recovery, and business continuity strategies.

## Table of Contents

- [Recovery Objectives](#recovery-objectives)
- [Backup Strategy](#backup-strategy)
- [Database Backup and Recovery](#database-backup-and-recovery)
- [Application Recovery](#application-recovery)
- [Kafka Data Recovery](#kafka-data-recovery)
- [Configuration Backup](#configuration-backup)
- [Multi-Region Failover](#multi-region-failover)
- [Recovery Testing](#recovery-testing)
- [Emergency Procedures](#emergency-procedures)
- [Post-Recovery Validation](#post-recovery-validation)

## Recovery Objectives

### Service Level Objectives (SLOs)

- **Recovery Time Objective (RTO)**: 4 hours maximum downtime
- **Recovery Point Objective (RPO)**: 15 minutes maximum data loss
- **Availability Target**: 99.9% uptime
- **Data Retention**: 90 days for operational data, 7 years for audit data

### Business Impact Classification

| Scenario | Impact Level | RTO | RPO | Priority |
|----------|--------------|-----|-----|----------|
| Single pod failure | Low | 2 minutes | 0 | P4 |
| Database corruption | High | 2 hours | 15 minutes | P1 |
| Complete region failure | Critical | 4 hours | 15 minutes | P0 |
| Kafka cluster failure | High | 1 hour | 5 minutes | P1 |
| Configuration loss | Medium | 30 minutes | 0 | P2 |

## Backup Strategy

### Backup Schedule

```bash
# Automated backup schedule
# Database: Every 15 minutes (WAL), Daily full backup
# Configuration: Every change (Git), Daily snapshot
# Application state: Continuous (Kubernetes)
# Kafka: Continuous replication, Daily topic backup

# Backup retention policy
# Daily backups: 30 days
# Weekly backups: 12 weeks
# Monthly backups: 12 months
# Yearly backups: 7 years
```

### Backup Components

1. **Database Backups**
   - PostgreSQL WAL-E continuous archiving
   - Daily full database dumps
   - Point-in-time recovery capability

2. **Configuration Backups**
   - Kubernetes manifests in Git
   - Helm values files versioned
   - Environment-specific configurations

3. **Application State**
   - Container images in registry
   - Persistent volume snapshots
   - Kubernetes resource definitions

4. **Monitoring Data**
   - Prometheus metrics (30 days)
   - Grafana dashboards
   - Alert configurations

## Database Backup and Recovery

### Continuous Backup Setup

```bash
# WAL-E configuration for continuous archiving
export WALE_S3_PREFIX="s3://biopro-backups/postgres-wal"
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-key"

# PostgreSQL configuration
# postgresql.conf
wal_level = replica
archive_mode = on
archive_command = 'wal-e wal-push %p'
archive_timeout = 60  # Archive every minute
max_wal_senders = 3
wal_keep_segments = 32
```

### Daily Full Backup

```bash
#!/bin/bash
# scripts/backup-database.sh

set -e

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/postgres"
S3_BUCKET="s3://biopro-backups/postgres-full"

# Create backup directory
mkdir -p $BACKUP_DIR

# Perform full backup
pg_dump -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER \
  --format=custom --compress=9 --verbose \
  --file=$BACKUP_DIR/full_backup_$BACKUP_DATE.dump \
  $DATABASE_NAME

# Upload to S3
aws s3 cp $BACKUP_DIR/full_backup_$BACKUP_DATE.dump \
  $S3_BUCKET/full_backup_$BACKUP_DATE.dump

# Verify backup integrity
pg_restore --list $BACKUP_DIR/full_backup_$BACKUP_DATE.dump > /dev/null

# Cleanup old local backups (keep last 7 days)
find $BACKUP_DIR -name "full_backup_*.dump" -mtime +7 -delete

echo "Backup completed successfully: full_backup_$BACKUP_DATE.dump"
```

### Point-in-Time Recovery

```bash
#!/bin/bash
# scripts/restore-database-pitr.sh

RESTORE_TIME=$1  # Format: 2025-08-05 14:30:00
BACKUP_DATE=$2   # Base backup date

if [ -z "$RESTORE_TIME" ] || [ -z "$BACKUP_DATE" ]; then
    echo "Usage: $0 <restore-time> <backup-date>"
    echo "Example: $0 '2025-08-05 14:30:00' '20250805_120000'"
    exit 1
fi

# Stop PostgreSQL
systemctl stop postgresql

# Clear data directory
rm -rf /var/lib/postgresql/data/*

# Restore base backup
wal-e backup-fetch /var/lib/postgresql/data LATEST

# Create recovery configuration
cat > /var/lib/postgresql/data/recovery.conf << EOF
restore_command = 'wal-e wal-fetch %f %p'
recovery_target_time = '$RESTORE_TIME'
recovery_target_action = 'promote'
EOF

# Set permissions
chown -R postgres:postgres /var/lib/postgresql/data
chmod 700 /var/lib/postgresql/data

# Start PostgreSQL
systemctl start postgresql

echo "Point-in-time recovery initiated to $RESTORE_TIME"
```

### Database Recovery Verification

```bash
#!/bin/bash
# scripts/verify-database-recovery.sh

echo "Verifying database recovery..."

# Check database connectivity
if ! pg_isready -h $DATABASE_HOST -p $DATABASE_PORT; then
    echo "ERROR: Database is not ready"
    exit 1
fi

# Verify table existence and row counts
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME << EOF
\dt
SELECT 'interface_exceptions' as table_name, count(*) as row_count FROM interface_exceptions
UNION ALL
SELECT 'retry_attempts' as table_name, count(*) as row_count FROM retry_attempts;

-- Check data integrity
SELECT 
    COUNT(*) as total_exceptions,
    COUNT(DISTINCT transaction_id) as unique_transactions,
    MAX(created_at) as latest_exception
FROM interface_exceptions;
EOF

echo "Database recovery verification completed"
```

## Application Recovery

### Container Image Recovery

```bash
# Ensure container images are available in multiple registries
docker tag interface-exception-collector:${VERSION} \
  primary-registry/interface-exception-collector:${VERSION}
docker push primary-registry/interface-exception-collector:${VERSION}

docker tag interface-exception-collector:${VERSION} \
  backup-registry/interface-exception-collector:${VERSION}
docker push backup-registry/interface-exception-collector:${VERSION}
```

### Kubernetes Resource Recovery

```bash
#!/bin/bash
# scripts/backup-k8s-resources.sh

NAMESPACE="biopro-prod"
BACKUP_DIR="/backups/kubernetes/$(date +%Y%m%d)"

mkdir -p $BACKUP_DIR

# Backup all resources
kubectl get all,configmaps,secrets,pvc,ingress -n $NAMESPACE -o yaml > \
  $BACKUP_DIR/all-resources.yaml

# Backup specific resources
kubectl get deployment interface-exception-collector -n $NAMESPACE -o yaml > \
  $BACKUP_DIR/deployment.yaml

kubectl get service interface-exception-collector -n $NAMESPACE -o yaml > \
  $BACKUP_DIR/service.yaml

kubectl get configmap interface-exception-collector-config -n $NAMESPACE -o yaml > \
  $BACKUP_DIR/configmap.yaml

kubectl get secret interface-exception-collector-secret -n $NAMESPACE -o yaml > \
  $BACKUP_DIR/secret.yaml

# Upload to S3
aws s3 sync $BACKUP_DIR s3://biopro-backups/kubernetes/$(date +%Y%m%d)/

echo "Kubernetes resources backed up to $BACKUP_DIR"
```

### Application Recovery Procedure

```bash
#!/bin/bash
# scripts/recover-application.sh

ENVIRONMENT=$1
VERSION=$2

if [ -z "$ENVIRONMENT" ] || [ -z "$VERSION" ]; then
    echo "Usage: $0 <environment> <version>"
    exit 1
fi

echo "Starting application recovery for $ENVIRONMENT..."

# Set kubectl context
kubectl config use-context ${ENVIRONMENT}-cluster

# Restore from backup if needed
if [ "$3" = "--from-backup" ]; then
    BACKUP_DATE=$4
    kubectl apply -f /backups/kubernetes/$BACKUP_DATE/
fi

# Deploy application
helm upgrade --install interface-exception-collector ./helm \
  --namespace biopro-${ENVIRONMENT} \
  --values helm/values-${ENVIRONMENT}.yaml \
  --set image.tag=$VERSION \
  --wait --timeout=600s

# Verify deployment
kubectl rollout status deployment/interface-exception-collector \
  -n biopro-${ENVIRONMENT} --timeout=300s

# Run health checks
./scripts/health-check.sh $ENVIRONMENT

echo "Application recovery completed for $ENVIRONMENT"
```

## Kafka Data Recovery

### Kafka Backup Strategy

```bash
# Kafka topic backup using MirrorMaker 2.0
# Source cluster to backup cluster replication

# MirrorMaker 2.0 configuration
cat > mirror-maker-config.properties << EOF
clusters = source, backup
source.bootstrap.servers = source-kafka:9092
backup.bootstrap.servers = backup-kafka:9092

source->backup.enabled = true
source->backup.topics = OrderRejected,OrderCancelled,CollectionRejected,DistributionFailed,ValidationError

replication.factor = 3
checkpoints.topic.replication.factor = 3
heartbeats.topic.replication.factor = 3
offset-syncs.topic.replication.factor = 3

sync.topic.acls.enabled = false
emit.checkpoints.interval.seconds = 60
emit.heartbeats.interval.seconds = 30
EOF

# Start MirrorMaker 2.0
kafka-mirror-maker.sh --config mirror-maker-config.properties
```

### Kafka Topic Recovery

```bash
#!/bin/bash
# scripts/recover-kafka-topics.sh

KAFKA_CLUSTER=$1
BACKUP_DATE=$2

if [ -z "$KAFKA_CLUSTER" ] || [ -z "$BACKUP_DATE" ]; then
    echo "Usage: $0 <kafka-cluster> <backup-date>"
    exit 1
fi

echo "Recovering Kafka topics from backup..."

# List of topics to recover
TOPICS=(
    "OrderRejected"
    "OrderCancelled"
    "CollectionRejected"
    "DistributionFailed"
    "ValidationError"
    "ExceptionCaptured"
    "ExceptionResolved"
    "CriticalExceptionAlert"
)

# Recreate topics
for topic in "${TOPICS[@]}"; do
    echo "Recreating topic: $topic"
    
    kafka-topics.sh --bootstrap-server $KAFKA_CLUSTER \
      --create --topic $topic \
      --partitions 3 --replication-factor 2 \
      --config retention.ms=604800000  # 7 days
done

# Restore data from backup cluster using MirrorMaker
kafka-mirror-maker.sh --consumer.config consumer.properties \
  --producer.config producer.properties \
  --whitelist "OrderRejected|OrderCancelled|CollectionRejected|DistributionFailed|ValidationError"

echo "Kafka topic recovery completed"
```

### Consumer Group Recovery

```bash
#!/bin/bash
# scripts/recover-consumer-groups.sh

KAFKA_CLUSTER=$1

# Reset consumer group offsets to latest
kafka-consumer-groups.sh --bootstrap-server $KAFKA_CLUSTER \
  --group interface-exception-collector \
  --reset-offsets --to-latest --all-topics --execute

# Verify consumer group status
kafka-consumer-groups.sh --bootstrap-server $KAFKA_CLUSTER \
  --describe --group interface-exception-collector

echo "Consumer group recovery completed"
```

## Configuration Backup

### Git-based Configuration Backup

```bash
#!/bin/bash
# scripts/backup-configuration.sh

BACKUP_REPO="git@github.com:biopro/config-backup.git"
BACKUP_DIR="/tmp/config-backup"

# Clone backup repository
git clone $BACKUP_REPO $BACKUP_DIR

# Copy current configurations
cp -r helm/ $BACKUP_DIR/interface-exception-collector/helm/
cp -r k8s/ $BACKUP_DIR/interface-exception-collector/k8s/
cp -r src/main/resources/ $BACKUP_DIR/interface-exception-collector/config/

# Commit and push changes
cd $BACKUP_DIR
git add .
git commit -m "Configuration backup - $(date)"
git push origin main

# Cleanup
rm -rf $BACKUP_DIR

echo "Configuration backup completed"
```

### Environment-specific Backup

```bash
#!/bin/bash
# scripts/backup-environment-config.sh

ENVIRONMENT=$1
BACKUP_DATE=$(date +%Y%m%d_%H%M%S)

# Backup Kubernetes configurations
kubectl get configmaps -n biopro-${ENVIRONMENT} -o yaml > \
  /backups/config/${ENVIRONMENT}/configmaps_${BACKUP_DATE}.yaml

kubectl get secrets -n biopro-${ENVIRONMENT} -o yaml > \
  /backups/config/${ENVIRONMENT}/secrets_${BACKUP_DATE}.yaml

# Backup Helm values
cp helm/values-${ENVIRONMENT}.yaml \
  /backups/config/${ENVIRONMENT}/helm-values_${BACKUP_DATE}.yaml

# Upload to S3
aws s3 sync /backups/config/${ENVIRONMENT}/ \
  s3://biopro-backups/config/${ENVIRONMENT}/

echo "Environment configuration backup completed for $ENVIRONMENT"
```

## Multi-Region Failover

### Active-Passive Setup

```bash
# Primary region: us-east-1
# Secondary region: us-west-2

# Database replication setup
# Primary database with read replica in secondary region
aws rds create-db-instance-read-replica \
  --db-instance-identifier exception-collector-replica-west \
  --source-db-instance-identifier exception-collector-primary-east \
  --db-instance-class db.r5.xlarge

# Kafka cross-region replication
# MirrorMaker 2.0 for topic replication between regions
```

### Failover Procedure

```bash
#!/bin/bash
# scripts/failover-to-secondary.sh

echo "Initiating failover to secondary region..."

# 1. Stop traffic to primary region
kubectl patch service interface-exception-collector \
  -n biopro-prod \
  -p '{"spec":{"selector":{"failover":"true"}}}'

# 2. Promote read replica to primary
aws rds promote-read-replica \
  --db-instance-identifier exception-collector-replica-west

# 3. Update DNS to point to secondary region
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123456789 \
  --change-batch file://dns-failover.json

# 4. Deploy application in secondary region
kubectl config use-context secondary-cluster
helm upgrade --install interface-exception-collector ./helm \
  --namespace biopro-prod \
  --values helm/values-prod-west.yaml

# 5. Verify application health
./scripts/health-check.sh prod-west

echo "Failover to secondary region completed"
```

### Failback Procedure

```bash
#!/bin/bash
# scripts/failback-to-primary.sh

echo "Initiating failback to primary region..."

# 1. Ensure primary region is healthy
./scripts/health-check.sh prod-east

# 2. Sync data from secondary to primary
pg_dump -h secondary-db -U postgres exception_collector | \
  psql -h primary-db -U postgres exception_collector

# 3. Deploy application in primary region
kubectl config use-context primary-cluster
helm upgrade --install interface-exception-collector ./helm \
  --namespace biopro-prod \
  --values helm/values-prod.yaml

# 4. Update DNS to point back to primary
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123456789 \
  --change-batch file://dns-failback.json

# 5. Stop application in secondary region
kubectl config use-context secondary-cluster
kubectl scale deployment interface-exception-collector --replicas=0

echo "Failback to primary region completed"
```

## Recovery Testing

### Automated Recovery Testing

```bash
#!/bin/bash
# scripts/test-disaster-recovery.sh

echo "Starting disaster recovery test..."

# Test 1: Database backup and restore
echo "Testing database backup and restore..."
./scripts/backup-database.sh
./scripts/restore-database-test.sh

# Test 2: Application recovery
echo "Testing application recovery..."
kubectl delete deployment interface-exception-collector -n biopro-test
./scripts/recover-application.sh test latest

# Test 3: Configuration recovery
echo "Testing configuration recovery..."
kubectl delete configmap interface-exception-collector-config -n biopro-test
./scripts/restore-configuration.sh test

# Test 4: End-to-end functionality
echo "Testing end-to-end functionality..."
./scripts/e2e-test.sh test

echo "Disaster recovery test completed"
```

### Recovery Time Testing

```bash
#!/bin/bash
# scripts/measure-recovery-time.sh

START_TIME=$(date +%s)

echo "Starting recovery time measurement..."

# Simulate disaster
kubectl delete namespace biopro-test

# Measure recovery time
./scripts/full-recovery.sh test

END_TIME=$(date +%s)
RECOVERY_TIME=$((END_TIME - START_TIME))

echo "Recovery completed in $RECOVERY_TIME seconds"

# Check if RTO is met (4 hours = 14400 seconds)
if [ $RECOVERY_TIME -lt 14400 ]; then
    echo "✓ RTO objective met"
else
    echo "✗ RTO objective not met"
fi
```

## Emergency Procedures

### Emergency Contact List

```bash
# Emergency contacts and escalation procedures
# Level 1: On-call Engineer (+1-555-0129)
# Level 2: Engineering Manager (+1-555-0130)
# Level 3: CTO (+1-555-0131)

# Communication channels:
# Slack: #incident-response
# Email: incident-response@biopro.com
# Status page: https://status.biopro.com
```

### Emergency Response Checklist

```bash
# Disaster Recovery Emergency Checklist

# Immediate Response (0-15 minutes)
# [ ] Assess the scope of the disaster
# [ ] Notify stakeholders via incident response channel
# [ ] Activate disaster recovery team
# [ ] Update status page with incident information

# Short-term Response (15 minutes - 1 hour)
# [ ] Execute appropriate recovery procedure
# [ ] Monitor recovery progress
# [ ] Communicate updates to stakeholders
# [ ] Document actions taken

# Recovery Phase (1-4 hours)
# [ ] Complete system recovery
# [ ] Verify all services are operational
# [ ] Conduct post-recovery validation
# [ ] Update stakeholders on resolution

# Post-Incident (After recovery)
# [ ] Conduct post-mortem analysis
# [ ] Update disaster recovery procedures
# [ ] Schedule recovery testing
# [ ] Implement preventive measures
```

### Communication Templates

```bash
# Initial incident notification
SUBJECT: "CRITICAL: Interface Exception Collector Service Outage"
BODY: "
We are experiencing a critical outage with the Interface Exception Collector Service.

Impact: Exception processing is currently unavailable
Start Time: $(date)
Estimated Recovery: 4 hours
Status Page: https://status.biopro.com

We are actively working on recovery and will provide updates every 30 minutes.
"

# Recovery completion notification
SUBJECT: "RESOLVED: Interface Exception Collector Service Restored"
BODY: "
The Interface Exception Collector Service has been fully restored.

Resolution Time: $(date)
Root Cause: [To be determined in post-mortem]
Next Steps: Post-mortem scheduled for [date/time]

All services are now operational. Thank you for your patience.
"
```

## Post-Recovery Validation

### Comprehensive Validation Script

```bash
#!/bin/bash
# scripts/post-recovery-validation.sh

echo "Starting post-recovery validation..."

# 1. Health checks
echo "Checking application health..."
if ! curl -f http://localhost:8080/actuator/health; then
    echo "ERROR: Health check failed"
    exit 1
fi

# 2. Database connectivity and data integrity
echo "Validating database..."
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME << EOF
-- Check table existence
\dt

-- Verify data integrity
SELECT 
    COUNT(*) as total_exceptions,
    COUNT(DISTINCT transaction_id) as unique_transactions,
    MAX(created_at) as latest_exception,
    MIN(created_at) as earliest_exception
FROM interface_exceptions;

-- Check for data consistency
SELECT 
    ie.status,
    COUNT(*) as count
FROM interface_exceptions ie
GROUP BY ie.status
ORDER BY count DESC;
EOF

# 3. Kafka connectivity
echo "Validating Kafka connectivity..."
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list

# 4. API functionality
echo "Testing API endpoints..."
curl -H "Authorization: Bearer $TEST_TOKEN" \
  "http://localhost:8080/api/v1/exceptions?page=0&size=5"

# 5. Exception processing
echo "Testing exception processing..."
./scripts/test-exception-processing.sh

# 6. Monitoring and alerting
echo "Validating monitoring..."
curl http://localhost:8080/actuator/prometheus

echo "Post-recovery validation completed successfully"
```

### Performance Validation

```bash
#!/bin/bash
# scripts/validate-performance.sh

echo "Validating post-recovery performance..."

# API response time test
RESPONSE_TIME=$(curl -o /dev/null -s -w '%{time_total}' \
  http://localhost:8080/api/v1/exceptions)

if (( $(echo "$RESPONSE_TIME > 1.0" | bc -l) )); then
    echo "WARNING: High API response time: ${RESPONSE_TIME}s"
else
    echo "✓ API response time acceptable: ${RESPONSE_TIME}s"
fi

# Database query performance
DB_QUERY_TIME=$(psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER \
  -d $DATABASE_NAME -c "\timing on" \
  -c "SELECT COUNT(*) FROM interface_exceptions WHERE created_at > NOW() - INTERVAL '1 hour';" \
  2>&1 | grep "Time:" | awk '{print $2}')

echo "Database query time: $DB_QUERY_TIME"

# Memory usage check
MEMORY_USAGE=$(curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | \
  jq '.measurements[0].value')

echo "JVM memory usage: $(echo "scale=2; $MEMORY_USAGE / 1024 / 1024" | bc) MB"

echo "Performance validation completed"
```

## Contact Information

- **Disaster Recovery Team**: dr-team@biopro.com
- **Emergency Hotline**: +1-555-0129
- **Database Team**: db-team@biopro.com
- **Infrastructure Team**: infra-team@biopro.com

## Related Documentation

- [Service Lifecycle Management](service-lifecycle.md)
- [Database Troubleshooting](database-troubleshooting.md)
- [Monitoring Setup](monitoring-setup.md)
- [Deployment Guide](../DEPLOYMENT_GUIDE.md)