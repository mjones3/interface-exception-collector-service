# Tilt Development Environment

This document describes how to use Tilt for local development of the Interface Exception Collector Service.

## Prerequisites

1. **Tilt**: Install from https://docs.tilt.dev/install.html
2. **kubectl**: Kubernetes command-line tool
3. **Kubernetes cluster**: One of the following:
   - Docker Desktop with Kubernetes enabled
   - minikube
   - kind
   - Any other local Kubernetes cluster
4. **Java 17+**: For Maven builds
5. **Maven 3.6+**: For dependency management

## Quick Start

1. **Start the development environment:**
   ```bash
   ./dev-start.sh
   ```

2. **Access the services:**
   - **Tilt UI**: http://localhost:10350
   - **Application**: http://localhost:8080
   - **API Documentation**: http://localhost:8080/swagger-ui.html
   - **Health Check**: http://localhost:8080/actuator/health
   - **Metrics**: http://localhost:8080/actuator/prometheus
   - **Kafka UI**: http://localhost:8081

## What Tilt Does

### Services Started
- **PostgreSQL**: Database with automatic schema migration
- **Redis**: Cache for performance optimization
- **Kafka**: Message broker with pre-created topics
- **Kafka UI**: Web interface for Kafka management
- **Application**: The main service with live reload

### Live Reload Features
- **Automatic compilation**: Changes to Java files trigger Maven compilation
- **Hot restart**: Application restarts automatically when classes change
- **File watching**: Monitors `src/main/java` for changes
- **Dependency management**: Handles Maven dependencies efficiently

### Development Features
- **Debug port**: Port 5005 exposed for remote debugging
- **Structured logging**: Enhanced logging for development
- **Health checks**: Kubernetes-style health and readiness probes
- **Resource limits**: Appropriate limits for local development

## Manual Operations

### Run Tests
```bash
tilt trigger run-tests
```

### Package Application
```bash
tilt trigger maven-package
```

### Clean Kafka Topics
```bash
tilt trigger cleanup-kafka-topics
```

## File Structure

```
├── Tiltfile                    # Main Tilt configuration
├── Dockerfile.dev              # Development Docker image
├── .tiltignore                 # Files to ignore for watching
├── dev-start.sh               # Development startup script
├── k8s/                       # Kubernetes manifests
│   ├── postgres.yaml          # PostgreSQL deployment
│   ├── redis.yaml             # Redis deployment
│   ├── kafka.yaml             # Kafka deployment
│   ├── kafka-ui.yaml          # Kafka UI deployment
│   ├── migration-job.yaml     # Database migration job
│   ├── kafka-topics-job.yaml  # Kafka topics setup job
│   └── app.yaml               # Application deployment
├── scripts/
│   └── prepare-build.sh       # Build preparation script
└── src/main/resources/
    └── application-local.yml   # Local development configuration
```

## Troubleshooting

### Application Won't Start
1. Check Tilt UI for error messages
2. Verify Kubernetes cluster is running: `kubectl cluster-info`
3. Check resource availability: `kubectl get pods`

### Database Connection Issues
1. Verify PostgreSQL pod is running: `kubectl get pods -l app=postgres`
2. Check migration job status: `kubectl get jobs`
3. View migration logs: `kubectl logs job/migration-job`

### Kafka Connection Issues
1. Verify Kafka pod is running: `kubectl get pods -l app=kafka`
2. Check topics creation: `kubectl logs job/kafka-topics-job`
3. Use Kafka UI to verify topics exist

### Live Reload Not Working
1. Check if Maven compilation is successful in Tilt UI
2. Verify file changes are in `src/main/java`
3. Check application logs for restart messages

### Port Conflicts
If ports are already in use, you can modify the port forwards in the Tiltfile:
```python
k8s_resource('interface-exception-collector', port_forwards=['8080:8080'])
```

## Advanced Usage

### Custom Namespace
```bash
tilt up -- --namespace=my-namespace
```

### Debug Mode
The application runs with debug port 5005 exposed. Connect your IDE debugger to `localhost:5005`.

### Environment Variables
Modify `k8s/app.yaml` to add custom environment variables for development.

### Resource Limits
Adjust resource limits in the Kubernetes manifests if you need more/less resources.

## Stopping the Environment

Press `Ctrl+C` in the terminal where Tilt is running, or:
```bash
tilt down
```

This will clean up all resources created by Tilt.