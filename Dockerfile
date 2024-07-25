# Builder
FROM artifactory.sha.ao.arc-one.com/docker/system/build-cicd/java_maven:21.0.2.3.9.7 AS builder

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
FROM artifactory.sha.ao.arc-one.com/docker/system/build-cicd/java_maven:21-jre-alpine AS runner

WORKDIR /app

ENV API_PORT=${API_PORT:-8080}

ARG START_CLASS
#=com.arcone.biopro.BioProApplication
ARG DEPENDENCY=/app/target/dependency
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app
ENV START_CLASS=${START_CLASS}

ENTRYPOINT java -noverify -XX:+AlwaysPreTouch -Djava.security.egd=file:/dev/./urandom -cp .:./lib/* ${START_CLASS} "$@"

EXPOSE ${API_PORT}
