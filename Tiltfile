# Tiltfile for BioPro Interface Services Local Development

# Load extensions
load('ext://restart_process', 'docker_build_with_restart')

# Configuration
config.define_string("namespace", args=False, usage="Kubernetes namespace to use")
config.define_bool("skip-tests", args=False, usage="Skip running tests on startup")
config.define_string("services", args=False, usage="Comma-separated list of services to run (interface-exception-collector,partner-order-service)")
cfg = config.parse()
namespace = cfg.get("namespace", "default")
skip_tests = cfg.get("skip-tests", False)
services_to_run = cfg.get("services", "interface-exception-collector,partner-order-service").split(",")

# Set namespace
k8s_namespace(namespace)

# Shared Infrastructure
# PostgreSQL Database (Interface Exception Collector)
k8s_yaml('k8s/postgres.yaml')
k8s_resource('postgres', port_forwards='5432:5432')

# PostgreSQL Database (Partner Order Service)
k8s_yaml('k8s/partner-order-postgres.yaml')
k8s_resource('partner-order-postgres', port_forwards='5433:5432')

# Redis Cache
k8s_yaml('k8s/redis.yaml')
k8s_resource('redis', port_forwards='6379:6379')

# Kafka (using simplified configuration)
k8s_yaml('k8s/kafka-simple.yaml')
k8s_resource('kafka', port_forwards='9092:9092')

# Kafka UI for development
k8s_yaml('k8s/kafka-ui.yaml')
k8s_resource('kafka-ui', port_forwards='8081:8080', resource_deps=['kafka'])

# Database migration jobs
k8s_yaml('k8s/migration-job.yaml')
k8s_resource('migration-job', resource_deps=['postgres'])

k8s_yaml('k8s/partner-order-migration-job.yaml')
k8s_resource('partner-order-migration-job', resource_deps=['partner-order-postgres'])

# Kafka topics setup job
k8s_yaml('k8s/kafka-topics-job.yaml')
k8s_resource('kafka-topics-job', resource_deps=['kafka'])

# Mock RSocket Server for development and testing
k8s_yaml('k8s/mock-rsocket-server.yaml')
k8s_resource(
    'mock-rsocket-server',
    port_forwards='7000:7000'
)

# Watch for mapping and response file changes
watch_file('./mappings')
watch_file('./mock-responses')

# Local resource to reload mock server when mappings change
local_resource(
    'reload-mock-mappings',
    'kubectl rollout restart deployment/mock-rsocket-server -n ' + namespace,
    deps=['mappings', 'mock-responses'],
    auto_init=False,
    trigger_mode=TRIGGER_MODE_AUTO
)

# Interface Exception Collector Service
if 'interface-exception-collector' in services_to_run:
    # Prepare dependencies
    local_resource(
        'iec-maven-dependencies',
        'cd interface-exception-collector && mvn dependency:copy-dependencies -DoutputDirectory=target/lib -DincludeScope=runtime',
        deps=['interface-exception-collector/pom.xml', 'pom.xml'],
        ignore=['interface-exception-collector/src']
    )

    # Build the application with live reload
    local_resource(
        'iec-maven-compile',
        'cd interface-exception-collector && mvn compile -q',
        deps=['interface-exception-collector/src/main', 'interface-exception-collector/pom.xml'],
        ignore=['interface-exception-collector/src/test'],
        resource_deps=['iec-maven-dependencies']
    )

    # Docker build for the Java application with live reload
    docker_build(
        'interface-exception-collector',
        '.',
        dockerfile='./interface-exception-collector/Dockerfile.dev',
        live_update=[
            sync('./interface-exception-collector/target/classes', '/app/target/classes'),
            sync('./interface-exception-collector/target/lib', '/app/target/lib'),
        ]
    )

    # Application deployment
    k8s_yaml('k8s/interface-exception-collector.yaml')
    k8s_resource(
        'interface-exception-collector',
        port_forwards=['8080:8080', '5005:5005'],
        resource_deps=['postgres', 'redis', 'kafka', 'migration-job', 'kafka-topics-job']
    )

# Partner Order Service
if 'partner-order-service' in services_to_run:
    # Prepare dependencies
    local_resource(
        'pos-maven-dependencies',
        'cd partner-order-service && mvn dependency:copy-dependencies -DoutputDirectory=target/lib -DincludeScope=runtime',
        deps=['partner-order-service/pom.xml', 'pom.xml'],
        ignore=['partner-order-service/src']
    )

    # Build the application with live reload
    local_resource(
        'pos-maven-compile',
        'cd partner-order-service && mvn compile -q',
        deps=['partner-order-service/src/main', 'partner-order-service/pom.xml'],
        ignore=['partner-order-service/src/test'],
        resource_deps=['pos-maven-dependencies']
    )

    # Docker build for the Java application with live reload
    docker_build(
        'partner-order-service',
        '.',
        dockerfile='./partner-order-service/Dockerfile.dev',
        live_update=[
            sync('./partner-order-service/target/classes', '/app/target/classes'),
            sync('./partner-order-service/target/lib', '/app/target/lib'),
        ]
    )

    # Application deployment
    k8s_yaml('k8s/partner-order-service.yaml')
    k8s_resource(
        'partner-order-service',
        port_forwards=['8090:8090', '5006:5005'],
        resource_deps=['partner-order-postgres', 'kafka', 'partner-order-migration-job', 'kafka-topics-job']
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

# Local resource for testing mock RSocket server connectivity
local_resource(
    'test-mock-rsocket-server',
    'echo "Testing mock RSocket server connectivity..." && timeout 3 nc -z localhost 7000 && echo "✅ Mock RSocket server is reachable on port 7000" || echo "❌ Mock RSocket server is not reachable (this is OK if it is still starting)"',
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL
)

# Local resource to force restart mock server if it hangs
local_resource(
    'restart-mock-rsocket-server',
    'kubectl rollout restart deployment/mock-rsocket-server -n ' + namespace + ' && echo "🔄 Mock RSocket server restarted"',
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL
)

print("🚀 BioPro Interface Services development environment ready!")
print("📊 Kafka UI: http://localhost:8081")
print("🔌 Mock RSocket Server: rsocket://localhost:7000")
print("🗄️  PostgreSQL Database: localhost:5432 (exception_collector_db)")
print("🔴 Redis Cache: localhost:6379")
if 'interface-exception-collector' in services_to_run:
    print("🔍 Interface Exception Collector: http://localhost:8080")
    print("   📖 API Docs: http://localhost:8080/swagger-ui.html")
    print("   💊 Health Check: http://localhost:8080/actuator/health")
    print("   📈 Metrics: http://localhost:8080/actuator/prometheus")
    print("   🐛 Debug Port: localhost:5005")
    print("   🎯 Profile: tilt (PostgreSQL + Redis + Kafka)")
if 'partner-order-service' in services_to_run:
    print("🏪 Partner Order Service: http://localhost:8090")
    print("📖 POS API Docs: http://localhost:8090/swagger-ui.html")
    print("💊 POS Health Check: http://localhost:8090/actuator/health")
    print("📈 POS Metrics: http://localhost:8090/actuator/prometheus")
    print("🐛 POS Debug Port: localhost:5006")