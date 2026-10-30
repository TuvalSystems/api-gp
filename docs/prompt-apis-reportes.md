# Prompt para crear APIs de reportes de SQL Server

## Revisión del proyecto

El proyecto usa Java 21, Spring Boot 3.4.4 y Maven. Se organiza por funcionalidad,
con capas `controller`, `service`, `dao`, `dto`, `request` y `entity` cuando hacen
falta; la base se separa dentro de cada módulo. Ver [estructura-modular.md](estructura-modular.md).

Se establece el nuevo módulo `tuval.api.web.reportes.<funcionalidad>` y las rutas
`/api/reportes/<reporte>` para todas las APIs creadas con este prompt.

| Conexión obligatoria | JDBC | JDBC con parámetros nombrados | Gestor transaccional | Unidad JPA |
|---|---|---|---|---|
| `tuval` | `tuvalJdbcTemplate` | `tuvalNamedParameterJdbcTemplate` | `tuvalTransactionManager` | `tuvalPU` |
| `dynamics` | `dynamicsJdbcTemplate` | `dynamicsNamedParameterJdbcTemplate` | `dynamicsTransactionManager` | `dynamicsPU` |

Dynamics es `@Primary`: omitir el `@Qualifier` puede dirigir una consulta a la
conexión equivocada. La base se fija en código; no se selecciona desde HTTP.
El catálogo físico depende del ambiente y debe verificarse.

Fuentes revisadas bajo `src/main/java/tuval/api/web`:

- `config/TuvalJpaConfig.java` y `config/DynamicsJpaConfig.java`: beans y escaneo JPA.
- `ventas/cotizaciones/controller/QuoteController.java`: rutas y respuestas comunes.
- `ventas/cotizaciones/dao/dynamics/QuoteRecordDao.java`: consultas parametrizadas.
- `ventas/ordenCompra/dao/dynamics/PurchaseOrderDao.java`: parámetros explícitos
  con `SimpleJdbcCall`; su SP de escritura no es un ejemplo de reporte.
- `common/response/ResponseJsonUtils.java`: contrato `success`, `message`, `data`.

También se revisaron `ModuleDatabaseWiringTest` y `PersistenceConfigurationTest`:
verifican conexiones e inventarios exactos de componentes y paquetes JPA.

## Prompt reutilizable

Copiar el bloque siguiente y completar conexión, definición, entradas y salidas.
Usar `ninguna` cuando no haya entradas y `según definición` para salidas que se
puedan deducir del SQL. Para implementar y publicar un PR,
ChatGPT/Codex debe disponer de acceso al repositorio y herramientas de escritura en
GitHub. Sin ese acceso debe entregar la propuesta y sus pasos pendientes.

```text
Crea una API de reporte en git@github.com:TuvalSystems/api-gp.git siguiendo
docs/prompt-apis-reportes.md y las instrucciones aplicables del repositorio.
Implementa, prueba y abre un PR hacia develop; solicita revisión a los
responsables definidos en .github/CODEOWNERS. No hagas merge.

Conexión: [tuval | dynamics]
Entradas: [filtros/parámetros, o ninguna]
Salidas: [campos esperados, o según definición]
Definición SQL de la tabla, vista o SP:
[pegar aquí]
```

## Reglas que el agente debe tomar del repositorio

Deduce del SQL el tipo de objeto, esquema, nombre y tipos de los datos cuando
sean inequívocos. Deriva nombres de clases, funcionalidad y ruta a partir del
objeto, respetando las convenciones existentes y evitando colisiones. No pidas al
usuario que repita estos detalles. Las entradas son parámetros de consulta,
no operaciones de escritura; las salidas son el contrato del reporte.

Para tablas/vistas sin filtros pedidos, usa solo los controles de paginación.
Por defecto, página 0 y tamaño 100, con máximo 1000 y orden por una clave única
verificada. Usa timeout local de 30 segundos. Para SP respeta los límites y
parámetros reales. Si no existe orden determinista o límite aplicable, o el SQL
no permite conocer entradas/salidas, pide únicamente el dato faltante.
Documenta las decisiones derivadas en el PR.

Estas reglas se aplican junto con la estructura actual del proyecto:

