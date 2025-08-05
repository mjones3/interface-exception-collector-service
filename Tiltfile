# Tiltfile for Interface Exception Collector Service

# Load extensions
load('ext://restart_process', 'docker_build_with_restart')

# Build the Docker image
docker_build(
    'interface-exception-collector',
    '.',
    dockerfile='src/main/docker/Dockerfile',
    live_update=[
        sync('./target', '/app/target'),
        run('java -jar /app/app.jar', trigger=['./target/interface-exception-collector-service-*.jar'])
    ]
)

# Deploy Kubernetes resources
k8s_yaml(['src/main/k8s/postgres.yaml'])
k8s_yaml(['src/main/k8s/kafka.yaml'])
k8s_yaml(['src/main/k8s/deployment.yaml'])
k8s_yaml(['src/main/k8s/service.yaml'])

# Create secrets
k8s_yaml(blob("""
apiVersion: v1
kind: Secret
metadata:
  name: postgres-secret
type: Opaque
data:
  username: ZXhjZXB0aW9uX3VzZXI=  # exception_user
  password: ZXhjZXB0aW9uX3Bhc3M=  # exception_pass
"""))

# Port forward for local access
k8s_resource('interface-exception-collector', port_forwards='8080:8080')
k8s_resource('postgres', port_forwards='5432:5432')
k8s_resource('kafka', port_forwards='9092:9092')

# Local Maven build trigger
local_resource(
    'maven-build',
    'mvn clean package -DskipTests',
    deps=['src', 'pom.xml'],
    ignore=['target']
)