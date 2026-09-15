# Stage 1: Build JAR using Gradle with JDK 17
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle /app/
COPY src /app/src
RUN gradle build --no-daemon -x test

# Stage 2: Runtime image using Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/order-service-1.0.0.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