1. REVISIÓN
Lee los AGENTS.md aplicables, pom.xml, docs/estructura-modular.md,
docs/prompt-apis-reportes.md, ambas configuraciones JPA, ResponseJsonUtils,
un controller/service/DAO comparable y las pruebas de arquitectura actuales.
Revisa Git y preserva cambios ajenos. No inventes objetos, columnas, claves,
relaciones, tipos ni firmas de SP. Si falta conexión, definición SQL, contrato
de salida o una regla esencial, solicita esos datos antes de implementar esa parte.
Si no hay acceso a SQL Server, usa la definición proporcionada e indica que falta
validación real en QA. No leas ni publiques credenciales para resolverlo.

2. UBICACIÓN OBLIGATORIA
Todo el código de producción del reporte debe quedar en:
src/main/java/tuval/api/web/reportes/<funcionalidad>/
  controller/<Nombre>ReporteController.java
  service/<base>/<Nombre>ReporteService.java
  dao/<base>/<Nombre>ReporteDao.java
  dto/<Nombre>ReporteDto.java
  request/<Nombre>ReporteRequest.java (solo cuando haga falta)
  entity/<base>/... (solo si se justifica JPA)
<base> debe ser tuval o dynamics. No crees capas vacías ni ubiques el reporte
en ventas, bodega, clientes, cobranzas, transacciones o paquetes globales de DAO.
Las pruebas van en src/test/java/tuval/api/web/reportes/<funcionalidad>/.
Usa nombres de componentes únicos para evitar colisiones de beans.
Fuera del módulo solo se permiten documentación, inventarios de arquitectura y
los cambios mínimos de escaneo JPA necesarios. No refactorices módulos ajenos.

3. CONEXIÓN Y PERSISTENCIA
Usa beans existentes e inyección por constructor con @Qualifier explícito:
tuval -> tuvalJdbcTemplate o tuvalNamedParameterJdbcTemplate;
dynamics -> dynamicsJdbcTemplate o dynamicsNamedParameterJdbcTemplate.
No dependas de @Primary ni agregues DataSource, credenciales o propiedades.
No permitas elegir base, esquema, objeto ni SQL libre desde la petición HTTP.
Prefiere JDBC y DTO para reportes: una tabla/vista no exige una entidad JPA.
No agregues @Entity ni @Id ficticios a resultados de consultas o SP.
Si necesitas JPA, verifica la identidad real, registra exclusivamente los paquetes
dao/entity de la base elegida en su configuración y actualiza sus inventarios y
pruebas. Nunca registres el padre reportes en ambas conexiones.
Si usas @Transactional, especifica transactionManager de la base seleccionada
y readOnly = true; readOnly no impide escrituras ni prueba que un SP sea de lectura.
Cada reporte usa una sola conexión. Si hacen falta ambas, solicita definir fuentes y
combinación y usa DAO separados por base. No supongas joins entre catálogos ni
transacciones distribuidas.

4. SQL Y SP
Estas APIs son de consulta: no generes CRUD ni ejecutes SP con escrituras
persistentes. Si no puedes verificar que el SP es de reporte, pide su definición.
Selecciona columnas explícitas y esquema verificado. Enlaza valores con parámetros
JDBC, nunca concatenes datos recibidos. Fija identificadores SQL en código.
Para orden configurable, traduce solo opciones de una lista cerrada a columnas
constantes. Implementa filtros, orden estable y límites/paginación acordados.
Para SP usa SimpleJdbcCall o CallableStatement con firma y tipos explícitos;
en SimpleJdbcCall desactiva inferencia de metadatos y declara IN/OUT/INOUT, retorno
y RowMapper de cada result set según el contrato real. No inventes columnas ni
agregues filtros/paginación que el SP no soporta. Si devuelve resultados sin límite,
acuerda una solución antes de publicar; recortar en memoria no es paginación SQL.
Configura timeout local sin modificar los templates compartidos. No copies catálogos
hardcodeados de otros DAO sin verificar necesidad y ambiente.
Mapea decimal/moneda a BigDecimal y fechas a java.time según su tipo SQL.
Conserva NULL: no conviertas un NULL numérico en cero al usar ResultSet.

