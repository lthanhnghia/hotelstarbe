# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy all project files
COPY . .

# Build the application and skip tests (optional)
RUN mvn clean package -DskipTests

# Stage 2: Use a lighter image to run the app
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=builder /app/target/Hotel_Stars-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
