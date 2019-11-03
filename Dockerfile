FROM openjdk:8-jdk-alpine
# uncomment if need write to filesystem
# VOLUME /tmp
ARG JAR_FILE=target/files-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar", "--debug"]
EXPOSE 8080