# Inventario / varianza: consulta de varianzas en trabajo

## Organización actual

La consulta se ubica en `src/main/java/tuval/api/web/reportes/inventario/varianza/`:

```text
reportes/inventario/varianza/
├── controller/VarianzaController.java
├── service/dynamics/VarianzaService.java (interfaz)
├── service/dynamics/imp/VarianzaServiceImp.java
├── dao/dynamics/VarianzaDao.java
├── dto/VarianzaDto.java
└── request/VarianzaRequest.java
```

Se retira la ruta `/api/reportes/listado-varianzas-trabajo`. Los consumidores deben
usar `/api/reportes/inventario/varianza/listado-trabajo`; se conservan el parámetro
`NumeroDocumento`, la conexión, el procedimiento y los campos de respuesta.
La verificación local descrita al final corresponde a la implementación original,
anterior a esta reorganización.

Validación de la reorganización (2026-11-11): `mvn -o clean verify` finalizó
correctamente con Java 21. Esta copia del repositorio no contiene `src/test`,
por lo que Maven no ejecutó pruebas. Se verificaron las cinco clases nuevas en
el JAR y la ausencia del paquete antiguo, además de comparar el código con el
original: solo cambian paquetes, nombres de clases y ruta HTTP. No se ejecutó
el SP contra SQL Server ni se desplegó esta reorganización.

`GET /api/reportes/inventario/varianza/listado-trabajo` expone la consulta del procedimiento existente
`Dynamics2021.dbo.spInv60` mediante `dynamicsJdbcTemplate`. api-gp es el
intermediario entre tuvalBackend y Dynamics/Tuval, por lo que este reporte sigue
la ubicacion y el contrato HTTP de los reportes existentes.

## Origen y conexion

- Llamada fija: `{? = call dbo.spInv60(@NumeroDocumento = ?)}`.
- Entrada SQL: `@NumeroDocumento varchar(50) = NULL`.
- El primer marcador es el estado de retorno entero de SQL Server; no hay otros
  parametros OUT ni INOUT en la definicion proporcionada.
- Una sola conexion, seleccionada por `@Qualifier("dynamicsJdbcTemplate")`.
  El catalogo esperado es `Dynamics2021`; el procedimiento consulta las tablas
  `TUVAL.dbo.IV10000`, `IV10001`, `IV00101` e `IV00102` en la misma instancia.
- Se utiliza el SP tal como fue compartido: filtro `IVDOCTYP = 2`, inventario
  `RCRDTYPE = 2`, `INNER JOIN` y orden por `IVDOCNBR, LNSEQNBR`. No se modifica el
  procedimiento ni se duplican sus joins en Java.

El DAO sigue el patrón de los DAO de artículos, cobranzas y órdenes de compra:
`SimpleJdbcCall` configurado en el constructor, `MapSqlParameterSource` para las
entradas y `returningResultSet("data", varianzaMapper())` para mapear las filas.
Declara esquema `dbo`, procedimiento `spInv60`, retorno `INTEGER` y parámetro
`@NumeroDocumento` de tipo `VARCHAR`, con enlace por nombre y sin inferencia de
columnas del procedimiento desde metadatos. Spring administra los recursos JDBC.
Un retorno distinto de cero, retorno nulo, ausencia de
resultado o un segundo conjunto de resultados produce un error; nunca una
respuesta exitosa parcial. Se admiten conteos intermedios del controlador JDBC.

La configuración local usa un `JdbcTemplate` con el mismo DataSource de Dynamics,
timeout de 30 segundos, fetch size de 100 y max rows de 0. No agrega conexiones
configuradas ni altera el bean compartido.

Validación del ajuste del DAO (2026-11-11): se ejecutó una comprobación temporal
con JDBC simulado y el `SimpleJdbcCall` real, verificando firma y enlace de
parámetros, documento omitido/vacío/espacios, lista vacía, nulos, fecha, decimales
negativos, duplicados, 1505 filas sin recorte, conteos intermedios, cierre del
statement, configuración compartida intacta y rechazo de retorno fallido/nulo,
resultado ausente o adicional. La comprobación temporal se retiró tras validar.
`mvn -o clean verify` compila y empaqueta el proyecto; no hay suite de pruebas en
esta copia. La validación con SQL Server real sigue pendiente.

## Service y diagnóstico de errores

