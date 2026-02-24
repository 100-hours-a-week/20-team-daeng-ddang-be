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

RUN groupadd -g 10001 app && useradd -m -u 10001 -g 10001 -s /usr/sbin/nologin app

COPY --from=build --chown=app:app /app/build/libs/*.jar /app/app.jar

USER 10001:10001
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]