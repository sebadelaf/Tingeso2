FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} descuentos-grupo-service.jar
ENTRYPOINT ["java","-jar","/descuentos-grupo-service.jar"]