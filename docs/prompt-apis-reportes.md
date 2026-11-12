# Prompt para crear o ampliar APIs de consulta

api-gp es un microservicio que Laravel consume: **Laravel → api-gp → SQL Server**.
Laravel administra cotizaciones, borradores y sus procesos de negocio. Estas APIs
consultan y devuelven información; no llaman a Laravel ni crean borradores.

## Estructura y reutilización obligatorias

El proyecto usa Java 21, Spring Boot 3.4.4 y Maven. Revisar primero el código actual
y [estructura-modular.md](estructura-modular.md). Organizar los reportes por área
de negocio y funcionalidad, no por nombre de vista, SP, endpoint o prompt generado.

Ejemplo vigente:

```text
reportes/inventario/articulos/
├── controller/ArticulosController.java
├── service/dynamics/ArticulosService.java
├── service/dynamics/imp/ArticulosServiceImp.java
├── dao/dynamics/ArticulosDao.java
├── dto/
└── request/

reportes/inventario/varianza/
├── controller/VarianzaController.java
├── service/dynamics/VarianzaService.java
├── service/dynamics/imp/VarianzaServiceImp.java
├── dao/dynamics/VarianzaDao.java
├── dto/
└── request/
```

- Buscar la funcionalidad existente antes de crear carpetas o clases.
- Dentro del mismo módulo y conexión, agregar métodos al controller, interfaz de
  service, implementación y DAO existentes. **No crear uno de cada uno por API**.
- Artículos comparte `ArticulosController`, `ArticulosService`,
  `ArticulosServiceImp` y `ArticulosDao` para maestro, listado y resolución.
- Crear DTO/request específicos cuando sus datos sean distintos; no forzar todas
  las respuestas o entradas en una sola clase ni añadir campos sin relación.
- Una nueva funcionalidad justificada puede necesitar sus propias capas. El agente
  decide el área y la funcionalidad mediante el análisis descrito abajo; no delega
  al usuario la elección de carpetas.
- No crear capas vacías, clientes HTTP ni infraestructura para una consulta SQL.
  Las reglas de interpretación pueden separarse en un helper dentro del módulo.
- No mover ni refactorizar módulos ajenos para implementar un endpoint.

Las clases se nombran por su función: `ArticulosDao`, `VarianzaService`, etc.
Evitar nombres técnicos como `Inv60Reporte...` y clases por cada endpoint.

### Clasificación automática por función de negocio

Antes de escribir, el agente analiza la definición SQL, tablas/vistas/SP usados,
joins, filtros, agregaciones, entradas y campos de salida. Deduce qué información
ofrece o qué operación realiza y la compara con las funcionalidades del proyecto.
El nombre técnico del objeto es solo una pista, no determina el módulo.

1. Identificar el área: inventario, ventas, cobranzas, clientes u otra sustentada
   por la consulta y las convenciones existentes.
2. Identificar la funcionalidad y buscar su controller, service y DAO actuales.
   Reutilizarlos aunque el endpoint o el SP tengan otro nombre.
3. Ubicar la consulta en `reportes/<area>/<funcionalidad>` y añadir un método con
   nombre de negocio. Crear capas únicamente si la funcionalidad aún no existe.
4. Explicar brevemente en la entrega la ubicación elegida y qué datos del SQL
   respaldan esa clasificación. No pedir al usuario que decida la carpeta.

Ejemplos: `spInv60`, que consulta varianzas, pertenece a
`reportes/inventario/varianza`; el maestro, listado y resolución de artículos
pertenecen a `reportes/inventario/articulos`; `spVen11`, que compara ventas y
costos por sucursal, pertenece a `reportes/ventas/sucursales`.
Este último usa `SucursalesController`, `SucursalesService`, `SucursalesServiceImp`
y `SucursalesDao`, con `GET /api/reportes/ventas/sucursales/comparativo`.

Si la clasificación tiene varias opciones razonables, elegir la más coherente
con el proyecto y explicar la decisión. Preguntar solo por información funcional
indispensable que no esté en las fuentes y cuya ausencia impida definir el
comportamiento o la conexión; una preferencia de carpeta no es ese bloqueo.
Esta decisión la realiza el agente durante la generación, sin añadir IA externa,
API keys ni dependencias de IA a la aplicación.

## Prompt reutilizable

Copiar el siguiente bloque y completar únicamente los datos de la consulta:

