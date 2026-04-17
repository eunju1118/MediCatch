# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml                    ./
COPY eureka-server/pom.xml      eureka-server/
COPY api-gateway/pom.xml        api-gateway/
COPY health-service/pom.xml     health-service/
COPY insurance-service/pom.xml  insurance-service/
COPY chat-service/pom.xml       chat-service/

RUN mvn dependency:go-offline -pl api-gateway -B -q

COPY api-gateway/src api-gateway/src
RUN mvn package -pl api-gateway -DskipTests -B -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S medicatch && adduser -S medicatch -G medicatch

WORKDIR /app
COPY --from=build --chown=medicatch:medicatch \
     /workspace/api-gateway/target/api-gateway-*.jar app.jar

USER medicatch

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
