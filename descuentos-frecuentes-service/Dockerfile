FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} descuentos-frecuentes-service.jar
ENTRYPOINT ["java","-jar","/descuentos-frecuentes-service.jar"]