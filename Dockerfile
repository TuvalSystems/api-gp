# Usa OpenJDK 21 en lugar de 17 

FROM eclipse-temurin:21-jre-alpine

#Establece el director de trabajo en /app
WORKDIR /app


#Copia el archivo jar en el contenedor en la ruta /app
# COPY web-0.0.1-SNAPSHOT.jar ./app.jar
COPY target/web-0.0.1-SNAPSHOT.jar ./app.jar

#Expone el puerto 8080
EXPOSE 8006

#Comando para ejecutar la aplicación

ENTRYPOINT ["java", "-jar", "app.jar"]