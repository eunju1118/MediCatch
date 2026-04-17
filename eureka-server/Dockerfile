# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy all POMs first — Docker cache invalidates only when POMs change
COPY pom.xml                    ./
COPY eureka-server/pom.xml      eureka-server/
COPY api-gateway/pom.xml        api-gateway/
COPY health-service/pom.xml     health-service/
COPY insurance-service/pom.xml  insurance-service/
COPY chat-service/pom.xml       chat-service/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl eureka-server -B -q

# Copy source and build
COPY eureka-server/src eureka-server/src
RUN mvn package -pl eureka-server -DskipTests -B -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S medicatch && adduser -S medicatch -G medicatch

WORKDIR /app
COPY --from=build --chown=medicatch:medicatch \
     /workspace/eureka-server/target/eureka-server-*.jar app.jar

USER medicatch

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8761

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
