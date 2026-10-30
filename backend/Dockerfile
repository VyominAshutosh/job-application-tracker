# Render has no built-in Java option, so it runs this file instead.

# Stage 1: build the jar with Maven and Java 21.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests package

# Stage 2: copy only the jar onto a smaller Java 21 runtime image and start it.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
