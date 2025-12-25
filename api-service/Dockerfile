FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
COPY api-service/pom.xml /app/api-service/pom.xml
COPY worker-service/pom.xml /app/worker-service/pom.xml
RUN mvn -q -pl api-service -am dependency:go-offline
COPY api-service /app/api-service
RUN mvn -q -pl api-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/api-service/target/api-service-0.0.1-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=local
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
