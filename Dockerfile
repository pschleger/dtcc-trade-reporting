# Stage 1: Build the application using Gradle
FROM gradle:7.6-jdk21 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the project files from the build context (current directory)
COPY . .

# Run Gradle to build the bootJar
RUN ./gradlew bootJarPrototype

# Stage 2: Set up the production runtime environment
FROM openjdk:21-jdk-slim AS production

# Set the working directory in the container
WORKDIR /app

# Copy the generated JAR file from the builder stage
COPY --from=builder /app/build/libs/java-client-template-1.0-SNAPSHOT-prototype.jar /app/app.jar

# Expose the port on which the app will run (default Spring Boot port)
EXPOSE 8080

# Set the default command to run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]