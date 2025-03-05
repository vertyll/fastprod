# Arguments
ARG JAVA_VERSION=23
ARG MAVEN_VERSION=3.9.5
ARG APP_PORT=8080
ARG DEBUG_PORT=5005

# Base stage
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS base

ARG MAVEN_VERSION

WORKDIR /app

# Install maven
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -P /tmp && \
    tar xf /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn && \
    rm -rf /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    apt-get remove -y wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Development stage
FROM base AS development

WORKDIR /app

ARG APP_PORT
ARG DEBUG_PORT

EXPOSE ${APP_PORT}
EXPOSE ${DEBUG_PORT}

# Run the application with devtools and remote debugging
CMD mvn spring-boot:run -Dspring-boot.run.jvmArguments="-XX:TieredStopAtLevel=1 -Dspring.devtools.restart.enabled=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# Build stage
FROM base AS build

WORKDIR /app

COPY fastprod-backend .

RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:${JAVA_VERSION}-jre AS production

WORKDIR /app

ARG APP_PORT

COPY --from=build /app/target/*.jar app.jar

EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]