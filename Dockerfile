FROM maven:3.6-jdk-10-slim as maven
WORKDIR /build
COPY . /build
RUN mvn -DskipTests package

FROM openjdk:10
WORKDIR /app
COPY --from=maven /build/target/docker-event-dispatcher.jar /app/docker-event-dispatcher.jar
CMD ["java", "-jar", "docker-event-dispatcher.jar"]
