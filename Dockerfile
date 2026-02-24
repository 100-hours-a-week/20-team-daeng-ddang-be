# Build Stage
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew settings.gradle* build.gradle* gradle.properties* ./
COPY gradle/ ./gradle/

RUN --mount=type=cache,target=/root/.gradle \
    chmod +x ./gradlew && \
    ./gradlew -x test dependencies

COPY src/ ./src/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew -x test bootJar

# Runtime Stage
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]