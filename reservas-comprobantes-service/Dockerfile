FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} reservas-comprobantes-service.jar
ENTRYPOINT ["java","-jar","/reservas-comprobantes-service.jar"]