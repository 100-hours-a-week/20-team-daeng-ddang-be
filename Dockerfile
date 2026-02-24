FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew && ./gradlew -x test build

ENTRYPOINT [ "java", "-jar", "build/libs/daengdong-map-0.0.1-SNAPSHOT.jar" ]