# Step 1: Use an official Java runtime as a parent image
FROM eclipse-temurin:21-jdk-jammy

# Step 2: Set the working directory
WORKDIR /app

# Step 3: Copy your build file (assuming Maven)
COPY target/*.jar app.jar

# Step 4: Run the jar
ENTRYPOINT ["java", "-jar", "/app.jar"]