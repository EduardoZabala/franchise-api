# syntax=docker/dockerfile:1

# ---------- Stage 1: build the executable jar ----------
FROM docker.io/library/eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Gradle wrapper and build scripts first: dependencies are cached
# as long as these files do not change.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon > /dev/null

COPY src ./src
# Tests run in CI / locally with Testcontainers, not inside the image build.
RUN ./gradlew bootJar --no-daemon -x test \
    && cp build/libs/*-SNAPSHOT.jar app.jar

# ---------- Stage 2: minimal runtime image ----------
FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
