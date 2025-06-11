FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ./target/*.jar ps-mp-app.jar
ENTRYPOINT ["java","-jar","ps-mp-app.jar"]