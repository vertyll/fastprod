ARG GRADLE_VERSION=9.3.1
ARG JAVA_VERSION=25

# Build stage
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS build
WORKDIR /app

# Copy Gradle configuration files
COPY fastprod-backend/build.gradle.kts fastprod-backend/settings.gradle.kts ./
COPY fastprod-backend/gradle ./gradle
COPY fastprod-backend/gradlew ./
COPY fastprod-backend/gradlew.bat ./

# Copy source code
COPY fastprod-backend ./fastprod-backend

# Build the application
RUN gradle :fastprod-backend:bootJar --no-daemon --parallel

# Production stage
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS production
WORKDIR /app

# Install runtime dependencies, create user, and prepare directories in single layer
RUN apk add --no-cache \
    bash \
    netcat-openbsd \
    curl \
    dos2unix \
    postgresql-client && \
    addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --ingroup appuser appuser && \
    mkdir -p /var/app/uploads && \
    chown -R appuser:appuser /var/app/uploads

# Copy built JAR
COPY --from=build /app/fastprod-backend/build/libs/*.jar app.jar

# Copy entrypoint script and set permissions
COPY fastprod-backend/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN dos2unix /usr/local/bin/docker-entrypoint.sh && \
    chmod +x /usr/local/bin/docker-entrypoint.sh

USER appuser

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]
