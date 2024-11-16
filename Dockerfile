# Utiliza una imagen base de Java
FROM openjdk:17-jdk-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR de la aplicación al contenedor
COPY target/vg-ms-study-programme-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto que utiliza la aplicación
EXPOSE 8082

# Define el comando de inicio de la aplicación
CMD ["java", "-jar", "app.jar"]
