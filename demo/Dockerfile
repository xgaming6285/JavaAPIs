FROM maven:3.9-eclipse-temurin-17 AS development
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd ./
RUN chmod +x mvnw
COPY pom.xml .
COPY src ./src

CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005'"]

FROM eclipse-temurin:17-jre-alpine AS production
WORKDIR /app
COPY --from=development /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]