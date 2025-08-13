# Tiltfile for Interface Exception Collector Service Local Development

# Load extensions
load('ext://restart_process', 'docker_build_with_restart')

# Configuration
config.define_string("namespace", args=False, usage="Kubernetes namespace to use")
config.define_bool("skip-tests", args=False, usage="Skip running tests on startup")
cfg = config.parse()
namespace = cfg.get("namespace", "default")
skip_tests = cfg.get("skip-tests", False)

# Set namespace
k8s_namespace(namespace)

# PostgreSQL Database
k8s_yaml('k8s/postgres.yaml')
k8s_resource('postgres', port_forwards='5432:5432')

# Redis Cache
k8s_yaml('k8s/redis.yaml')
k8s_resource('redis', port_forwards='6379:6379')

# Kafka (using simplified configuration)
k8s_yaml('k8s/kafka-simple.yaml')
k8s_resource('kafka', port_forwards='9092:9092')

# Kafka UI for development
k8s_yaml('k8s/kafka-ui.yaml')
k8s_resource('kafka-ui', port_forwards='8081:8080', resource_deps=['kafka'])

# Database migration job
k8s_yaml('k8s/migration-job.yaml')
k8s_resource('migration-job', resource_deps=['postgres'])

# Kafka topics setup job
k8s_yaml('k8s/kafka-topics-job.yaml')
k8s_resource('kafka-topics-job', resource_deps=['kafka'])

# Prepare dependencies (run once)
local_resource(
    'maven-dependencies',
    'mvn dependency:copy-dependencies -DoutputDirectory=target/lib -DincludeScope=runtime',
    deps=['pom.xml'],
    ignore=['src']
)

# Build the application with live reload
local_resource(
    'maven-compile',
    'mvn compile',
    deps=['src/main', 'pom.xml'],
    ignore=['src/test'],
    resource_deps=['maven-dependencies']
)

# Docker build for the Java application with live reload
docker_build(
    'interface-exception-collector',
    '.',
    dockerfile='Dockerfile.dev',
    live_update=[
        sync('./target/classes', '/app/target/classes'),
    ]
)

# Application deployment
k8s_yaml('k8s/app.yaml')
k8s_resource(
    'interface-exception-collector',
    port_forwards=['8080:8080', '5005:5005'],
    resource_deps=['postgres', 'redis', 'kafka']
)

# Local resource for running tests
local_resource(
    'run-tests',
    'mvn test -q',
    deps=['src/test', 'src/main'],
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL
)

# Local resource for Maven package (useful for debugging)
local_resource(
    'maven-package',
    'mvn package -Dmaven.test.skip=true',
    deps=['src', 'pom.xml'],
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL
)

# Local resource for cleaning up Kafka topics (useful for development)
local_resource(
    'cleanup-kafka-topics',
    'kubectl exec -n ' + namespace + ' deployment/kafka -- kafka-topics --bootstrap-server localhost:9092 --delete --topic ".*exception.*" || true',
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL
)

print("🚀 Interface Exception Collector Service development environment ready!")
print("📊 Kafka UI: http://localhost:8081")
print("🔍 Application: http://localhost:8080")
print("📖 API Docs: http://localhost:8080/swagger-ui.html")
print("💊 Health Check: http://localhost:8080/actuator/health")
print("📈 Metrics: http://localhost:8080/actuator/prometheus")
print("🐛 Debug Port: localhost:5005")