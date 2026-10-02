FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
ENV MAVEN_OPTS="-Dhttps.protocols=TLSv1.2 -Djdk.tls.client.protocols=TLSv1.2"
RUN --mount=type=cache,target=/root/.m2 mvn -B package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/AI-dianping-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
