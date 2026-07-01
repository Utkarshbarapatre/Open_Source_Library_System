# Build stage using Maven and Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml file to download dependencies first (caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application package (skipping tests for packaging speed)
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage using a lightweight JRE Alpine image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/osls-1.0.0.jar app.jar

# Expose port 8080
EXPOSE 8080

# Configure entrypoint to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
