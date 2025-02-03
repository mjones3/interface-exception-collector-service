# Flyway
FROM artifactory.sha.ao.arc-one.com/docker/flyway/flyway:10.21.0-alpine AS flyway

# Builder
FROM artifactory.sha.ao.arc-one.com/docker/maven:3.9.9-eclipse-temurin-21-alpine AS builder

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
FROM artifactory.sha.ao.arc-one.com/docker/eclipse-temurin:21-jre-alpine AS runner

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

ARG START_CLASS
#=com.arcone.biopro.seeddata.seeddata.BioProApplication
ARG DEPENDENCY=/app/target/dependency
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

# RUN echo ${START_CLASS}

ENV START_CLASS=${START_CLASS}
ENTRYPOINT java -XX:+AlwaysPreTouch -Djava.security.egd=file:/dev/./urandom -cp .:./lib/* ${START_CLASS} "$@"

EXPOSE 8080
