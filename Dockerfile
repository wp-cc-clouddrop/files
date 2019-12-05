FROM openjdk:11
# uncomment if need write to filesystem
# VOLUME /tmp

# fix for google cloud vision issue (https://github.com/grpc/grpc-java/issues/5655)
# RUN apk update && apk add libc6-compat

ARG JAR_FILE=target/files-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dlogging.level.com.clouddrop.files=TRACE", "-Dlogging.level.org.springframework=TRACE" , "-jar","/app.jar"]
EXPOSE 8080