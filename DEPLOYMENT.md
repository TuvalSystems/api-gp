# Despliegue con configuracion externa

Los perfiles privados dev, qa y prod se conservan fuera de Git y del JAR.
`application.properties` contiene la configuracion comun y permanece en el JAR.

## Servidor

Ruta: `/opt/apigp/config/application-{dev,qa,prod}.properties`.
Directorio propiedad de root con permisos 700; archivos propiedad de root con
permisos 600. El contenedor actual utiliza root. Si cambia el usuario de la imagen,
ajustar los permisos y verificar la lectura antes de desplegar.

El pipeline monta el directorio en `/config` como solo lectura y configura
`SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/` y el perfil activo. No copia
configuracion privada durante el build. Para cambiar una configuracion, actualizar
el archivo del servidor de forma segura y reiniciar el contenedor.

## Jenkins

Usar el contenido de `Jenkinsfile` en el job actual, o configurar Pipeline script
from SCM con la rama develop y Script Path Jenkinsfile una vez publicado el cambio.
Mantener JDK 21 y la credencial SSH `deploy-ssh-key`, con la llave privada que
corresponde a la llave publica autorizada en root. Una llave publica sola no permite
autenticarse. El checkout conserva la configuracion Git del pipeline anterior.

El agente necesita Maven Wrapper, Docker, ssh, scp, ssh-keyscan y ssh-keygen.
La huella del servidor se fija en el Jenkinsfile; si cambia la llave del servidor,
verificar la nueva huella por un canal confiable antes de actualizarla.

El pipeline usa dev. Para prod, configurar el perfil y el destino correspondientes
en un job separado; cambiar solo el perfil en el mismo destino reemplazaria apigp.
No ejecutar dos jobs que desplieguen simultaneamente al mismo contenedor.

Se comprueba la ausencia de perfiles privados en el JAR, la lectura de la
configuracion antes de detener la app y que el contenedor permanezca ejecutandose
sin reinicios durante 10 segundos. Esto no verifica disponibilidad HTTP ni las
conexiones a las bases de datos. Se mantiene el comportamiento previo de omitir
tests. Las imagenes remotas anteriores se conservan; requieren limpieza periodica.

## Desarrollo local

Las copias privadas locales se conservan, pero Maven ya no las incluye en recursos.
Para ejecutar localmente, establecer estas variables de entorno (ruta relativa a
la raiz del proyecto) antes de iniciar Spring:

```text
SPRING_PROFILES_ACTIVE=dev
SPRING_CONFIG_ADDITIONAL_LOCATION=file:./src/main/resources/
```

## Migracion

Publicar juntos el Jenkinsfile, el pom.xml, los archivos ignore y las retiradas de
los tres perfiles del indice de Git. No borrar las copias del servidor.
El contenedor existente se reemplaza cuando se ejecute el nuevo pipeline.
Los perfiles siguen en el historial Git y las imagenes/JAR antiguos: rotar las
credenciales que estuvieron versionadas y gestionar esos artefactos antiguos.
