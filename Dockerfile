FROM maven:3.9.9 AS builder

WORKDIR /wallet-service
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine
WORKDIR /wallet
COPY --from=builder /wallet-service/target/*.jar /wallet-app.jar

ENTRYPOINT ["java", "-jar", "/wallet-app.jar"]
EXPOSE 8080