```text
Crea o amplía una API de consulta en git@github.com:TuvalSystems/api-gp.git
siguiendo docs/prompt-apis-reportes.md y las instrucciones del repositorio.

Contexto de negocio opcional: [si se conoce; el agente deduce área y funcionalidad]
Conexión: [tuval | dynamics]
Entradas: [filtros/parámetros, o ninguna]
Salidas: [campos esperados, o según definición SQL]
Definición SQL de la tabla, vista o SP:
[pegar aquí]

Implementa y valida. No crees controller, service ni DAO por endpoint: reutiliza
las capas de la funcionalidad. Decide su ubicación analizando lo que consulta o
hace el SQL y los módulos existentes; no pidas al usuario elegir la carpeta.
Los DTO de respuesta deben seguir la convención
camelCase o PascalCase existente de forma consistente; en artículos, camelCase.
No implementes creación de borradores ni llamadas HTTP desde api-gp a Laravel.

Conserva las pruebas existentes. Registra y ejecuta las pruebas temporales que
crees; retira únicamente esas pruebas después de validar y antes del commit.
Actualiza la documentación del módulo y los inventarios existentes que apliquen.
Abre un PR hacia develop y solicita revisión según .github/CODEOWNERS.
No hagas merge ni despliegues.
```

Esta plantilla autoriza la entrega por PR cuando el usuario la invoca para una
implementación. Leer o editar este documento por sí solo no autoriza commits,
push, PR, merge ni despliegues de los cambios actuales.

## 1. Revisar antes de implementar

Leer los `AGENTS.md` aplicables, `pom.xml`, configuración de las dos bases,
`ResponseJsonUtils`, y controller/service/DAO comparables. `CollectorController`
es un ejemplo de múltiples operaciones en un mismo controller.

Revisar Git y preservar cambios ajenos. Inventariar los tests y recursos de prueba
existentes, incluidos los no seguidos. No asumir que existen `src/test` o los
inventarios de arquitectura solo porque una documentación histórica los menciona.

Derivar tipos y parámetros de la definición proporcionada. No inventar tablas,
columnas, joins, claves, valores por defecto ni firmas de procedimientos.
Verificar que el SP sea de lectura antes de ejecutarlo. Si falta una definición
o contrato esencial, preguntar únicamente lo que falta y avanzar con lo demás.
No mostrar credenciales ni modificar perfiles privados para implementar reportes.

## 2. Conexión y persistencia

| Conexión | JDBC | JDBC con parámetros nombrados | Gestor transaccional |
|---|---|---|---|
| Dynamics | `dynamicsJdbcTemplate` | `dynamicsNamedParameterJdbcTemplate` | `dynamicsTransactionManager` |
| Tuval | `tuvalJdbcTemplate` | `tuvalNamedParameterJdbcTemplate` | `tuvalTransactionManager` |

Usar inyección por constructor y `@Qualifier` explícito. No depender de `@Primary`
ni agregar DataSource, credenciales o propiedades de conexión. La base y los
objetos SQL se fijan en código, nunca se eligen desde la petición HTTP.

Cada consulta usa una conexión. Separar `dao/dynamics` y `dao/tuval` si una
funcionalidad necesita ambas; no suponer transacciones distribuidas ni joins entre
servidores. No copiar catálogos hardcodeados sin verificar su necesidad.

Preferir JDBC y DTO. Consultar una vista o un SP no requiere JPA: no agregar
`@Entity`, `@Id` ficticios ni tocar escaneo JPA sin necesidad. Si se justifica JPA,
registrar exclusivamente los paquetes de su base y actualizar los inventarios y
pruebas existentes. `@Transactional`, si se necesita, declara el gestor correcto y
`readOnly = true`; esta anotación no demuestra que el SQL no escriba.

## 3. DAO y SQL

El DAO contiene el SQL; el service valida y aplica reglas; el controller recibe
la petición y construye la respuesta. Reutilizar métodos y mappers del módulo.

- Vista/tabla: consulta parametrizada con `JdbcTemplate` o
  `NamedParameterJdbcTemplate`, columnas explícitas y mapper por tipo de resultado.
- SP: seguir el patrón existente de `SimpleJdbcCall`, configurado en el constructor,
  con esquema, procedimiento, firma explícita, metadatos de parámetros desactivados
  y `returningResultSet` cuando corresponda. Declarar y validar IN, OUT y retorno.
- En SQL Server, si se usa `withNamedBinding()`, declarar los nombres SQL con `@`
  y usar las mismas claves en `MapSqlParameterSource`. No cambiar por ello los
  nombres HTTP. Si se usa enlace posicional, respetar el orden de la firma.
- Nunca concatenar valores recibidos. Para orden configurable, usar una lista
  cerrada de columnas/direcciones; no admitir SQL ni identificadores libres.
- Cantidades e importes: `BigDecimal`. Fechas: `java.time` según SQL.
  Preservar nulos, precisión, ceros y negativos; no normalizar valores sin contrato.
- No cambiar la semántica de esquemas de unidades ni atributos crudos de las vistas:
  no asumir que equivalen a unidad de venta, marca o material comerciales.

Para tablas/vistas nuevas, si no hay contrato de paginación, usar página 0 y tamaño
100, máximo 500, con orden estable justificado. Reutilizar límites existentes al
ampliar una API. Paginar en SQL; no descargar todo para recortar en Java.
Si falta una clave única, no inventarla: justificar un orden por valores y sus
límites. Los SP conservan sus parámetros y límites reales; no añadir paginación
ficticia. Si su resultado no tiene límite, resolver el alcance antes de publicar.

