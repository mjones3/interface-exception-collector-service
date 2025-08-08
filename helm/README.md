# Interface Exception Collector Helm Chart

This Helm chart deploys the Interface Exception Collector Service on a Kubernetes cluster using the Helm package manager.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- PostgreSQL 12+ (if not using the bundled PostgreSQL)
- Redis 6+ (if not using the bundled Redis)
- Apache Kafka 2.8+ (external dependency)

## Installing the Chart

To install the chart with the release name `exception-collector`:

```bash
# Add the repository (if using a Helm repository)
helm repo add arcone https://charts.arcone.com
helm repo update

# Install with default values
helm install exception-collector arcone/interface-exception-collector

# Install with custom values file
helm install exception-collector arcone/interface-exception-collector -f values-prod.yaml

# Install from local chart
helm install exception-collector ./helm
```

## Uninstalling the Chart

To uninstall/delete the `exception-collector` deployment:

```bash
helm uninstall exception-collector
```

## Configuration

The following table lists the configurable parameters of the Interface Exception Collector chart and their default values.

### Application Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `2` |
| `image.repository` | Image repository | `interface-exception-collector` |
| `image.tag` | Image tag | `latest` |
| `image.pullPolicy` | Image pull policy | `IfNotPresent` |
| `app.profile` | Spring profile | `production` |
| `app.logLevel` | Log level | `INFO` |

### Database Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `postgresql.enabled` | Enable PostgreSQL dependency | `true` |
| `postgresql.auth.database` | Database name | `exception_collector` |
| `postgresql.auth.username` | Database username | `exception_user` |
| `postgresql.auth.password` | Database password | `exception-password` |
| `app.database.host` | External database host | `postgresql` |
| `app.database.port` | Database port | `5432` |

### Kafka Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `app.kafka.bootstrapServers` | Kafka bootstrap servers | `kafka:9092` |
| `app.kafka.consumerGroup` | Consumer group ID | `interface-exception-collector` |
| `kafkaTopics.enabled` | Enable Kafka topic creation | `true` |

### Redis Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `redis.enabled` | Enable Redis dependency | `true` |
| `redis.auth.password` | Redis password | `redis-password` |
| `app.redis.host` | External Redis host | `redis-master` |
| `app.redis.port` | Redis port | `6379` |

### Resource Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `resources.limits.cpu` | CPU limit | `1000m` |
| `resources.limits.memory` | Memory limit | `1Gi` |
| `resources.requests.cpu` | CPU request | `500m` |
| `resources.requests.memory` | Memory request | `512Mi` |

### Autoscaling Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `autoscaling.enabled` | Enable HPA | `true` |
| `autoscaling.minReplicas` | Minimum replicas | `2` |
| `autoscaling.maxReplicas` | Maximum replicas | `10` |
| `autoscaling.targetCPUUtilizationPercentage` | Target CPU utilization | `70` |

### Ingress Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `ingress.enabled` | Enable ingress | `false` |
| `ingress.className` | Ingress class name | `""` |
| `ingress.hosts[0].host` | Hostname | `exception-collector.local` |

### Monitoring Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `monitoring.enabled` | Enable monitoring | `true` |
| `monitoring.serviceMonitor.enabled` | Enable ServiceMonitor | `true` |
| `monitoring.serviceMonitor.interval` | Scrape interval | `30s` |

## Environment-Specific Deployments

### Development Environment

```bash
helm install exception-collector ./helm -f helm/values-dev.yaml
```

### Staging Environment

```bash
helm install exception-collector ./helm -f helm/values-staging.yaml
```

### Production Environment

```bash
helm install exception-collector ./helm -f helm/values-prod.yaml
```

## Upgrading

To upgrade the chart:

```bash
helm upgrade exception-collector ./helm -f values-prod.yaml
```

## Database Migrations

Database migrations are automatically executed using Helm hooks before installation and upgrades. The migration job runs with the `pre-install` and `pre-upgrade` hooks.

To disable automatic migrations:

```yaml
migration:
  enabled: false
```

## Kafka Topic Creation

Kafka topics are automatically created using Helm hooks. To disable automatic topic creation:

```yaml
kafkaTopics:
  enabled: false
```

## Health Checks

The application includes comprehensive health checks:

- **Liveness Probe**: `/actuator/health/liveness`
- **Readiness Probe**: `/actuator/health/readiness`
- **Health Endpoint**: `/actuator/health`

## Monitoring and Metrics

The application exposes Prometheus metrics at `/actuator/prometheus`. When monitoring is enabled, a ServiceMonitor resource is created for automatic discovery by Prometheus.

## Security

The chart includes several security features:

- Non-root container execution
- Read-only root filesystem
- Security contexts and pod security contexts
- RBAC configuration
- Secret management for sensitive data

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   ```bash
   kubectl logs -l app.kubernetes.io/name=interface-exception-collector
   kubectl describe pod -l app.kubernetes.io/name=interface-exception-collector
   ```

2. **Kafka Connection Issues**
   ```bash
   kubectl exec -it deployment/exception-collector -- kafka-topics --bootstrap-server kafka:9092 --list
   ```

3. **Migration Failures**
   ```bash
   kubectl logs job/exception-collector-migration
   ```

### Testing the Deployment

Run the Helm test to verify the deployment:

```bash
helm test exception-collector
```

## Contributing

Please read the contributing guidelines before submitting pull requests.

## License

This chart is licensed under the Apache 2.0 License.