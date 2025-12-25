FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
COPY api-service/pom.xml /app/api-service/pom.xml
COPY worker-service/pom.xml /app/worker-service/pom.xml
RUN mvn -q -pl worker-service -am dependency:go-offline
COPY worker-service /app/worker-service
RUN mvn -q -pl worker-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/worker-service/target/worker-service-0.0.1-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java","-jar","/app/app.jar"]
