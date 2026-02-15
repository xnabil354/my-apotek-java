# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for security (best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar
# Copy the dependencies
COPY --from=build /app/target/lib ./lib

# Create the data directory (volume mount point)
USER root
RUN mkdir -p /app/data && chown spring:spring /app/data
USER spring

# Expose the port the app runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
