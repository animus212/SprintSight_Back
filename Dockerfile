# 1. Build Stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
# 2. Run Stage
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
# This line finds the .jar in the target folder created in the build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Adding memory limits to keep Spring Boot within Render's free RAM
ENTRYPOINT ["java", "-Xmx400m", "-Xms256m", "-jar", "app.jar"]
