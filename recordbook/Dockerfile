# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests clean package

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy fat jar
COPY --from=builder /app/target/*.jar /app/app.jar

# Ensure config directory exists and copy YAML
RUN mkdir -p /app/config
COPY src/main/resources/application.yaml /app/config/application.yaml

EXPOSE 8080

# Use spring.config.location to point to your external YAML
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.config.location=file:/app/config/application.yaml"]