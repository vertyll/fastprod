ARG GRADLE_VERSION=9.3.1
ARG JAVA_VERSION=25

# Build stage
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS build
WORKDIR /app

# Vaadin requires Node.js for building the production package
RUN apk add --no-cache nodejs npm

# Copy the entire frontend folder
COPY fastprod-frontend/ .

RUN chmod +x gradlew

# Build the application (Vaadin + Spring Boot Jar)
RUN ./gradlew vaadinBuildFrontend bootJar --no-daemon --parallel

# Production stage
FROM azul/zulu-openjdk-alpine:${JAVA_VERSION}-jre AS production
WORKDIR /app

RUN apk add --no-cache bash curl && \
    addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --ingroup appuser appuser

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

USER appuser

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8001

EXPOSE 8001

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8001/actuator/health || exit 1

# Use exec form to ensure signals are received properly
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]