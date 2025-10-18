# Arguments
ARG JAVA_VERSION=21
ARG GRADLE_VERSION=8.14
ARG APP_PORT=8080
ARG DEBUG_PORT=5005

# Base stage
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS base

WORKDIR /app

# Install Gradle
ARG GRADLE_VERSION
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    ln -s /opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle && \
    rm /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    apt-get remove -y wget unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Development stage
FROM base AS development

WORKDIR /app

ARG APP_PORT
ARG DEBUG_PORT

EXPOSE ${APP_PORT}
EXPOSE ${DEBUG_PORT}

# Run the application with devtools and remote debugging using Gradle
CMD ["gradle", "bootRun", "--args=--spring-boot.run.jvmArguments='-XX:TieredStopAtLevel=1 -Dspring.devtools.restart.enabled=true -Dspring.devtools.restart.poll-interval=2s -Dspring.devtools.restart.quiet-period=1s -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005'"]

# Build stage
FROM base AS build

WORKDIR /app

COPY fastprod-backend .
RUN gradle clean bootJar -x test

# Production stage
FROM eclipse-temurin:${JAVA_VERSION}-jre AS production

WORKDIR /app

ARG APP_PORT
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE ${APP_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
