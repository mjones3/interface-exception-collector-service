# Security Incident Response Runbook

## Overview

This runbook provides procedures for responding to security incidents affecting the Interface Exception Collector Service, including detection, containment, eradication, and recovery procedures.

## Table of Contents

- [Incident Classification](#incident-classification)
- [Response Team](#response-team)
- [Detection and Analysis](#detection-and-analysis)
- [Containment Procedures](#containment-procedures)
- [Eradication and Recovery](#eradication-and-recovery)
- [Post-Incident Activities](#post-incident-activities)
- [Common Security Scenarios](#common-security-scenarios)
- [Forensic Procedures](#forensic-procedures)
- [Communication Guidelines](#communication-guidelines)

## Incident Classification

### Severity Levels

| Level | Description | Response Time | Examples |
|-------|-------------|---------------|----------|
| **Critical (P0)** | Active attack, data breach | 15 minutes | Unauthorized data access, active intrusion |
| **High (P1)** | Potential breach, system compromise | 1 hour | Malware detection, privilege escalation |
| **Medium (P2)** | Security policy violation | 4 hours | Failed authentication attempts, policy violations |
| **Low (P3)** | Security concern, no immediate threat | 24 hours | Vulnerability reports, security misconfigurations |

### Incident Types

- **Data Breach**: Unauthorized access to sensitive exception data
- **System Compromise**: Malware, unauthorized access to systems
- **Denial of Service**: Attacks affecting service availability
- **Insider Threat**: Malicious or negligent insider activities
- **Supply Chain**: Compromised dependencies or third-party services

## Response Team

### Core Team Members

- **Incident Commander**: Security team lead
- **Technical Lead**: Senior engineer familiar with the service
- **Communications Lead**: PR/Communications team member
- **Legal Counsel**: Legal team representative (for data breaches)

### Contact Information

```bash
# Emergency Security Contacts
Security Team Lead: +1-555-0132
Technical Lead: +1-555-0133
Communications Lead: +1-555-0134
Legal Counsel: +1-555-0135

# Communication Channels
Slack: #security-incidents
Email: security-incidents@biopro.com
Conference Bridge: security-bridge@biopro.com
```

## Detection and Analysis

### Security Monitoring

```bash
# Check for security alerts
kubectl logs -f deployment/interface-exception-collector | grep -i "security\|unauthorized\|breach"

# Review authentication logs
kubectl logs -f deployment/interface-exception-collector | grep -i "authentication\|login\|token"

# Check for unusual API activity
curl http://localhost:8080/actuator/metrics/http.server.requests | \
  jq '.measurements[] | select(.statistic=="COUNT" and .value > 1000)'

# Monitor failed authentication attempts
curl http://localhost:8080/actuator/metrics/authentication.failures
```

### Log Analysis

```bash
#!/bin/bash
# scripts/security-log-analysis.sh

TIMEFRAME=${1:-"1h"}  # Default to last hour

echo "Analyzing security logs for the last $TIMEFRAME..."

# Check for suspicious API calls
kubectl logs --since=$TIMEFRAME deployment/interface-exception-collector | \
  grep -E "(401|403|429)" | \
  awk '{print $1, $7, $9}' | \
  sort | uniq -c | sort -nr

# Look for unusual user agents
kubectl logs --since=$TIMEFRAME deployment/interface-exception-collector | \
  grep "User-Agent" | \
  awk -F'"' '{print $6}' | \
  sort | uniq -c | sort -nr

# Check for SQL injection attempts
kubectl logs --since=$TIMEFRAME deployment/interface-exception-collector | \
  grep -i -E "(union|select|drop|insert|update|delete)" | \
  grep -v "SELECT.*FROM interface_exceptions"

# Monitor for privilege escalation attempts
kubectl logs --since=$TIMEFRAME deployment/interface-exception-collector | \
  grep -i -E "(sudo|admin|root|privilege)"

echo "Security log analysis completed"
```

### Threat Detection

```bash
#!/bin/bash
# scripts/threat-detection.sh

echo "Running threat detection checks..."

# Check for known malicious IPs
MALICIOUS_IPS=$(curl -s https://reputation-api.example.com/malicious-ips)
kubectl logs deployment/interface-exception-collector | \
  grep -f <(echo "$MALICIOUS_IPS") || echo "No malicious IPs detected"

# Monitor for brute force attacks
kubectl logs deployment/interface-exception-collector | \
  grep "401" | \
  awk '{print $1}' | \
  sort | uniq -c | \
  awk '$1 > 10 {print "Potential brute force from " $2 ": " $1 " attempts"}'

# Check for data exfiltration patterns
kubectl logs deployment/interface-exception-collector | \
  grep -E "GET.*exceptions.*size=[5-9][0-9]|size=100" | \
  awk '{print $1}' | \
  sort | uniq -c | \
  awk '$1 > 5 {print "Potential data exfiltration from " $2 ": " $1 " large requests"}'

echo "Threat detection completed"
```

## Containment Procedures

### Immediate Containment

```bash
#!/bin/bash
# scripts/immediate-containment.sh

INCIDENT_TYPE=$1

echo "Initiating immediate containment for $INCIDENT_TYPE..."

case $INCIDENT_TYPE in
  "data-breach")
    # Block suspicious IPs
    kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: emergency-block
  namespace: biopro-prod
spec:
  podSelector:
    matchLabels:
      app: interface-exception-collector
  policyTypes:
  - Ingress
  ingress:
  - from: []  # Block all ingress traffic temporarily
EOF
    ;;
    
  "system-compromise")
    # Isolate affected pods
    kubectl label pods -l app=interface-exception-collector quarantine=true
    kubectl patch deployment interface-exception-collector \
      -p '{"spec":{"template":{"spec":{"nodeSelector":{"quarantine":"true"}}}}}'
    ;;
    
  "dos-attack")
    # Enable rate limiting
    kubectl patch configmap interface-exception-collector-config \
      -p '{"data":{"RATE_LIMIT_ENABLED":"true","RATE_LIMIT_REQUESTS":"10"}}'
    kubectl rollout restart deployment/interface-exception-collector
    ;;
    
  *)
    echo "Unknown incident type: $INCIDENT_TYPE"
    ;;
esac

echo "Immediate containment completed"
```

### Access Control Lockdown

```bash
#!/bin/bash
# scripts/access-lockdown.sh

echo "Implementing access control lockdown..."

# Revoke all API tokens
kubectl delete secret interface-exception-collector-tokens

# Disable service account
kubectl patch serviceaccount interface-exception-collector \
  -p '{"automountServiceAccountToken":false}'

# Enable emergency authentication mode
kubectl patch configmap interface-exception-collector-config \
  -p '{"data":{"EMERGENCY_AUTH_MODE":"true"}}'

# Restart application with emergency settings
kubectl rollout restart deployment/interface-exception-collector

echo "Access control lockdown completed"
```

### Network Isolation

```bash
#!/bin/bash
# scripts/network-isolation.sh

echo "Implementing network isolation..."

# Create restrictive network policy
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: security-isolation
  namespace: biopro-prod
spec:
  podSelector:
    matchLabels:
      app: interface-exception-collector
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: monitoring
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: database
    ports:
    - protocol: TCP
      port: 5432
EOF

echo "Network isolation implemented"
```

## Eradication and Recovery

### Malware Removal

```bash
#!/bin/bash
# scripts/malware-removal.sh

echo "Starting malware removal process..."

# Stop affected services
kubectl scale deployment interface-exception-collector --replicas=0

# Scan container images
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image interface-exception-collector:latest

# Rebuild clean images
docker build --no-cache -t interface-exception-collector:clean .
docker tag interface-exception-collector:clean \
  ${REGISTRY}/interface-exception-collector:clean

# Deploy clean image
kubectl set image deployment/interface-exception-collector \
  interface-exception-collector=${REGISTRY}/interface-exception-collector:clean

# Scale back up
kubectl scale deployment interface-exception-collector --replicas=3

echo "Malware removal completed"
```

### System Hardening

```bash
#!/bin/bash
# scripts/system-hardening.sh

echo "Implementing system hardening measures..."

# Update security configurations
kubectl apply -f - <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: interface-exception-collector-security-config
data:
  SECURITY_HEADERS_ENABLED: "true"
  CSRF_PROTECTION_ENABLED: "true"
  RATE_LIMITING_ENABLED: "true"
  AUDIT_LOGGING_ENABLED: "true"
  ENCRYPTION_AT_REST_ENABLED: "true"
EOF

# Apply pod security policy
kubectl apply -f - <<EOF
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: interface-exception-collector-psp
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
EOF

# Enable network policies
kubectl apply -f k8s/network-policies.yaml

echo "System hardening completed"
```

### Data Integrity Verification

```bash
#!/bin/bash
# scripts/verify-data-integrity.sh

echo "Verifying data integrity..."

# Check database for unauthorized changes
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME << EOF
-- Check for unusual data patterns
SELECT 
    DATE(created_at) as date,
    COUNT(*) as exception_count,
    COUNT(DISTINCT customer_id) as unique_customers
FROM interface_exceptions 
WHERE created_at >= NOW() - INTERVAL '7 days'
GROUP BY DATE(created_at)
ORDER BY date DESC;

-- Look for suspicious modifications
SELECT 
    transaction_id,
    status,
    updated_at,
    acknowledged_by
FROM interface_exceptions 
WHERE updated_at > created_at + INTERVAL '1 day'
ORDER BY updated_at DESC
LIMIT 20;

-- Verify referential integrity
SELECT 
    COUNT(*) as orphaned_retries
FROM retry_attempts ra
LEFT JOIN interface_exceptions ie ON ra.exception_id = ie.id
WHERE ie.id IS NULL;
EOF

echo "Data integrity verification completed"
```

## Post-Incident Activities

### Evidence Collection

```bash
#!/bin/bash
# scripts/collect-evidence.sh

INCIDENT_ID=$1
EVIDENCE_DIR="/tmp/evidence-$INCIDENT_ID"

mkdir -p $EVIDENCE_DIR

echo "Collecting evidence for incident $INCIDENT_ID..."

# Collect logs
kubectl logs deployment/interface-exception-collector --since=24h > \
  $EVIDENCE_DIR/application-logs.txt

# Collect system metrics
curl http://localhost:8080/actuator/metrics > \
  $EVIDENCE_DIR/metrics.json

# Collect network traffic (if available)
kubectl exec -it deployment/interface-exception-collector -- \
  tcpdump -w $EVIDENCE_DIR/network-traffic.pcap -c 1000

# Collect configuration
kubectl get deployment interface-exception-collector -o yaml > \
  $EVIDENCE_DIR/deployment-config.yaml

# Create evidence archive
tar -czf evidence-$INCIDENT_ID.tar.gz -C /tmp evidence-$INCIDENT_ID/

# Secure evidence
gpg --encrypt --recipient security@biopro.com evidence-$INCIDENT_ID.tar.gz

echo "Evidence collection completed: evidence-$INCIDENT_ID.tar.gz.gpg"
```

### Incident Report Template

```markdown
# Security Incident Report

**Incident ID**: SEC-2025-001
**Date**: 2025-08-05
**Severity**: Critical
**Status**: Resolved

## Executive Summary
Brief description of the incident and its impact.

## Timeline
- **Detection**: 2025-08-05 10:30 UTC
- **Containment**: 2025-08-05 10:45 UTC
- **Eradication**: 2025-08-05 12:00 UTC
- **Recovery**: 2025-08-05 14:00 UTC

## Impact Assessment
- **Systems Affected**: Interface Exception Collector Service
- **Data Compromised**: None confirmed
- **Customers Affected**: 0
- **Financial Impact**: $0

## Root Cause Analysis
Detailed analysis of how the incident occurred.

## Response Actions
1. Immediate containment measures
2. Investigation and analysis
3. Eradication procedures
4. Recovery and validation

## Lessons Learned
- What went well
- What could be improved
- Preventive measures

## Recommendations
1. Implement additional monitoring
2. Update security procedures
3. Conduct security training
```

## Common Security Scenarios

### Scenario 1: Unauthorized API Access

```bash
#!/bin/bash
# Response to unauthorized API access

echo "Responding to unauthorized API access..."

# Identify suspicious requests
kubectl logs deployment/interface-exception-collector | \
  grep -E "(401|403)" | \
  awk '{print $1}' | \
  sort | uniq -c | \
  sort -nr > suspicious-ips.txt

# Block suspicious IPs
while read count ip; do
  if [ $count -gt 50 ]; then
    kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: block-$ip
spec:
  podSelector:
    matchLabels:
      app: interface-exception-collector
  policyTypes:
  - Ingress
  ingress:
  - from:
    - ipBlock:
        cidr: $ip/32
    except:
    - ipBlock:
        cidr: $ip/32
EOF
  fi
done < suspicious-ips.txt

# Rotate API keys
kubectl delete secret api-keys
kubectl create secret generic api-keys --from-literal=key=$(openssl rand -hex 32)

echo "Unauthorized access response completed"
```

### Scenario 2: Data Exfiltration Attempt

```bash
#!/bin/bash
# Response to data exfiltration attempt

echo "Responding to data exfiltration attempt..."

# Identify large data requests
kubectl logs deployment/interface-exception-collector | \
  grep -E "GET.*exceptions.*size=[5-9][0-9]|size=100" | \
  awk '{print $1, $7}' | \
  sort | uniq -c | \
  sort -nr > large-requests.txt

# Implement emergency data access controls
kubectl patch configmap interface-exception-collector-config \
  -p '{"data":{"MAX_PAGE_SIZE":"10","AUDIT_ALL_REQUESTS":"true"}}'

# Enable enhanced logging
kubectl patch configmap interface-exception-collector-config \
  -p '{"data":{"LOG_LEVEL":"DEBUG","LOG_REQUEST_BODIES":"true"}}'

# Restart application
kubectl rollout restart deployment/interface-exception-collector

echo "Data exfiltration response completed"
```

### Scenario 3: Insider Threat

```bash
#!/bin/bash
# Response to insider threat

echo "Responding to insider threat..."

# Disable user access
kubectl patch rolebinding interface-exception-collector-binding \
  -p '{"subjects":[]}'

# Enable enhanced audit logging
kubectl patch configmap interface-exception-collector-config \
  -p '{"data":{"AUDIT_USER_ACTIONS":"true","LOG_ALL_DATABASE_QUERIES":"true"}}'

# Require multi-factor authentication
kubectl patch configmap interface-exception-collector-config \
  -p '{"data":{"REQUIRE_MFA":"true"}}'

# Notify HR and Legal
curl -X POST https://hr-api.biopro.com/security-incident \
  -H "Content-Type: application/json" \
  -d '{"type":"insider_threat","severity":"high","timestamp":"'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"}'

echo "Insider threat response completed"
```

## Forensic Procedures

### Digital Forensics

```bash
#!/bin/bash
# scripts/digital-forensics.sh

INCIDENT_ID=$1
FORENSICS_DIR="/forensics/$INCIDENT_ID"

mkdir -p $FORENSICS_DIR

echo "Starting digital forensics for incident $INCIDENT_ID..."

# Create memory dump
kubectl exec -it deployment/interface-exception-collector -- \
  gcore -o /tmp/memory-dump $(pgrep java)

# Copy memory dump
kubectl cp interface-exception-collector-pod:/tmp/memory-dump.* \
  $FORENSICS_DIR/

# Collect file system artifacts
kubectl exec -it deployment/interface-exception-collector -- \
  find /app -type f -newer /tmp/incident-start-time > \
  $FORENSICS_DIR/modified-files.txt

# Network connection analysis
kubectl exec -it deployment/interface-exception-collector -- \
  netstat -tulpn > $FORENSICS_DIR/network-connections.txt

# Process analysis
kubectl exec -it deployment/interface-exception-collector -- \
  ps aux > $FORENSICS_DIR/process-list.txt

# Create forensic image
dd if=/dev/disk of=$FORENSICS_DIR/disk-image.dd bs=1M

echo "Digital forensics completed"
```

### Chain of Custody

```bash
#!/bin/bash
# scripts/chain-of-custody.sh

EVIDENCE_FILE=$1
CUSTODIAN=$2

echo "Recording chain of custody for $EVIDENCE_FILE..."

# Calculate hash
HASH=$(sha256sum $EVIDENCE_FILE | awk '{print $1}')

# Create custody record
cat > custody-record-$(basename $EVIDENCE_FILE).txt << EOF
CHAIN OF CUSTODY RECORD

Evidence File: $EVIDENCE_FILE
SHA256 Hash: $HASH
Collection Date: $(date)
Collected By: $CUSTODIAN
Collection Method: Automated script
Storage Location: Secure evidence locker

Custody Transfer Log:
$(date) - Collected by $CUSTODIAN
EOF

echo "Chain of custody recorded"
```

## Communication Guidelines

### Internal Communication

```bash
# Incident notification template
SUBJECT: "SECURITY INCIDENT - $SEVERITY - $INCIDENT_TYPE"
BODY: "
Security incident detected in Interface Exception Collector Service

Incident ID: $INCIDENT_ID
Severity: $SEVERITY
Type: $INCIDENT_TYPE
Detection Time: $(date)
Status: Under Investigation

Initial Response:
- Incident response team activated
- Containment measures implemented
- Investigation in progress

Next Update: In 30 minutes
"
```

### External Communication

```bash
# Customer notification template (if required)
SUBJECT: "Security Notice - Interface Exception Collector Service"
BODY: "
We are writing to inform you of a security incident that may have affected your data.

What Happened:
[Brief description of the incident]

What Information Was Involved:
[Description of potentially affected data]

What We Are Doing:
[Description of response actions]

What You Can Do:
[Recommended customer actions]

For More Information:
Contact our security team at security@biopro.com
"
```

## Contact Information

- **Security Team**: security-team@biopro.com
- **Emergency Security Hotline**: +1-555-0132
- **Legal Team**: legal@biopro.com
- **PR Team**: pr@biopro.com

## Related Documentation

- [Service Lifecycle Management](service-lifecycle.md)
- [Monitoring Setup](monitoring-setup.md)
- [Disaster Recovery](disaster-recovery.md)