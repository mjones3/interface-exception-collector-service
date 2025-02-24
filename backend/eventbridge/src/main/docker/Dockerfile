# Flyway
FROM flyway/flyway:10.21.0-alpine AS flyway

# Builder
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the source code into the image for building
COPY ./.mvn ./.mvn
COPY ./pom.xml .
COPY ./checkstyle.xml .
COPY ./sonar-project.properties .
COPY ./src/main/java ./src/main/java
COPY ./src/main/resources ./src/main/resources

RUN mvn install -DskipTests && \
    mkdir -p target/dependency && \
    (cd target/dependency; jar -xf ../*.jar)

# Runner
FROM eclipse-temurin:21-jre-alpine AS runner

############################################
### Install Bash
############################################
RUN apk add --no-cache bash

############################################
### Install Flyway CLI
############################################
WORKDIR /flyway

COPY --from=flyway /flyway /flyway
COPY ./src/main/db ./db

ENV PATH="/flyway:${PATH}"

############################################
### Install BIOPRO application
############################################
WORKDIR /app
COPY --from=builder /app/target/*.jar ./app.jar

ENV CLUSTER_PORT=${CLUSTER_PORT:-8082}
ENV ADMIN_PORT=${ADMIN_PORT:-8081}
ENV API_PORT=${API_PORT:-8080}

ENTRYPOINT [ "java", "-jar", "app.jar" ]

CMD [ "standalone" ]

EXPOSE $CLUSTER_PORT $ADMIN_PORT $API_PORT