5. HTTP Y CAPAS
Usa @RestController y @RequestMapping con produces = "application/json" en
/api/reportes/<slug>. Prefiere GET para filtros simples; usa POST /consultar con
cuerpo para filtros complejos y justifícalo. No uses cuerpo en GET.
El controller valida y delega al service; el service aplica reglas y llama al DAO;
solo el DAO contiene SQL/acceso a datos. No expongas entidades como entradas HTTP.
Usa Jakarta Validation y @Valid cuando corresponda; valida fechas, longitudes,
límites y orden antes de consultar.
Conserva ResponseEntity<Map<String,Object>> y reutiliza ResponseJsonUtils:
- éxito 200: success, message, data;
- lista vacía 200: data = [];
- entrada inválida 400: success = false y message (buildNoFound devuelve 400);
- error interno 500: buildError con mensaje público genérico.
Si necesitas manejar errores de validación, hazlo localmente al módulo sin cambiar
APIs existentes. No envíes e.getMessage() al cliente: puede revelar SQL o servidores.
Respeta el mecanismo de acceso existente; no inventes autenticación ni amplíes permisos.

6. VERIFICACIÓN Y ENTREGA
Agrega pruebas sin conexiones reales para verificar selección de conexión entre
ambos beans, parámetros y tipos SQL, mapeo con NULL, éxito, lista vacía, validación
400 sin llamada al DAO y errores 500 sin detalles SQL. Para SP prueba firma,
OUT/retorno y conjuntos de resultados pertinentes.
Actualiza src/test/resources/architecture/components.txt al agregar componentes:
ModuleDatabaseWiringTest compara el inventario exacto de toda la aplicación.
Si cambias escaneo JPA, actualiza <base>-repositories.txt y <base>-entities.txt
y PersistenceConfigurationTest según corresponda.
Ejecuta mvn clean test (o mvnw.cmd clean test en Windows) y git diff --check.
No declares pruebas exitosas si no se ejecutaron o fallaron. Los mocks no validan
SQL ni permisos reales; informa la comprobación pendiente en QA.
Documenta ruta, base, origen, filtros, JSON, límites y pendientes de validación.

Crea rama codex/reporte-<slug> desde origin/develop actualizado. Incluye únicamente cambios
de esta tarea, haz commit y push al repositorio indicado y abre un pull request
hacia develop. Solicita revisión a los responsables de .github/CODEOWNERS con
acceso de escritura, excluyendo al autor del PR (no puede aprobar su propio PR).
Si un usuario no existe o no tiene permiso, informa el bloqueo sin sustituirlo
por otra cuenta. Describe comportamiento, conexión, objeto SQL, pruebas y QA.
No hagas merge ni despliegues. Si faltan herramientas o permisos, entrega los
archivos preparados y explica el bloqueo; no afirmes crear un PR sin URL verificable.
Entrega resumen, archivos, endpoint, petición/respuesta, pruebas y enlace del PR.

## Aprobación de pull requests

Los responsables designados son `@josemg23`, `@devtuval` y `@tuvalSystem`.
Mantenerlos juntos en la misma regla de `.github/CODEOWNERS`: las reglas repetidas
para el mismo patrón se reemplazan, no se acumulan.

La aprobación de al menos un responsable distinto del autor es la política para
estos reportes. CODEOWNERS no concede permisos ni impide por sí solo hacer merge:
para exigir esa aprobación en GitHub, `develop` debe tener una regla de protección
o ruleset con revisión obligatoria de code owners y sin bypass aplicable.
Esto no exige que los tres aprueben ni garantiza que solo ellos puedan hacer merge.
Ver la [documentación de GitHub sobre code owners](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners).

## Ejemplos de selección (fichas parciales)

- Vista de Tuval: `Conexión: tuval`, `Tipo: VISTA`, nombre verificado
  `esquema.nombre`. DAO en `reportes/<funcionalidad>/dao/tuval` con
  `tuvalJdbcTemplate` o `tuvalNamedParameterJdbcTemplate`.
- SP de Dynamics: `Conexión: dynamics`, `Tipo: SP`, nombre verificado, firma y
  columnas. DAO en `reportes/<funcionalidad>/dao/dynamics` con `dynamicsJdbcTemplate`.

Estos ejemplos no identifican objetos reales ni sustituyen la ficha completa.

## Alcance

Este cambio entrega el prompt y las instrucciones del módulo. No agrega endpoints
concretos porque aún no se proporcionó una tabla, vista o SP con su contrato.
El cumplimiento debe comprobarse con pruebas y revisión del PR; un prompt no
constituye por sí solo un control automático ni un generador en tiempo de ejecución.
