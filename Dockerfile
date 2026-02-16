FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/target/lib ./lib

RUN mkdir -p /app/data && chmod 777 /app/data

ENV DB_URL=jdbc:h2:file:/app/data/my_apotek

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