El timeout y los límites son locales a la consulta; no mutar los templates
compartidos. Para consultas nuevas, timeout de 30 segundos salvo requisito distinto;
las existentes conservan sus límites específicos. `fetchSize` no es paginación.

## 4. HTTP y DTO

Las rutas comienzan con `/api/reportes/<area>/<funcionalidad>`. Agregar métodos
bajo el `@RequestMapping` existente del módulo y conservar los contratos vigentes
salvo cambio expresamente solicitado. No crear alias o rutas paralelas sin motivo.

Ejemplos actuales, todos en `ArticulosController`:

```http
GET /api/reportes/inventario/articulos
GET /api/reportes/inventario/articulos/listado-items
POST /api/reportes/inventario/articulos/resolver
```

Usar GET con query para filtros simples. Para lotes/filtros complejos, POST con
JSON y justificarlo. No usar body en GET. Validar con Jakarta Validation y `@Valid`
antes de acceder a la base; mantener validación de reglas en el service.

Los DTO de respuesta siguen camelCase o PascalCase de forma consistente con el
módulo; en artículos se usa camelCase, por ejemplo `txItm`, `descripcion`,
`txAtrMar`, `txAtrMat`. No copiar snake_case de SQL ni forzar nombres SQL con
`JsonProperty`. El DAO traduce entre las columnas reales y los campos del DTO.
No renombrar parámetros HTTP ni columnas SQL como efecto secundario de este ajuste.

Reutilizar `ResponseEntity<Map<String,Object>>` y `ResponseJsonUtils`:

- Éxito 200: `success`, `message`, `data`.
- Sin coincidencias 200: `data: []` según el contrato de la consulta.
- Validación 400: `success: false`, mensaje público; `buildNoFound` devuelve 400.
- Error interno 500: `buildError` con mensaje genérico y excepción registrada en
  el servidor. No devolver `e.getMessage()`, SQL ni detalles de conexión al cliente.

En controllers compartidos, conservar el mensaje de validación que corresponda a
cada request. El manejo local no debe alterar los errores de otros endpoints.
Respetar el acceso existente: no inventar autenticación ni ampliar permisos.

## 5. Responsabilidad de Laravel

La resolución solo interpreta renglones y devuelve identificaciones, candidatos,
ambigüedades y datos faltantes. Laravel decide las selecciones y crea el borrador
con su propio proceso. api-gp no conoce ni replica ese proceso.

No crear endpoints `crear-borrador`, carpetas `client` o `service/tuvalbackend`,
clientes HTTP, reintentos, registros de idempotencia o dependencias para llamar a
Laravel. Los antiguos puentes de borradores fueron retirados. No regenerarlos a
partir de documentación histórica ni reutilizar sus excepciones antiguas.

## 6. Validación y entrega

Probar lo que cambie: registro de rutas sin colisiones, un controller/service/DAO
por funcionalidad y base, selección de conexión, parámetros/tipos SQL, nulos,
precisión, mappers, límites y paginación, respuestas 200/400/500 y validación sin
llamada al DAO. Para SP, verificar firma, enlace SQL Server, retorno y result sets.
Las simulaciones no prueban permisos, datos ni funcionamiento real de SQL Server.

Si se crean pruebas temporales, mantener una lista explícita y retirarlas solo
tras corregir y validar todos sus fallos. No borrar tests ajenos, preexistentes o
sin seguimiento, ni carpetas completas de pruebas. Reutilizar la infraestructura
existente; no agregar dependencias permanentes solo para comprobaciones temporales.

Si existen inventarios exactos de componentes/JPA, actualizarlos; no generar una
estructura de tests ausente solo por copiar un registro histórico. Ejecutar
`mvn clean verify` (o `mvnw.cmd clean verify` si corresponde), limpiar clases viejas
al mover o eliminar componentes y comprobar el JAR final. Declarar explícitamente
si Maven no encontró tests. Ejecutar `git diff --check` y revisar todo el diff.

Documentar ubicación, ruta, entradas, JSON, conexión, fuente SQL, límites, cambios
para consumidores y validaciones realmente ejecutadas. Distinguir comprobaciones
históricas de actuales y mocks de pruebas con base real. No afirmar despliegue ni
funcionamiento en otro servidor basándose en una prueba local.

Cuando la tarea incluya entrega por PR: crear rama `codex/reporte-<funcionalidad>`
desde `origin/develop` actualizado sin perder cambios locales; incluir solo su
alcance, commit, push y PR hacia `develop`. Usar `.github/CODEOWNERS` para solicitar
revisión a responsables con acceso de escritura distintos del autor. Si falta
acceso, informar el bloqueo; no inventar revisores, aprobación ni una URL de PR.
No hacer merge ni desplegar salvo autorización explícita posterior.
