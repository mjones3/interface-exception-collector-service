# Local Cache
FROM maven:3.9.6-eclipse-temurin-21 AS local-cache

WORKDIR /app

# Fetch dependencies first
COPY ./pom.xml .
COPY ./sonar-project.properties .

RUN mvn dependency:go-offline -Dsilent=true -DexcludeGroupIds="com.arcone.biopro" -am

# Builder
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Then copy the rest
COPY ./src ./src
# Copy the local cache
COPY --from=local-cache /app/pom.xml .
COPY --from=local-cache /app/sonar-project.properties .
COPY --from=local-cache /root/.m2 /root/.m2

RUN chmod +x ./src/main/docker/scripts/build_backend_version.sh

# Build
RUN ./src/main/docker/scripts/build_backend_version.sh

# Runner
FROM eclipse-temurin:21-jre-jammy AS runner

WORKDIR /app
COPY --from=builder /app/target/*.jar ./app.jar

ENV CLUSTER_PORT=${CLUSTER_PORT:-8082}
ENV ADMIN_PORT=${ADMIN_PORT:-8081}
ENV API_PORT=${API_PORT:-8080}
ENV RSOCKET_PORT=${RSOCKET_PORT:-7002}


ENTRYPOINT [ "java", "-jar", "app.jar" ]

CMD [ "standalone" ]

EXPOSE $CLUSTER_PORT $ADMIN_PORT $API_PORT $RSOCKET_PORT
