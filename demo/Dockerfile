# Stage 1: Development stage
FROM maven:3.9-eclipse-temurin-17 AS development
WORKDIR /app

# Copy Maven wrapper files first
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd ./
RUN chmod +x mvnw

# Copy project files
COPY pom.xml .
COPY src ./src

# Development mode: use Spring Boot DevTools
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005'"]

# Stage 2: Production stage
FROM eclipse-temurin:17-jre-alpine AS production
WORKDIR /app
COPY --from=development /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 