`VarianzaService` define el contrato y `service/dynamics/imp/VarianzaServiceImp`
implementa la validación y delegación al DAO, con `@Service`,
`@RequiredArgsConstructor` e inyección por constructor. Se sigue el patrón de
interfaz/implementación usado en cobranzas y devoluciones.

Al usar `withNamedBinding()` en SQL Server, el nombre SQL se declara como
`@NumeroDocumento` tanto en `SqlParameter` como en `MapSqlParameterSource`.
El nombre HTTP sigue siendo `NumeroDocumento`. La declaración anterior sin `@`
generaba `NumeroDocumento = ?` dentro de la llamada; se corrigió para generar
`@NumeroDocumento = ?`. El driver JDBC conserva ese prefijo al traducir a `EXEC`.
Esta corrección no confirma por sí sola la causa del fallo observado en el entorno.

El controller registra la excepción completa con SLF4J y mantiene el mensaje
genérico de error en el JSON. Así se pueden distinguir errores de SQL, permisos,
conexión y mapeo sin exponerlos en la respuesta HTTP.

Validación temporal del flujo (2026-11-11): contexto Spring real con ambos beans
JDBC y conexión simulada, una implementación del service, delegación
controller/service/DAO, documento opcional y vacío, decimales y nulos, respuesta
200 con datos, validación 400 sin consultar el DAO y error 500 controlado con log.
También se comprobó con el traductor real del driver Microsoft SQL Server que la
llamada corregida produce `EXEC ? = dbo.spInv60 @NumeroDocumento = ?`.
La comprobación temporal se retiró después de validar. No había servicio local
escuchando en el puerto 8006 y no se ejecutó el SP en una base real.

## Comprobación integrada posterior (2026-11-11)

Se consultó `VAR0000617` usando el DAO actual y el perfil DEV local, sin modificar
datos ni mostrar credenciales. SQL Server confirmó el catálogo `Dynamics2021`,
la visibilidad de `dbo.spInv60` y permiso `EXECUTE = 1`. El DAO devolvió 17 filas.
La petición HTTP al mismo documento en `http://localhost:8006` devolvió 200 con
datos; en `http://172.30.1.25:8006` devolvió 500 con el mensaje genérico.
La diferencia entre instancias requiere comprobar versión y configuración del
servidor, además de su excepción interna; no demuestra que ambas ejecuten el
mismo código ni usen la misma configuración. No se desplegaron cambios durante
esta comprobación. El diagnóstico temporal se retiró.
Las notas de permisos pendientes en QA más abajo corresponden a ese otro perfil
y no se sustituyen con esta validación DEV.

## Peticion

```http
GET /api/reportes/inventario/varianza/listado-trabajo?NumeroDocumento=VAR0000617
Accept: application/json
```

| Parametro de consulta | Obligatorio | Regla |
|---|---|---|
| `NumeroDocumento` | No | Longitud maxima de 50 caracteres, como la entrada del SP |

El nombre HTTP es `NumeroDocumento`, respetando mayusculas. No se usa cuerpo en
GET. El valor se enlaza sin transformarlo; el SP aplica `TRIM` y `NULLIF`.
Omitir el parametro envia SQL `NULL`; enviarlo vacio o solo con espacios tambien
consulta todos los documentos segun el procedimiento. No enviar la palabra
literal `NULL` para representar un nulo: seria un numero de documento.

```http
GET /api/reportes/inventario/varianza/listado-trabajo
GET /api/reportes/inventario/varianza/listado-trabajo?NumeroDocumento=
```

Ambas peticiones anteriores tienen alcance completo. Usar un documento concreto
en las consultas ordinarias para evitar solicitar todos los registros por
omision accidental del filtro.

## Respuesta

Se conservan exactamente los 17 nombres de columna, todos los registros y su
orden de llegada, incluyendo duplicados. No se agrupa, deduplica, redondea ni
recalcula ningun valor. Los `NULL` se incluyen explicitamente como `null` y se
conservan los ceros y las cantidades negativas.

