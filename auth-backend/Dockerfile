FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built Spring Boot JAR
COPY target/auth-backend-*.jar app.jar

# Copy PEM keys into the image (readonly in prod)
COPY secrets/keys/public.pem public.pem
COPY secrets/keys/private.pem private.pem

# Expose port 8080
EXPOSE 8080

# Run the app with prod profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
