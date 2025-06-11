FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built Spring Boot JAR
COPY target/auth-backend-*.jar app.jar

# Create the keys folder and copy PEM keys into it
RUN mkdir -p keys
COPY secrets/keys/public.pem keys/public.pem
COPY secrets/keys/private.pem keys/private.pem

# Expose port
EXPOSE 8080

# Run the app with prod profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
