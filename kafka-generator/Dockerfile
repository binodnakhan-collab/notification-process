FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || return 0
COPY . .
RUN ./gradlew bootJar -x test --no-daemon
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8001
ENTRYPOINT ["java", "-jar", "app.jar"]