| Campos JSON | Tipo Java / JSON |
|---|---|
| `IVDOCTYP` | `Integer` / numero o `null` |
| `IVDOCNBR`, `BACHNUMB`, `ITEMNMBR`, `ITMGEDSC`, `ITMSHNAM`, `ITEMDESC`, `TRXLOCTN`, `Reason_Code` | `String` / texto o `null` |
| `DOCDATE` | `LocalDate` / `YYYY-MM-DD` o `null`; el SP hace `CAST(... AS DATE)` |
| `LNSEQNBR`, `QTYBSUOM`, `TRXQTY`, `UNITCOST`, `EXTDCOST`, `CURRCOST`, `EXISTENCIA` | `BigDecimal` / numero o `null` |

Se lee `LNSEQNBR` como `BigDecimal` para conservar el valor numerico del driver
sin asumir una escala SQL que no se proporciono. Los importes y cantidades
tampoco se convierten a `double` ni a enteros. Los consumidores deben usar tipos
decimales cuando requieran conservar la precision del JSON.

`CURRCOST` representa el costo promedio segun la convencion confirmada por el
usuario. `EXISTENCIA` ya viene calculada por el SP como
`IV00102.QTYONHND - IV00102.ATYALLOC` para el articulo y la bodega. La API solo
informa estos valores: no aprueba, contabiliza ni valida costos o inventario.

Ejemplo sintetico, sin datos consultados en SQL Server:

```json
{
  "success": true,
  "message": "Respuesta exitosa",
  "data": [{
    "IVDOCTYP": 2,
    "IVDOCNBR": "VAR0000617",
    "DOCDATE": "2026-11-05",
    "BACHNUMB": "PRUEBA",
    "ITEMNMBR": "TEST-001",
    "ITMGEDSC": null,
    "ITMSHNAM": null,
    "ITEMDESC": "Articulo de prueba",
    "LNSEQNBR": 16384.00000,
    "QTYBSUOM": 1.00000,
    "TRXLOCTN": "BODEGA",
    "TRXQTY": -2.00000,
    "UNITCOST": 0.00000,
    "EXTDCOST": 0.00000,
    "Reason_Code": null,
    "CURRCOST": 48.78538,
    "EXISTENCIA": 12.123456
  }]
}
```

- `200`: contrato `success`, `message`, `data`; sin filas devuelve `data: []`.
- `400`: mas de 50 caracteres; `success: false` y mensaje publico de parametros
  invalidos, antes de acceder al DAO.
- `500`: fallo de consulta o resultado inesperado; `success: false` y
  `message: "No se pudo consultar el reporte spInv60"`, sin SQL ni detalles del
  servidor en la respuesta.

## Limites y acceso

El SP no ofrece paginacion ni limite de filas. Se conserva expresamente su
comportamiento de consulta completa con `NULL` o vacio. No hay tope inventado,
recorte en memoria, filtros adicionales ni parametros de paginacion.
Se fija `setMaxRows(0)` en la llamada para desactivar cualquier tope heredado del
template, sin modificar la configuracion compartida.
`fetchSize = 100` es solo una sugerencia de lectura al driver y no limita el
resultado. Toda la respuesta se materializa en memoria.

La llamada tiene un timeout local de 30 segundos, sin alterar el template
compartido. Ese timeout limita la ejecucion JDBC; no limita el volumen de filas
ni garantiza un plazo total de respuesta HTTP o uso acotado de memoria.

Se mantiene el mecanismo de acceso existente; este modulo no agrega
autenticacion ni permisos. Antes de publicar, acordar el manejo de consultas
masivas (capacidad, acceso externo y, si hace falta, un cambio acordado del SP
para filtrar o paginar).

El usuario autorizo publicar en DEV. Se sigue el flujo de
`DEPLOYMENT.md`: PR hacia `develop`, revision requerida y posterior despliegue
automatico a `srv-docker00`, contenedor `apigp`, perfil `dev`. No se cambia el
flujo de despliegue ni se publica a produccion. Tras completar ese flujo, la URL
esperada para la prueba es:

```text
http://172.30.1.25:8006/api/reportes/inventario/varianza/listado-trabajo?NumeroDocumento=VAR0000617
```

## Validacion pendiente en QA

El usuario confirmo que la conexion Dynamics del perfil QA apunta al servidor
donde creo el SP. El 2026-11-05 a las 16:14 UTC se abrio esa conexion y se
comprobo el catalogo `Dynamics2021`. Se consultaron unicamente metadatos, sin
ejecutar el procedimiento ni modificar datos. SQL Server devolvio:

