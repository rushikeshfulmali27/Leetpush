# ---- Gradle Dependencies Cache ----
FROM gradle:8.5-jdk21-alpine AS gradle-cache
WORKDIR /app
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle.properties ./
RUN gradle dependencies --no-daemon

# ---- Build Stage ----
FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app
COPY --from=gradle-cache /root/.gradle /root/.gradle
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle.properties ./
COPY src/ src/
RUN gradle bootJar --no-daemon -x test

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache dumb-init && \
    addgroup -S app && \
    adduser -S app -G app

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["/sbin/dumb-init", "--"]
CMD ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxRAMPercentage=75", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
