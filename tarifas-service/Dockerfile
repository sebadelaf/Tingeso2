FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} tarifas-service.jar
ENTRYPOINT ["java","-jar","/tarifas-service.jar"]