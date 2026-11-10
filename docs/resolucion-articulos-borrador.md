# Resolver articulos y crear un borrador de prueba

## Alcance y contrato comprobado

Nueva operacion explicita: `POST /api/reportes/resolucion-articulos/crear-borrador`.
`POST /api/reportes/resolucion-articulos/consultar` conserva su contrato y nunca crea borradores.
La nueva operacion reutiliza el resolver, su DAO Dynamics y sus limites. No agrega SQL,
conexiones, precios, descuentos, reservas, aprobaciones ni sincronizacion a GP.

Se verifico el 2026-11-10 el codigo de tuvalBackend/develop:
- [Ruta web](https://github.com/TuvalSystems/tuvalBackend/blob/develop/routes/web.php):
  `GET /cotizaciones/borrador-items`, sin middleware web.
- [Request](https://github.com/TuvalSystems/tuvalBackend/blob/develop/app/Http/Requests/Venta/Cotizaciones/StoreQuotationItemsDraftRequest.php):
  cuerpo JSON con `cliente_ruc`, `pv_id`, `items`; solo local/testing/qa/staging.
- [Controller](https://github.com/TuvalSystems/tuvalBackend/blob/develop/app/Http/Controllers/Ventas/Cotizaciones/Borradores/CotizacionBorradorController.php):
  usuario fijo 6, creacion 201 con success/message/data.
- [Servicio](https://github.com/TuvalSystems/tuvalBackend/blob/develop/app/Services/Ventas/Cotizaciones/Borradores/QuotationItemsDraftService.php):
  valida articulos/unidad/precision y guarda borrador en transaccion. No acepta request_id ni tiene idempotencia persistente.

Este contrato NO es el anterior `POST /api/cotizaciones/borrador-items` con Bearer/UUID.
El puente anterior `/api/reportes/cotizacion-borrador` queda intacto y no se usa como fallback.
No se verifico el despliegue remoto de estos cambios ni se creo un borrador real en esta tarea.

## Peticion

```json
{
  "request_id": "e0f7a456-2026-4000-8000-000000000001",
  "cliente_ruc": "0990007020001",
  "renglones": [
    {"texto": "MECH BUSHING GALVANIZADO 150 UL/FM 1\" X 1/2\" NPT"}
  ]
}
```

Usar una UUID nueva para una nueva operacion intencional. La UUID de arriba es solo un ejemplo.
Cliente omitido: `0990007020001`. Punto de venta omitido: `2` (California).
Cantidad omitida: `1`, conservando `cantidadAsumida` en la resolucion.
`cliente_ruc` y `pv_id` explicitos se respetan. El usuario 6 lo fija Laravel; no se envia ni acepta `user_id`.

La creacion admite 1-20 renglones, texto de hasta 200 caracteres, cantidades positivas
hasta 1000000 con maximo 2 decimales (limite real del destino), sin exponentes ni redondeo.
Se rechazan null explicitos, precios, unidades, URLs y campos desconocidos. El contrato
original de consulta sigue admitiendo 3 decimales; no significa que se puedan persistir sin redondear.

El flujo confirmado por el usuario envia el codigo identificado o los codigos opcion
que devuelve el resolver, hasta cinco por renglon. Un ambiguo no se convierte en una
seleccion unica: sus opciones se guardan como lineas ordinarias del borrador, cada una
con la cantidad solicitada. El vendedor revisa y elimina las que no correspondan antes
de generar la cotizacion. Las busquedas incompletas y listas recortadas siempre se advierten.
No se crea un lote parcial si algun renglon carece de codigos utilizables, hay cantidades
conflictivas para el mismo codigo o el lote supera 20 codigos distintos. Repeticiones del
mismo codigo con igual cantidad se unifican sin sumar, con aviso y trazabilidad por renglon.

Para una seleccion humana explicita:

```json
{
  "request_id": "e0f7a456-2026-4000-8000-000000000002",
  "cliente_ruc": "0990007020001",
  "pv_id": 2,
  "renglones": [
    {"texto": "bushing galvanizado 1 por 1/2", "cantidad": 1,
     "codigoSeleccionado": "020-000006-I"}
  ]
}
```

El ejemplo no asegura que ese codigo aparezca en ese catalogo. La seleccion debe aparecer
exactamente una vez entre los candidatos de la consulta actual (maximo 5 por renglon).
Si no aparece, refinar el texto o consultar el codigo exacto; nunca se acepta un codigo arbitrario.
Una seleccion explicita puede resolver una busqueda truncada, pero no inventa unidad
comercial: Laravel sigue validando unidad base, precision y elegibilidad. No se envian precios.

## Envio remoto y respuesta

Solo se envia este cuerpo, por GET al path fijo `/cotizaciones/borrador-items`:

```json
{"cliente_ruc":"0990007020001","pv_id":2,"items":[{"codigo":"020-000006-I","cantidad":1}]}
```

El GET mutante es compatibilidad temporal de pruebas, no un patron recomendado.
El nuevo endpoint publico es POST y no convierte GET entrantes en escrituras.
El cliente Apache HttpClient 5 deshabilita reintentos automaticos, redirecciones,
autenticacion, cookies y descompresion. No usa proxy/credenciales del sistema ni
reenvia cabeceras recibidas. Conexion hasta 5 segundos, operacion total 30 segundos
(incluye lectura lenta), respuesta hasta 64 KiB, cancelacion al exceder el limite.
Se agrega solo la dependencia gestionada por Spring Boot, sin cambiar versiones existentes.
No se usa java.net.http.HttpClient para este GET: sus reintentos internos consideran GET idempotente.

- 201: creacion confirmada por respuesta valida de Laravel. `data.borrador` contiene
  id, numero, estado, usuario, punto de venta, cliente e incluidos; `data.resoluciones`
  conserva los candidatos, cantidades y omisiones asumidas.
- 200: la misma instancia devuelve un exito anterior de la misma UUID/contenido,
  con `data.repeticion_local=true`. NO implica idempotencia en Laravel.
- 400: entrada invalida, sin consultas ni escritura.
- 422 con `data.creado=false`: renglon sin codigos utilizables, seleccion explicita invalida, cantidades conflictivas o mas de 20 codigos;
  se incluyen resoluciones para corregir el lote. Tambien se conserva 422 remoto, sin errores internos.
- 409: UUID en curso/incierta o reutilizada con contenido distinto.
- 401/403/429: rechazo remoto, sin transmitir su cuerpo.
- 503: disabled, entorno/destino no permitido, o registro local lleno.
- 502/504: resultado remoto no confirmable, sin afirmar rollback o no creacion.
- 500: error inesperado, sin SQL, direcciones internas ni trazas.

Todas las respuestas incluyen Cache-Control: no-store. Los errores remotos/incertidumbre
indican `reintentable:false`. Ante respuesta incierta revisar Laravel antes de otro intento,
no generar otra UUID para eludir el bloqueo. Un timeout no revierte una creacion ya guardada.

## Proteccion local y limites de duplicados

Una reserva atomica por UUID y fingerprint evita envios simultaneos o repetidos en la
misma instancia. Guarda exitos para reproducirlos y retiene indefinidamente resultados
inciertos. No expulsa entradas: a 1000 UUID retenidas bloquea nuevas operaciones con 503.
Las resoluciones rechazadas antes del envio liberan su reserva para corregirlas.

NO hay garantia exactamente-una-vez: el registro se pierde al reiniciar y no se comparte
entre replicas. Cambiar UUID, repetir a otra instancia o un retry de proxy puede duplicar
borradores. No habilitar balanceo/reintentos de esta ruta ni reiniciar para sortear un 409/503.
Ante registro lleno, conciliar las operaciones antes de cualquier reinicio. Una evolucion
productiva exige POST y clave idempotente persistente en Laravel; no se agrego infraestructura
ni persistencia nueva sin autorizacion. El flujo consumidor debe desactivar doble envio.

## Habilitacion solo en pruebas

Deshabilitado por defecto; no se modificaron archivos de propiedades privados ni servidores.
Tras verificar explicitamente que api-gp, Laravel y ambas conexiones apuntan a pruebas,
un administrador puede configurar:

```properties
reportes.resolucion-borrador.enabled=true
reportes.resolucion-borrador.backend-origin=https://HOST_QA_VERIFICADO
```

El origen es configuracion de servidor, no entrada HTTP, sin path, credenciales, query o fragmento.
Exige perfiles activos no vacios y todos en `local`, `testing`, `qa`, `staging`.
`dev` solo se permite con la excepcion adicional descrita abajo. `prod`,
`production`, desconocidos y mezclas con produccion siempre se bloquean. No renombrar perfiles
compartidos para eludirlo: un nombre de perfil no demuestra el destino de las bases.

La URL que entrego el usuario usa HTTP. Solo para el piloto y con verificacion previa del
destino de pruebas, puede configurarse `backend-origin=http://tuvalapi.cosenco.ec` y
`reportes.resolucion-borrador.allow-insecure-http=true`. Este flag no envia credenciales,
pero RUC y articulos seguirian viajando sin cifrar. HTTPS valido es preferible. No se
habilito este permiso ni se transmitieron datos reales durante las pruebas automatizadas.

La ruta de Laravel carece de autenticacion y actua como usuario 6. Mantener ambos extremos
restringidos por los controles externos de acceso ya existentes; no exponerlos a Internet.
No se crea autenticacion ficticia en api-gp ni se amplian permisos existentes.

## Verificacion

JDK 21 y Maven 3.9.9, `mvn -o -B clean test` y luego `mvn -o -B clean verify`.
Pruebas permanentes ampliadas en los archivos existentes de HTTP del resolver y cliente
borrador; mocks del DAO/servicio y servidores loopback, sin bases ni backend reales.
Cubren seleccion, defaults, precision, campos desconocidos, lotes parciales, concurrencia,
replays locales, corte tras recibir el cuerpo (un solo envio), redirect, timeout de cuerpo,
maximo de bytes y guards de configuracion. Se actualiza inventario de componentes.

Se registro un unico temporal: `src/test/java/tuval/api/web/reportes/resolucionarticulos/ResolucionBorradorTemporalTest.java`,
para el limite de 1000 reservas. Tras pasar se retira solo ese archivo y se repite la suite
limpia. No se eliminan tests preexistentes. Resultados exactos en el PR.

Pendientes antes de una prueba real autorizada: confirmar deployment/path exacto, ambiente
y catalogos, unidad/precision real de cada articulo y permisos del usuario 6 para cliente/PV.
Las pruebas locales no certifican esos puntos. No hay merge, despliegue ni migracion.

Resultados locales finales (2026-11-10): clean test con temporal, 321 casos,
0 fallos, 0 errores, 1 omitido preexistente. Tras retirar solo el temporal,
clean test y clean verify: 320 casos, 0 fallos, 0 errores, 1 omitido en ambas
ejecuciones. JAR construido y verificado sin perfiles privados. Inventario de
tests previos conservado y git diff --check correcto.

## Excepcion DEV de pruebas (continuacion de PR #55)

El usuario confirmo el DEV de pruebas del servicio y su base de pruebas. No se
renombra ningun perfil ni se cambia configuracion de servidores. Para admitir
el perfil literal `dev` hacen falta TODAS estas condiciones del modulo:

```properties
reportes.resolucion-borrador.enabled=true
reportes.resolucion-borrador.dev-test-target-verified=true
reportes.resolucion-borrador.backend-origin=http://tuvalapi.cosenco.ec
reportes.resolucion-borrador.allow-insecure-http=true
```

Estos son valores para que el administrador los revise, NO cambios aplicados
por este PR. Si el destino dispone de HTTPS valido se debe preferir HTTPS y
omitir `allow-insecure-http`. No se envian tokens en ninguno de los dos casos.
El flag `dev-test-target-verified` es una atestacion administrativa posterior a
comprobar ambiente/BD, no una comprobacion automatica de la base Laravel.

La excepcion DEV solo acepta el hostname exacto `tuvalapi.cosenco.ec` y el puerto
estandar 80 HTTP o 443 HTTPS (u omitido). Su cliente fija tambien la conexion a
`172.30.1.7`, el servidor de pruebas comprobado, manteniendo Host/SNI y validacion
TLS normal. Otros destinos, URLs con path/credenciales/query/fragmento y mezclas
dev+prod siguen bloqueados. La fijacion de IP evita que un cambio DNS redirija
esta excepcion a otro servidor. Un cambio del servidor requiere nueva revision;
no basta cambiar la configuracion. No modifica DNS, proxy ni seguridad del SO.

Comprobacion de solo lectura del 2026-11-10 desde SISTEMAS6: DNS del hostname
resolvio a 172.30.1.7 y GET sin cuerpo devolvio 422 con los tres campos requeridos
(cliente_ruc, pv_id, items). No se envio ningun cliente/articulo ni se creo un
borrador. Es consistente con la ruta y guard de entorno Laravel verificados en
codigo, pero no prueba su conexion fisica de BD. Antes de habilitar, confirmar
esa conexion y permisos del usuario 6; las bases de Pruebas y QA son ambientes
distintos y no deben intercambiarse por inferencia.

Regresiones permanentes: DEV sin opt-in, HTTP sin opt-in, host/IP/puerto indebidos,
perfiles mixtos y resolucion de destino fijada. No abren sockets al destino real.

## Descripcion y cliente, sin codigo elegido por el consumidor

`renglones[].texto` contiene la descripcion del usuario. No hace falta codigo,
`codigoSeleccionado`, cantidad o PV para el ejemplo inicial. `request_id` es solo
metadato tecnico obligatorio de proteccion local contra repeticion, no un dato
del articulo. El flujo primero resuelve, luego forma el cuerpo interno de items
y llama a Laravel cuando cada renglon tiene codigos utilizables, sin cantidades
conflictivas ni superar 20 codigos distintos.

Se agrega una regla conservadora de identidad textual: la descripcion completa
puede diferir solo en mayusculas/minusculas y espacios. Debe existir una unica
fila exacta, con codigo GP no vacio y no duplicado en la fuente, y la busqueda de
familia debe estar completa. No admite contradicciones ni atributos estructurales
faltantes; no traduce marcas o materiales crudos. No aplica similitud, fuzzy
matching ni elimina palabras, certificaciones, signos, fracciones o acentos.
Si no cumple, siguen las reglas anteriores del resolver. La creacion puede enviar
las opciones ambiguas como lineas ordinarias, conforme al flujo confirmado abajo.
La respuesta agrega la observacion `comparacion_descripcion_exacta` cuando aplica.

Validacion descriptiva de solo lectura en DEV: la familia BUSHING devolvio 212
filas mediante listado-items (limite 500), y la descripcion inicial aparecio una
sola vez para 020-000006-I. Un test temporal usa esa captura fuera del repositorio
para ejecutar el resolver y orquestador reales con el creador simulado. No hace
una creacion real ni demuestra que el cambio ya este desplegado. Se elimina el
temporal antes del commit y se repiten las suites limpias. La captura no se publica.

Las regresiones permanentes cubren literal unico, case/espacios, duplicados de
descripcion/codigo, fuente truncada, errores tipograficos, omisiones y contradicciones.
La prueba existente de no descartar filas inciertas conserva esa propiedad para
especificaciones interpretadas no literales; la nueva regla de identidad literal
se prueba por separado.

Resultados de la continuacion DEV/descripciones: captura temporal verificada
con 326 casos, 0 fallos, 0 errores, 1 omitido. Se retiro solo DescripcionRealTemporalTest.java
y se conservo ademas una regresion HTTP completa con fixture local; clean test y
clean verify finales: 326 casos, 0 fallos, 0 errores, 1 omitido preexistente.
No se crearon borradores reales ni se aplico la configuracion descrita.

## Opciones como lineas del borrador (flujo confirmado)

El usuario confirmo que, si no hay coincidencia unica, se envian los codigos opcion
al mismo endpoint de creacion. No se requiere seleccionar manualmente uno antes de
crear. Laravel recibe exclusivamente codigo/cantidad por cada opcion, sin campos nuevos.
`CHECK_ALTERNATIVO` existe en otros flujos, pero esta API no lo admite ni guarda grupos.
No se cambia tuvalBackend ni se supone que el vendedor vea las opciones agrupadas.
Todas son lineas normales y deben revisarse antes de generar la cotizacion.

La respuesta de api-gp agrega:
- `opciones_por_renglon`: indice, codigos efectivamente preparados, cantidad por linea,
  origen (`identificado`, `seleccion_explicita` u `opciones`) y `listaCompleta`.
- `advertencias`: lineas ordinarias pendientes de revision; busqueda incompleta, lista
  recortada o candidatos sin codigo utilizable; codigos repetidos unificados; conflictos
  y limite del lote cuando corresponda.

Las advertencias se conservan tambien en una repeticion local. `resoluciones` sigue
mostrando estado ambiguo, datos faltantes, truncamientos y observaciones originales.
Una sola opcion utilizable de una busqueda incompleta no se declara identificacion unica.
Los candidatos sin codigo GP utilizable no se inventan: se informa su exclusion y la lista
queda incompleta. Si no queda ninguno, no se envia la operacion. No se recorta a 20 para
forzar un borrador: mas de 20 codigos distintos devuelve 422 sin ninguna escritura.

La cantidad solicitada se repite en cada linea opcion; no representa una compra de todos
los candidatos. Mismo codigo e igual cantidad se envia una sola vez y aparece ligado a
cada renglon de origen; cantidades distintas para ese codigo requieren correccion, no suma.
Laravel puede rechazar atomicamente el lote si algun codigo no cumple sus validaciones
actuales de actividad, permisos, unidad o precision.

La asociacion renglon/opciones queda en la respuesta de esta operacion, no persistida
como agrupacion en Laravel. No hay aprendizaje automatico ni captura de la validacion final
implementados: registrar la eleccion posterior y aprender de ella es una fase pendiente.
No se infiere una seleccion validada por el mero hecho de crear o consultar un borrador.


Verificacion final del flujo de opciones: clean test y clean verify, 331 casos,
0 fallos, 0 errores y 1 omitido preexistente en cada ejecucion. Se comprobaron
lista incompleta, maximo 20 codigos sin recorte silencioso, opciones sin codigo,
duplicados dentro y entre renglones, cantidades conflictivas y repeticion local.
No quedaron tests temporales, no se eliminaron tests existentes y el JAR no incluye
application-dev.properties, application-qa.properties ni application-prod.properties.
