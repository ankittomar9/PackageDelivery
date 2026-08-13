# ==========================================
# STAGE 1: Build the JAR using Maven & Java 21
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build final executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Lightweight Runtime Image
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root system user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from Stage 1
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8084
EXPOSE 8084

# Change user ownership
USER appuser

# Health check endpoint
#HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
 # CMD wget --quiet --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Launch Spring Boot Application
ENTRYPOINT ["java", "-jar", "app.jar"]