# Estructura modular de api-gp

api-gp es el intermediario entre tuvalBackend y las bases de datos Dynamics y Tuval.
Los paquetes se organizan primero por función de negocio. La conexión a la base de
datos es una separación interna de cada módulo, no el nivel principal del proyecto.

```text
tuval.api.web
├── ventas
│   ├── cotizaciones
│   ├── pedidos
│   ├── ordenCompra
│   ├── devoluciones
│   ├── facturacion
│   └── sincronizacion
├── bodega
│   ├── articulos
│   ├── stock
│   ├── transferencias
│   ├── despachos
│   ├── recepciones
│   └── reservas
├── cobranzas/cobros
├── clientes
│   ├── sincronizacion
│   └── sucursales
├── usuarios
├── transacciones
├── diagnostico
├── reportes
│   └── <funcionalidad> (nuevas APIs de reportes)
├── common
│   ├── response
│   └── util
└── config
```

Dentro de una funcionalidad se mantienen `controller`, `service`, `dao`, `dto`,
`request`, `entity` y `util` cuando existen. No se crean capas vacías. Por ejemplo:

```text
ventas/cotizaciones/
├── controller/QuoteController.java
├── service/dynamics/quoteRecordService.java
├── dao/dynamics/QuoteRecordDao.java
├── entity/dynamics/QuoteRecordEntity.java
├── dto/
└── request/

bodega/stock/
├── controller/
├── service/dynamics/
├── service/tuval/
├── dao/dynamics/
├── dao/tuval/
├── entity/tuval/
└── request/
```

## APIs nuevas de reportes

Toda API generada con el [prompt de reportes](prompt-apis-reportes.md) debe vivir en
`reportes/<funcionalidad>`, con sus capas internas y separación `tuval`/`dynamics`.
Sus rutas comienzan con `/api/reportes`. Las reglas de generación están en el
documento del prompt. No se trasladan APIs existentes.

`reportes/articulos` implementa [ReporteArticulos](reporte-articulos.md):
`GET /api/reportes/articulos`, consulta JDBC de `dbo.vw_MaestroArticulo` mediante
`dynamicsNamedParameterJdbcTemplate`, con filtros y paginación SQL. Su documentación
detalla las 37 columnas, límites, ausencia de autenticación propia y controles
externos de acceso pendientes antes de habilitarlo.

## Acceso a las dos bases de datos

La configuración permanece en `config/DynamicsJpaConfig.java` y
`config/TuvalJpaConfig.java`. Se conservan los prefijos de propiedades, nombres de
beans, unidades de persistencia y la selección de Dynamics como `@Primary`.

| Recurso | Dynamics | Tuval |
|---|---|---|
| Propiedades | `dynamics.datasource` | `tuval.datasource` |
| DataSource | `dynamicsDataSource` | `tuvalDataSource` |
| JdbcTemplate | `dynamicsJdbcTemplate` | `tuvalJdbcTemplate` |
| NamedParameterJdbcTemplate | `dynamicsNamedParameterJdbcTemplate` | `tuvalNamedParameterJdbcTemplate` |
| EntityManagerFactory | `dynamicsEntityManagerFactory` | `tuvalEntityManagerFactory` |
| Unidad de persistencia | `dynamicsPU` | `tuvalPU` |
| Gestor de transacciones | `dynamicsTransactionManager` | `tuvalTransactionManager` |

Los DAO conservan sus `@Qualifier`. Las configuraciones declaran explícitamente
los nuevos paquetes de repositorios y entidades de cada conexión. Un módulo que
acceda a las dos bases debe tener DAO separados: `dao/dynamics` y `dao/tuval`.

Al incorporar un nuevo paquete de repositorios JPA o entidades, actualizar el
escaneo en la configuración de su base de datos y las pruebas correspondientes.
Mover una clase JDBC que ya tiene anotaciones `@Table` no la convierte en entidad
JPA: esta migración conserva las anotaciones originales.

Declarar siempre el gestor de una operación transaccional:

```java
@Transactional(transactionManager = "tuvalTransactionManager", rollbackFor = Exception.class)
```

En esta migración se corrigen las operaciones de recepción/facturación de
`transactionService` y las actualizaciones de pedidos de `GeneralService` para
seleccionar Tuval explícitamente. La actualización de cotizaciones de
`GeneralService` selecciona Dynamics. Una transacción local no garantiza rollback
coordinado entre las dos bases de datos.

## Alcance de esta migración

Se mantienen las URLs, métodos HTTP, campos JSON, nombres de clases, SQL y
procedimientos almacenados. Los archivos de propiedades de conexión no cambian.
`ResponseJsonUtils` pasa a `common/response`, conservando su comportamiento.

El flujo existente de `TransactionController`, su servicio y su DAO combina
recepciones y facturación. Se conserva unido en `transacciones` para no mezclar
la reorganización con una división funcional de ese flujo. Sus DTO y solicitudes
ya están ubicados en `bodega/recepciones` y `ventas/facturacion`. `ventas/sincronizacion`
agrupa las operaciones existentes de actualización de pedidos y cotizaciones.

Las siguientes refactorizaciones pueden hacerse por submódulo: nombres de clases,
inyección por constructor, DTO de entrada separados de persistencia, respuestas
tipadas y manejo central de errores. Deben conservar el contrato de tuvalBackend o
coordinar expresamente sus cambios.

## Verificación

Ejecutar `mvn clean test` después de mover paquetes para eliminar clases antiguas
de `target` que podrían duplicar componentes durante el escaneo de Spring.

Las pruebas nuevas no abren conexiones reales:

- Arrancan los 52 componentes originales con ambas conexiones simuladas.
- Comprueban qué DataSource se inyecta en cada DAO.
- Verifican la separación de paquetes, unidades de persistencia y gestores.
- Comprueban mediante proxies de Spring la selección del gestor y la solicitud de
  rollback al fallar operaciones de Tuval y Dynamics.

La prueba original `WebApplicationTests` continúa deshabilitada porque requiere
las bases de datos. Antes de desplegar, validar en QA la ejecución de consultas,
procedimientos y rollback real en SQL Server.
