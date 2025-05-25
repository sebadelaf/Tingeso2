FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} tarifas-especiales-service.jar
ENTRYPOINT ["java","-jar","/tarifas-especiales-service.jar"]