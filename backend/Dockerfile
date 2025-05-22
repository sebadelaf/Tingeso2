# 1. Imagen Base: ¿Sobre qué sistema operativo y qué Java correrá?
# Usamos una imagen oficial de Java 17 (Eclipse Temurin) basada en Ubuntu Jammy.
# Coincide con la versión Java de tu pom.xml.
FROM eclipse-temurin:17-jdk-jammy

# 2. Directorio de Trabajo: ¿Dónde vivirá nuestra app dentro del contenedor?
# Creamos '/app' y nos movemos allí.
WORKDIR /app

# 3. Copiar el JAR: Copiamos el backend.jar desde tu máquina al contenedor.
# Asegúrate que 'backend.jar' sea el nombre correcto de tu archivo en la carpeta 'target'.
COPY target/backend.jar /app/app.jar

# 4. Exponer el Puerto: Informamos que la app DENTRO usará el puerto 8080.
EXPOSE 8080

# 5. Comando de Ejecución: Le decimos a Docker cómo iniciar tu aplicación.
# Ejecuta 'java -jar /app/app.jar' cuando el contenedor arranque.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]