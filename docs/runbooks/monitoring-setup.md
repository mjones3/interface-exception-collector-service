# Monitoring and Alerting Setup Guide

## Overview

This guide covers the complete setup and configuration of monitoring and alerting for the Interface Exception Collector Service using Prometheus, Grafana, and AlertManager.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Prometheus Setup](#prometheus-setup)
- [Grafana Configuration](#grafana-configuration)
- [AlertManager Setup](#alertmanager-setup)
- [Application Metrics](#application-metrics)
- [Custom Dashboards](#custom-dashboards)
- [Alert Rules](#alert-rules)
- [Log Aggregation](#log-aggregation)
- [Health Checks](#health-checks)
- [Troubleshooting](#troubleshooting)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Monitoring Stack                             │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   Prometheus    │    Grafana      │      AlertManager          │
│   (Metrics)     │  (Dashboards)   │   (Notifications)           │
└─────────┬───────┴─────────┬───────┴─────────────┬───────────────┘
          │                 │                     │
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│              Interface Exception Collector                      │
├─────────────────┬─────────────────┬─────────────────────────────┤
│  Application    │   JVM Metrics   │    Custom Metrics           │
│  Metrics        │                 │                             │
└─────────────────┴─────────────────┴─────────────────────────────┘
          │                 │                     │
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Data Sources                                 │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   PostgreSQL    │     Kafka       │        Redis                │
│   Metrics       │    Metrics      │       Metrics               │
└─────────────────┴─────────────────┴─────────────────────────────┘
```

## Prometheus Setup

### Installation

```bash
# Add Prometheus Helm repository
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Prometheus
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --values prometheus-values.yaml
```

### Prometheus Configuration

```yaml
# prometheus-values.yaml
prometheus:
  prometheusSpec:
    retention: 30d
    storageSpec:
      volumeClaimTemplate:
        spec:
          storageClassName: fast-ssd
          accessModes: ["ReadWriteOnce"]
          resources:
            requests:
              storage: 100Gi
    
    additionalScrapeConfigs:
      - job_name: 'interface-exception-collector'
        kubernetes_sd_configs:
          - role: endpoints
            namespaces:
              names:
                - biopro-dev
                - biopro-staging
                - biopro-prod
        relabel_configs:
          - source_labels: [__meta_kubernetes_service_name]
            action: keep
            regex: interface-exception-collector
          - source_labels: [__meta_kubernetes_endpoint_port_name]
            action: keep
            regex: http-actuator

grafana:
  adminPassword: ${GRAFANA_ADMIN_PASSWORD}
  persistence:
    enabled: true
    size: 10Gi
  
  dashboardProviders:
    dashboardproviders.yaml:
      apiVersion: 1
      providers:
      - name: 'default'
        orgId: 1
        folder: ''
        type: file
        disableDeletion: false
        editable: true
        options:
          path: /var/lib/grafana/dashboards/default

alertmanager:
  config:
    global:
      smtp_smarthost: 'smtp.biopro.com:587'
      smtp_from: 'alerts@biopro.com'
    
    route:
      group_by: ['alertname', 'cluster', 'service']
      group_wait: 10s
      group_interval: 10s
      repeat_interval: 1h
      receiver: 'web.hook'
      routes:
      - match:
          severity: critical
        receiver: 'critical-alerts'
      - match:
          severity: warning
        receiver: 'warning-alerts'
    
    receivers:
    - name: 'web.hook'
      webhook_configs:
      - url: 'http://alertmanager-webhook:5001/'
    
    - name: 'critical-alerts'
      email_configs:
      - to: 'oncall@biopro.com'
        subject: 'CRITICAL: {{ .GroupLabels.alertname }}'
        body: |
          {{ range .Alerts }}
          Alert: {{ .Annotations.summary }}
          Description: {{ .Annotations.description }}
          {{ end }}
      slack_configs:
      - api_url: '${SLACK_WEBHOOK_URL}'
        channel: '#critical-alerts'
        title: 'CRITICAL Alert'
        text: '{{ range .Alerts }}{{ .Annotations.summary }}{{ end }}'
    
    - name: 'warning-alerts'
      email_configs:
      - to: 'team@biopro.com'
        subject: 'WARNING: {{ .GroupLabels.alertname }}'
```

### ServiceMonitor Configuration

```yaml
# k8s/servicemonitor.yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: interface-exception-collector
  namespace: biopro-prod
  labels:
    app: interface-exception-collector
spec:
  selector:
    matchLabels:
      app: interface-exception-collector
  endpoints:
  - port: http-actuator
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
```

## Grafana Configuration

### Dashboard Import

```bash
# Import pre-built dashboards
kubectl create configmap grafana-dashboards \
  --from-file=monitoring/grafana/dashboards/ \
  --namespace monitoring

# Apply dashboard ConfigMap
kubectl apply -f monitoring/grafana/dashboard-configmap.yaml
```

### Custom Dashboard JSON

```json
{
  "dashboard": {
    "id": null,
    "title": "Interface Exception Collector",
    "tags": ["biopro", "exceptions"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "Exception Processing Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(exception_processing_total[5m])",
            "legendFormat": "{{instance}}"
          }
        ],
        "yAxes": [
          {
            "label": "Exceptions/sec",
            "min": 0
          }
        ]
      },
      {
        "id": 2,
        "title": "API Response Times",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          },
          {
            "expr": "histogram_quantile(0.50, rate(http_server_requests_seconds_bucket[5m]))",
            "legendFormat": "50th percentile"
          }
        ]
      },
      {
        "id": 3,
        "title": "Database Connection Pool",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active",
            "legendFormat": "Active Connections"
          },
          {
            "expr": "hikaricp_connections_idle",
            "legendFormat": "Idle Connections"
          }
        ]
      },
      {
        "id": 4,
        "title": "JVM Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area=\"heap\"}",
            "legendFormat": "Heap Used"
          },
          {
            "expr": "jvm_memory_max_bytes{area=\"heap\"}",
            "legendFormat": "Heap Max"
          }
        ]
      },
      {
        "id": 5,
        "title": "Kafka Consumer Lag",
        "type": "graph",
        "targets": [
          {
            "expr": "kafka_consumer_lag_sum",
            "legendFormat": "Consumer Lag"
          }
        ]
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "30s"
  }
}
```

## AlertManager Setup

### Alert Rules Configuration

```yaml
# k8s/prometheus-rules.yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: interface-exception-collector-rules
  namespace: biopro-prod
  labels:
    app: interface-exception-collector
spec:
  groups:
  - name: interface-exception-collector.rules
    rules:
    
    # High Error Rate
    - alert: HighErrorRate
      expr: rate(http_server_requests_total{status=~"5.."}[5m]) > 0.1
      for: 2m
      labels:
        severity: critical
        service: interface-exception-collector
      annotations:
        summary: "High error rate detected"
        description: "Error rate is {{ $value }} errors per second"
    
    # High Response Time
    - alert: HighResponseTime
      expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
      for: 5m
      labels:
        severity: warning
        service: interface-exception-collector
      annotations:
        summary: "High API response time"
        description: "95th percentile response time is {{ $value }}s"
    
    # Database Connection Pool Exhausted
    - alert: DatabaseConnectionPoolExhausted
      expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
      for: 2m
      labels:
        severity: critical
        service: interface-exception-collector
      annotations:
        summary: "Database connection pool nearly exhausted"
        description: "Connection pool usage is {{ $value | humanizePercentage }}"
    
    # High Memory Usage
    - alert: HighMemoryUsage
      expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.8
      for: 5m
      labels:
        severity: warning
        service: interface-exception-collector
      annotations:
        summary: "High JVM memory usage"
        description: "JVM heap usage is {{ $value | humanizePercentage }}"
    
    # Kafka Consumer Lag
    - alert: HighKafkaConsumerLag
      expr: kafka_consumer_lag_sum > 1000
      for: 5m
      labels:
        severity: warning
        service: interface-exception-collector
      annotations:
        summary: "High Kafka consumer lag"
        description: "Consumer lag is {{ $value }} messages"
    
    # Service Down
    - alert: ServiceDown
      expr: up{job="interface-exception-collector"} == 0
      for: 1m
      labels:
        severity: critical
        service: interface-exception-collector
      annotations:
        summary: "Service is down"
        description: "Interface Exception Collector service is not responding"
    
    # Low Exception Processing Rate
    - alert: LowExceptionProcessingRate
      expr: rate(exception_processing_total[10m]) < 0.1
      for: 10m
      labels:
        severity: warning
        service: interface-exception-collector
      annotations:
        summary: "Low exception processing rate"
        description: "Exception processing rate is {{ $value }} per second"
    
    # Critical Exception Volume
    - alert: CriticalExceptionVolume
      expr: increase(exception_processing_total{severity="CRITICAL"}[5m]) > 10
      for: 0m
      labels:
        severity: critical
        service: interface-exception-collector
      annotations:
        summary: "High volume of critical exceptions"
        description: "{{ $value }} critical exceptions in the last 5 minutes"
```

### Notification Channels

```yaml
# Slack notification configuration
apiVersion: v1
kind: Secret
metadata:
  name: alertmanager-slack-webhook
  namespace: monitoring
type: Opaque
stringData:
  webhook-url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"

---
# Email notification configuration
apiVersion: v1
kind: Secret
metadata:
  name: alertmanager-email-config
  namespace: monitoring
type: Opaque
stringData:
  smtp-username: "alerts@biopro.com"
  smtp-password: "your-smtp-password"
```

## Application Metrics

### Custom Metrics Configuration

```java
// Custom metrics in the application
@Component
public class ExceptionCollectorMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter exceptionProcessingCounter;
    private final Timer exceptionProcessingTimer;
    private final Gauge activeExceptionsGauge;
    
    public ExceptionCollectorMetrics(MeterRegistry meterRegistry, 
                                   ExceptionQueryService exceptionQueryService) {
        this.meterRegistry = meterRegistry;
        
        this.exceptionProcessingCounter = Counter.builder("exception.processing.total")
            .description("Total number of exceptions processed")
            .tag("service", "interface-exception-collector")
            .register(meterRegistry);
        
        this.exceptionProcessingTimer = Timer.builder("exception.processing.duration")
            .description("Time taken to process exceptions")
            .register(meterRegistry);
        
        this.activeExceptionsGauge = Gauge.builder("exception.active.count")
            .description("Number of active exceptions")
            .register(meterRegistry, this, ExceptionCollectorMetrics::getActiveExceptionCount);
    }
    
    public void recordExceptionProcessed(String interfaceType, String severity) {
        exceptionProcessingCounter.increment(
            Tags.of(
                Tag.of("interface_type", interfaceType),
                Tag.of("severity", severity)
            )
        );
    }
    
    public void recordProcessingTime(Duration duration) {
        exceptionProcessingTimer.record(duration);
    }
    
    private double getActiveExceptionCount() {
        // Implementation to get active exception count
        return exceptionQueryService.countActiveExceptions();
    }
}
```

### Metrics Endpoint Configuration

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: interface-exception-collector
      environment: ${SPRING_PROFILES_ACTIVE}
```

## Custom Dashboards

### Exception Processing Dashboard

```json
{
  "dashboard": {
    "title": "Exception Processing Overview",
    "panels": [
      {
        "title": "Exceptions by Interface Type",
        "type": "piechart",
        "targets": [
          {
            "expr": "sum by (interface_type) (increase(exception_processing_total[1h]))",
            "legendFormat": "{{interface_type}}"
          }
        ]
      },
      {
        "title": "Exceptions by Severity",
        "type": "bargraph",
        "targets": [
          {
            "expr": "sum by (severity) (increase(exception_processing_total[1h]))",
            "legendFormat": "{{severity}}"
          }
        ]
      },
      {
        "title": "Exception Processing Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(exception_processing_total[5m])",
            "legendFormat": "Processing Rate"
          }
        ]
      },
      {
        "title": "Active Exceptions",
        "type": "singlestat",
        "targets": [
          {
            "expr": "exception_active_count",
            "legendFormat": "Active"
          }
        ]
      }
    ]
  }
}
```

### Infrastructure Dashboard

```json
{
  "dashboard": {
    "title": "Infrastructure Metrics",
    "panels": [
      {
        "title": "CPU Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(process_cpu_seconds_total[5m]) * 100",
            "legendFormat": "CPU Usage %"
          }
        ]
      },
      {
        "title": "Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area=\"heap\"} / 1024 / 1024",
            "legendFormat": "Heap Memory (MB)"
          }
        ]
      },
      {
        "title": "Database Connections",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active",
            "legendFormat": "Active"
          },
          {
            "expr": "hikaricp_connections_idle",
            "legendFormat": "Idle"
          }
        ]
      },
      {
        "title": "Kafka Consumer Lag",
        "type": "graph",
        "targets": [
          {
            "expr": "kafka_consumer_lag_sum",
            "legendFormat": "Consumer Lag"
          }
        ]
      }
    ]
  }
}
```

## Alert Rules

### Business Logic Alerts

```yaml
# Business-specific alert rules
groups:
- name: business-alerts
  rules:
  
  # Critical Exception Spike
  - alert: CriticalExceptionSpike
    expr: increase(exception_processing_total{severity="CRITICAL"}[5m]) > 5
    for: 0m
    labels:
      severity: critical
      team: operations
    annotations:
      summary: "Spike in critical exceptions detected"
      description: "{{ $value }} critical exceptions in 5 minutes"
      runbook_url: "https://docs.biopro.com/runbooks/critical-exceptions"
  
  # Exception Processing Stopped
  - alert: ExceptionProcessingStopped
    expr: increase(exception_processing_total[10m]) == 0
    for: 10m
    labels:
      severity: critical
      team: development
    annotations:
      summary: "Exception processing has stopped"
      description: "No exceptions processed in the last 10 minutes"
  
  # High Retry Failure Rate
  - alert: HighRetryFailureRate
    expr: rate(retry_attempts_total{status="FAILED"}[5m]) / rate(retry_attempts_total[5m]) > 0.5
    for: 5m
    labels:
      severity: warning
      team: operations
    annotations:
      summary: "High retry failure rate"
      description: "{{ $value | humanizePercentage }} of retries are failing"
```

### Infrastructure Alerts

```yaml
groups:
- name: infrastructure-alerts
  rules:
  
  # Pod Restart Loop
  - alert: PodRestartLoop
    expr: increase(kube_pod_container_status_restarts_total[1h]) > 3
    for: 0m
    labels:
      severity: warning
      team: infrastructure
    annotations:
      summary: "Pod is restarting frequently"
      description: "Pod {{ $labels.pod }} has restarted {{ $value }} times in 1 hour"
  
  # Persistent Volume Usage
  - alert: HighPVUsage
    expr: kubelet_volume_stats_used_bytes / kubelet_volume_stats_capacity_bytes > 0.8
    for: 5m
    labels:
      severity: warning
      team: infrastructure
    annotations:
      summary: "High persistent volume usage"
      description: "PV usage is {{ $value | humanizePercentage }}"
```

## Log Aggregation

### ELK Stack Configuration

```yaml
# elasticsearch-values.yaml
elasticsearch:
  replicas: 3
  minimumMasterNodes: 2
  resources:
    requests:
      cpu: 1000m
      memory: 2Gi
    limits:
      cpu: 2000m
      memory: 4Gi
  volumeClaimTemplate:
    accessModes: ["ReadWriteOnce"]
    storageClassName: fast-ssd
    resources:
      requests:
        storage: 100Gi

# logstash-values.yaml
logstash:
  config:
    logstash.yml: |
      http.host: "0.0.0.0"
      path.config: /usr/share/logstash/pipeline
  pipeline:
    logstash.conf: |
      input {
        beats {
          port => 5044
        }
      }
      filter {
        if [fields][service] == "interface-exception-collector" {
          json {
            source => "message"
          }
          date {
            match => [ "timestamp", "ISO8601" ]
          }
        }
      }
      output {
        elasticsearch {
          hosts => ["elasticsearch:9200"]
          index => "interface-exception-collector-%{+YYYY.MM.dd}"
        }
      }

# kibana-values.yaml
kibana:
  resources:
    requests:
      cpu: 500m
      memory: 1Gi
    limits:
      cpu: 1000m
      memory: 2Gi
```

### Filebeat Configuration

```yaml
# filebeat-configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: filebeat-config
data:
  filebeat.yml: |
    filebeat.inputs:
    - type: container
      paths:
        - /var/log/containers/*interface-exception-collector*.log
      processors:
      - add_kubernetes_metadata:
          host: ${NODE_NAME}
          matchers:
          - logs_path:
              logs_path: "/var/log/containers/"
      fields:
        service: interface-exception-collector
      fields_under_root: true
    
    output.logstash:
      hosts: ["logstash:5044"]
    
    logging.level: info
```

## Health Checks

### Kubernetes Health Probes

```yaml
# Deployment health check configuration
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: interface-exception-collector
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        
        startupProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 30
```

### Custom Health Indicators

```java
// Custom health indicators
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connected")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.getMessage())
                .build();
        }
        return Health.down().build();
    }
}

@Component
public class KafkaHealthIndicator implements HealthIndicator {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Override
    public Health health() {
        try {
            // Test Kafka connectivity
            ListenableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send("health-check", "ping");
            future.get(5, TimeUnit.SECONDS);
            
            return Health.up()
                .withDetail("kafka", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("kafka", "Connection failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## Troubleshooting

### Common Monitoring Issues

1. **Metrics Not Appearing**:
   ```bash
   # Check ServiceMonitor
   kubectl get servicemonitor -n biopro-prod
   
   # Verify Prometheus targets
   kubectl port-forward svc/prometheus-operated 9090:9090 -n monitoring
   # Open http://localhost:9090/targets
   
   # Check application metrics endpoint
   curl http://localhost:8080/actuator/prometheus
   ```

2. **Alerts Not Firing**:
   ```bash
   # Check PrometheusRule
   kubectl get prometheusrules -n biopro-prod
   
   # Verify alert rules in Prometheus UI
   # Open http://localhost:9090/alerts
   
   # Check AlertManager configuration
   kubectl get secret alertmanager-prometheus-kube-prometheus-alertmanager -o yaml
   ```

3. **Dashboard Not Loading**:
   ```bash
   # Check Grafana pod logs
   kubectl logs -f deployment/prometheus-grafana -n monitoring
   
   # Verify dashboard ConfigMap
   kubectl get configmap grafana-dashboards -n monitoring
   
   # Check Grafana data source configuration
   kubectl exec -it deployment/prometheus-grafana -n monitoring -- \
     cat /etc/grafana/provisioning/datasources/datasources.yaml
   ```

### Monitoring Health Script

```bash
#!/bin/bash
# monitoring-health-check.sh

echo "Checking monitoring stack health..."

# Check Prometheus
if curl -f http://prometheus:9090/-/healthy; then
    echo "✓ Prometheus is healthy"
else
    echo "✗ Prometheus is unhealthy"
fi

# Check Grafana
if curl -f http://grafana:3000/api/health; then
    echo "✓ Grafana is healthy"
else
    echo "✗ Grafana is unhealthy"
fi

# Check AlertManager
if curl -f http://alertmanager:9093/-/healthy; then
    echo "✓ AlertManager is healthy"
else
    echo "✗ AlertManager is unhealthy"
fi

# Check application metrics
if curl -f http://interface-exception-collector:8080/actuator/prometheus; then
    echo "✓ Application metrics are available"
else
    echo "✗ Application metrics are unavailable"
fi
```

## Contact Information

- **Monitoring Team**: monitoring-team@biopro.com
- **On-call Engineer**: +1-555-0128
- **DevOps Team**: devops-team@biopro.com

## Related Documentation

- [Performance Tuning](performance-tuning.md)
- [Service Lifecycle Management](service-lifecycle.md)
- [Disaster Recovery](disaster-recovery.md)