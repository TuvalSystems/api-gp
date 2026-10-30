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

## GitHub Actions: ambiente local (dev)

Workflow: `.github/workflows/deploy-local.yml`.
Script de reemplazo y recuperacion: `scripts/deploy-local.sh`.

- Pull request hacia `develop`: compilar con Java 21 y ejecutar `mvnw clean verify`
  en un runner hospedado por GitHub. No usa el runner del servidor ni despliega.
- Push a `develop`: validar, guardar el JAR como artefacto por 3 dias y descargar
  ese mismo JAR en el runner local para construir y desplegar la imagen.
- Ejecucion manual: seleccionar `develop`; otras ramas no despliegan. Para que
  GitHub muestre `workflow_dispatch`, el workflow debe existir tambien en la rama
  predeterminada. Mientras tanto, el push a `develop` funciona como disparador.

Destino: `srv-docker00` (`172.30.1.25`), Docker local por
`unix:///var/run/docker.sock`. Contenedor `apigp`, perfil `dev`, red `host`, reinicio
`unless-stopped` y `/opt/apigp/config:/config:ro`. El puerto esperado de la app es
8006 salvo override en la configuracion externa. No se usan SSH, claves de
despliegue, Docker Hub ni GHCR: la imagen se construye en el servidor a partir
del JAR validado. Las imagenes se identifican por commit, run y reintento.

### Preparacion del runner

El runner `srv-docker00-apigp` debe estar instalado como servicio bajo `github`.
En Settings > Actions > Runners, editar sus etiquetas y agregar **`apigp-local`**.
El workflow exige esa etiqueta ademas de `self-hosted`, `Linux` y `X64`; el nombre
del runner no es una etiqueta. No asignar `apigp-local` a otros servidores.

Requisitos del usuario de servicio: Docker CLI y acceso al daemon local sin sudo;
Bash, `flock`, `hostname`, `curl` y Python 3.
Confirmar en una sesion del usuario `github`:

```bash
docker info > /dev/null
command -v bash flock hostname curl python3
```

Si el usuario no tiene acceso a Docker, el administrador puede agregarlo al grupo
que controla el socket (normalmente `docker`). Ese acceso permite administrar el
host; debe concederse solo al usuario dedicado del runner. Tras cambiar grupos,
reiniciar el servicio del runner para que obtenga los permisos nuevos. No usar
`chmod 666` en el socket ni convertir el runner en root.

El runner necesita salida HTTPS a GitHub y a los servicios de artefactos de Actions,
y salida al registro de la imagen base `eclipse-temurin`. El workflow solo requiere
`contents: read` del GITHUB_TOKEN, no secretos SSH ni tokens personales.

### Verificacion y recuperacion

Antes de detener la app se comprueba que el JAR no incluya perfiles privados y que
el usuario de la nueva imagen pueda leer el archivo externo, sin mostrar su contenido.
El contenedor anterior se detiene y se renombra temporalmente, conservando su
configuracion exacta. Si falla el reemplazo o su verificacion, se elimina el nuevo,
se devuelve el nombre `apigp` al anterior y se inicia si antes estaba ejecutandose.
El trabajo queda fallido aunque se restaure el anterior. Si falla la recuperacion
por un problema de Docker/host, se informa la necesidad de intervencion manual.

Por defecto se conserva la comprobacion del pipeline anterior: proceso en marcha
sin reinicios durante 10 segundos. Para comprobar disponibilidad real, definir la
variable de repositorio **`APIGP_HEALTHCHECK_URL`** con una URL HTTP local verificada
(por ejemplo `http://127.0.0.1:8006/actuator/health` solo si ese endpoint esta
habilitado, accesible y representa el estado requerido en este ambiente).
Con la variable configurada se espera una respuesta HTTP 2xx durante un maximo
aproximado de 120 segundos, ademas del estado del proceso. Sin ella, el workflow
advierte que no se verificaron HTTP ni SQL Server. Un endpoint que siempre devuelve
200 tampoco demuestra por si solo disponibilidad de las dos bases.

GitHub serializa los despliegues de apigp sin cancelar el que esta ejecutandose;
el script agrega un bloqueo local para otras invocaciones del mismo script.
Antes de reemplazar apigp, el workflow comprueba que su commit siga siendo la
punta de `develop`; un trabajo atrasado falla sin reemplazar el contenedor.
El reemplazo tiene una interrupcion breve; no es un despliegue sin downtime.
Un apagado del host o terminacion forzada que no permita ejecutar traps requiere
recuperacion manual. Las imagenes anteriores se conservan para recuperacion y
deben limpiarse con una politica separada, sin borrar imagenes de otros servicios.

### Cambio desde Jenkins

Desactivar el job de Jenkins (incluido pollSCM) antes de activar este workflow.
La concurrencia de GitHub y el bloqueo del script no coordinan el pipeline viejo.
Publicar el workflow en `develop` dispara el primer despliegue automaticamente
si el runner esta disponible; preparar antes su etiqueta, permisos y configuracion.
No ejecutar simultaneamente otro proceso que gestione `apigp`.
Otros ambientes y contenedores necesitan workflows y destinos propios.

## Desarrollo local

Las copias privadas locales se conservan, pero Maven ya no las incluye en recursos.
Para ejecutar localmente, establecer estas variables de entorno (ruta relativa a
la raiz del proyecto) antes de iniciar Spring:

```text
SPRING_PROFILES_ACTIVE=dev
SPRING_CONFIG_ADDITIONAL_LOCATION=file:./src/main/resources/
```

## Configuracion privada y artefactos antiguos

No borrar las copias privadas del servidor. El contenedor existente se reemplaza
cuando se ejecute el workflow de despliegue.
Los perfiles siguen en el historial Git y las imagenes/JAR antiguos: rotar las
credenciales que estuvieron versionadas y gestionar esos artefactos antiguos.
