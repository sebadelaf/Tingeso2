FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} reportes-service.jar
ENTRYPOINT ["java","-jar","/reportes-service.jar"]