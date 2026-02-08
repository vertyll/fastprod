ARG GRADLE_VERSION=9.3.1
ARG JAVA_VERSION=25

# Build stage
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS build
WORKDIR /app

# Install Node.js dla Vaadin frontend build
RUN apk add --no-cache nodejs npm

# Copy Gradle configuration files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Copy source code
COPY fastprod-frontend ./fastprod-frontend

# Build the application with Vaadin production build
RUN gradle :fastprod-frontend:vaadinBuildFrontend :fastprod-frontend:bootJar --no-daemon --parallel

# Production stage
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS production
WORKDIR /app

# Install runtime dependencies and create user in single layer
RUN apk add --no-cache \
    bash \
    curl && \
    addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --ingroup appuser appuser

# Copy built JAR
COPY --from=build /app/fastprod-frontend/build/libs/*.jar app.jar

USER appuser

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8001

EXPOSE 8001

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8001/actuator/health || exit 1

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
