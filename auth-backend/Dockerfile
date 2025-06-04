# Use OpenJDK 22 with Alpine base image
FROM eclipse-temurin:22-jdk-alpine

# Set working directory
WORKDIR /app

# Copy built Spring Boot JAR
COPY target/auth-backend-*.jar app.jar

# Expose the backend port
EXPOSE 8080

# Run with production profile
ENTRYPOINT ["java", "-jar", "app.jar"]
