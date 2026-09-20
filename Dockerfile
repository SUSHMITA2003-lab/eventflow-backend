# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and pre-fetch dependencies for better build caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package application skipping tests
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Production Runtime
# ==========================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root system user and group for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built fat JAR from the build stage with non-root ownership
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose default port
EXPOSE 8080
ENV PORT=8080

# Run Spring Boot application supporting Render's dynamic PORT environment variable
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
