# ==========================================
# STAGE 1: Build the Executable JAR using Maven & Java 21
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Lightweight Production Runtime Image
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create unprivileged system user for security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled executable JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8761 for Eureka Service Registry
EXPOSE 8761

# Set runtime system user
USER appuser

# Launch Microservice #6 (Eureka Server)
ENTRYPOINT ["java", "-jar", "app.jar"]
