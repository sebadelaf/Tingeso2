FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} rack-pista-service.jar
ENTRYPOINT ["java","-jar","/rack-pista-service.jar"]