| Comprobacion de `dbo.spInv60` | Resultado con la cuenta QA configurada |
|---|---|
| `OBJECT_ID(..., 'P')` | `NULL`; procedimiento no visible |
| `HAS_PERMS_BY_NAME(..., 'EXECUTE')` | `0` |
| `HAS_PERMS_BY_NAME(..., 'VIEW DEFINITION')` | `0` |
| `OBJECT_DEFINITION(...)` | `NULL` |

La prueba integrada se detuvo en la comprobacion previa de la definicion. Estos
resultados no permiten afirmar que el SP no exista: puede no estar visible para
esa cuenta. Se requiere verificar su existencia y los permisos de ejecucion y
lectura de definicion con el administrador de QA. No se probaron filas ni tipos
reales de salida y no se intento usar otra cuenta ni cambiar permisos.

Antes de una validacion integrada se debe comprobar la version desplegada de
`spInv60`, sus tipos y columnas, y los permisos para ejecutarlo y consultar
`TUVAL` mediante la conexion Dynamics. La prueba temporal de QA se retiro; las
pruebas normales del repositorio siguen sin abrir conexiones reales.

En ese entorno autorizado, comprobar un documento con ingresos, otro con egresos,
un documento inexistente, nulos y decimales reales, el orden y el volumen de la
consulta completa. El `INNER JOIN` y los `NOLOCK` del SP se conservan: la API
refleja sus filas y no garantiza una instantanea consistente del inventario.

## Verificacion local

Se uso JDK 21 y Maven 3.9.9 de la cache del equipo, con dependencias offline.
El wrapper intento descargar Maven y no pudo resolver el servidor; se ejecuto
directamente la misma version ya disponible. Para evitar el bloqueo de la
autoasociacion de Mockito en el sandbox, se precargo el agente Byte Buddy 1.15.11
en la JVM de pruebas, sin cambiar `pom.xml` ni la configuracion de produccion:

```text
mvn -o -DargLine=-javaagent:<ruta-al-byte-buddy-agent-1.15.11.jar> clean test
```

La suite con pruebas temporales completo 97 casos, sin fallos ni errores y con
un caso previamente deshabilitado (`WebApplicationTests`, requiere BD). Los
27 casos nuevos de `Inv60ReporteDaoTest` e `Inv60ReporteHttpTest` comprobaron:

- Seleccion de Dynamics incluso con Tuval como `@Primary`, firma JDBC, retorno,
  tipo `VARCHAR`, valores parametrizados y cierre de recursos.
- Filtro omitido, vacio, espacios y documento concreto; longitud de 50 aceptada
  y 51 rechazada antes del DAO, incluyendo llamadas directas al servicio.
- Los 17 campos, fecha ISO, precision decimal, `NULL`, ceros, negativos y textos
  sin normalizacion adicional; duplicados y orden conservados.
- Una respuesta de 1505 filas sin recorte, timeout local y limpieza de un tope
  heredado sin alterar el template compartido.
- HTTP 200 con datos o lista vacia, 400 de validacion y 500 generico sin detalles
  internos; rechazo de retornos fallidos, resultados ausentes o adicionales y
  manejo de conteos JDBC intermedios.
- Ruta solicitada `/api/reportes/listado-varianzas-trabajo`; la ruta tecnica
  anterior devuelve 404 sin consultar el DAO.

Segun `docs/prompt-apis-reportes.md`, esos dos archivos de pruebas se retiraron
despues de validar, sin eliminar ni cambiar pruebas preexistentes. Se conserva
la actualizacion de `architecture/components.txt` con los tres componentes del
reporte, verificados por `ModuleDatabaseWiringTest`.

Tras retirar los temporales, la ejecucion `clean verify` del estado final
completo 70 casos, sin fallos ni errores y con el mismo caso deshabilitado.
Se genero el JAR y se verifico que incluye la ruta solicitada y las cinco clases
del reporte, sin la ruta anterior ni perfiles privados dev, qa o prod.
`git diff --check` tambien paso. Las pruebas con mocks no validan SQL Server,
permisos, rendimiento ni datos reales; la comprobacion integrada sigue pendiente.

La prueba integrada temporal de QA fallo en la precondicion de visibilidad y
permisos del SP descrita arriba; no forma parte de la suite final ni se ejecuta
contra una base durante CI.
