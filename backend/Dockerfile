# ---- Build layer ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy wrapper + pom first to cache dependencies
COPY .mvn ./.mvn
COPY mvnw mvnw.cmd pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

# Copy sources and build
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ---- Runtime layer ----
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
