ARG JDK_IMAGE=azul/zulu-openjdk:25-latest
ARG JRE_IMAGE=azul/zulu-openjdk:25-jre-headless-latest

FROM ${JDK_IMAGE} AS build
WORKDIR /workspace
COPY fastprod-frontend/ ./
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon -Pvaadin.productionMode=true vaadinBuildFrontend bootJar

FROM ${JDK_IMAGE} AS extract
WORKDIR /extract
COPY --from=build /workspace/build/libs/fastprod-frontend.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination layers

FROM ${JRE_IMAGE} AS runtime
WORKDIR /app
RUN groupadd --system --gid 1001 fastprod && useradd --system --uid 1001 --gid fastprod fastprod

COPY --from=extract --chown=fastprod:fastprod /extract/layers/dependencies/ ./
COPY --from=extract --chown=fastprod:fastprod /extract/layers/spring-boot-loader/ ./
COPY --from=extract --chown=fastprod:fastprod /extract/layers/snapshot-dependencies/ ./
COPY --from=extract --chown=fastprod:fastprod /extract/layers/application/ ./

USER fastprod
EXPOSE 8001

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8001 && printf 'GET /actuator/health/liveness HTTP/1.0\\r\\n\\r\\n' >&3 && grep -q '\"UP\"' <&3"]

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
