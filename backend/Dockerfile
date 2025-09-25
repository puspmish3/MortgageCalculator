# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/mortgage-calculator-api-1.0.0.jar app.jar

# Create a non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Expose the port the app runs on
EXPOSE 8080

# Set environment variables for production
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the jar file
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]