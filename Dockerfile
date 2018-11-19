FROM maven:3.6-jdk-10-slim as maven
WORKDIR /build
COPY . /build
RUN mvn -DskipTests package

FROM openjdk:10
WORKDIR /app
COPY --from=maven /build/target/docker-event-dispatcher.jar /app/docker-event-dispatcher.jar
# See https://docs.openshift.com/container-platform/3.3/creating_images/guidelines.html
RUN chgrp -R 0 /app && \
    chmod -R g=u /app
EXPOSE 8080
USER 1000
CMD ["java", "-jar", "docker-event-dispatcher.jar"]
