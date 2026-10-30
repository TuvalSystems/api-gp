# ReporteArticulos

Consulta de artículos, precios, costos, tránsito, promedios de venta y
clasificaciones desde `dbo.vw_MaestroArticulo`. Funcionalidad: `articulos`.

## Acceso y seguridad

Por indicación expresa del solicitante, este microservicio no incorpora
autenticación propia. `GET /api/reportes/articulos` expone **precio, costo y margen
(`marUti`) sin exigir credenciales**. No se verificó que exista un gateway, proxy,
VPN, firewall u otro control externo que proteja esta ruta.

**Antes de habilitar el endpoint, deben establecerse y verificarse controles de
red o acceso externos** que restrinjan su uso a las aplicaciones y personas
autorizadas para consultar esos valores. Este cambio no configura ni presupone
esos controles. No se debe exponer la ruta directamente a usuarios no autorizados.
No se modifican los mecanismos de acceso de otras APIs.

Las respuestas de error no contienen SQL, servidores ni mensajes del driver. El
módulo no registra filtros ni resultados comercialmente sensibles.

## Arquitectura y conexión

- Código: `tuval.api.web.reportes.articulos`.
- Capas: `controller` -> `service/dynamics` -> `dao/dynamics`, con `dto` y `request`.
- Método/ruta: `GET /api/reportes/articulos`, parámetros en query string, sin body.
- Conexión exclusiva: `dynamics`, mediante el bean existente
  `@Qualifier("dynamicsNamedParameterJdbcTemplate")`.
- Origen único: vista `dbo.vw_MaestroArticulo`, operación `SELECT`.
- Catálogo: el configurado en el DataSource; no se fija ni se recibe desde HTTP.
- Sin entidades JPA, nuevas conexiones, dependencias ni cambios en escaneo JPA.
- Inventario de componentes: se incorporan controller, service y DAO a
  `src/test/resources/architecture/components.txt`.

Se revisaron `configVista.xlsx` y `detalleVista.xlsx` adjuntos antes de implementar.
Ambos contienen las mismas 37 columnas en el mismo orden; el detalle contiene 10
registros. Los ejemplos respetan las longitudes declaradas, no contienen NULL y
sus 10 códigos `tx_itm` son distintos. Esto no verifica la unicidad global ni los
metadatos actuales de SQL Server. La mención de 18 columnas en el documento de
solicitud corresponde a su ejemplo JSON abreviado: la respuesta implementa las 37.
Los Excel son referencias de desarrollo; nunca se leen en tiempo de ejecución.

## Columnas y tipos

Los nombres JSON conservan exactamente los de SQL, incluidas mayúsculas y
abreviaturas. Se usa `String`, `BigDecimal`, `Short` y `LocalDateTime`,
respectivamente. No se redondean decimales, no se recortan cadenas y NULL se
mantiene como `null`; las 37 propiedades se incluyen aun cuando son NULL.
`fechacreacion` se serializa como fecha/hora ISO local, sin asignarle una zona
horaria que el tipo SQL `datetime` no proporciona.

| Columna SQL / JSON | Tipo SQL | Admite NULL |
|---|---|---|
| tx_itm | varchar(31) | Sí |
| codZafiro | varchar(15) | Sí |
| cod3L | varchar(11) | Sí |
| Descripcion | varchar(101) | Sí |
| unidadMedida | varchar(9) | Sí |
| claseItem | varchar(11) | Sí |
| precio | numeric(19,5) | No |
| peso | numeric(18,6) | No |
| costo | numeric(19,5) | No |
| transito | numeric(18,6) | Sí |
| cabGrupoVta | smallint | No |
| detGrupoVta | smallint | No |
| cabGrupoCom | smallint | No |
| detGrupoComp | smallint | No |
| promVtaMenAct | numeric(18,6) | No |
| promVtaMenTeo | numeric(18,6) | Sí |
| promVntaMenAnt | numeric(19,5) | No |
| moq | numeric(18,6) | Sí |
| fre | numeric(18,6) | Sí |
| stokbase | numeric(18,6) | Sí |
| exc | numeric(18,6) | Sí |
| itemCabecera | varchar(31) | Sí |
| categoriaCompra | varchar(200) | Sí |
| lineaCompra | varchar(200) | Sí |
| claseCompra | varchar(200) | Sí |
| Marca | varchar(200) | Sí |
| Material | varchar(200) | Sí |
| compras | varchar(200) | Sí |
| SubCompras | varchar(200) | Sí |
| Control | varchar(200) | Sí |
| SubControl | varchar(200) | Sí |
| fechacreacion | datetime | Sí |
| catven | varchar(3) | Sí |
| catpes | varchar(3) | Sí |
| catfre | varchar(3) | Sí |
| cattuv | varchar(3) | Sí |
| marUti | decimal(18,4) | Sí |

## Filtros y validación

Todos son opcionales y combinables con `AND`. Los ausentes no agregan condiciones;
una cadena vacía suministrada se conserva. No se permiten valores de longitud
superior a la declarada. Los valores se enlazan como `VARCHAR` mediante JDBC.

| Parámetro HTTP | Columna | Comparación | Longitud máxima |
|---|---|---|---|
| tx_itm | tx_itm | Exacta | 31 |
| codZafiro | codZafiro | Exacta | 15 |
| cod3L | cod3L | Exacta | 11 |
| descripcion | Descripcion | Parcial | 101 |
| claseItem | claseItem | Exacta | 11 |
| unidadMedida | unidadMedida | Exacta | 9 |
| categoriaCompra | categoriaCompra | Exacta | 200 |
| lineaCompra | lineaCompra | Exacta | 200 |
| claseCompra | claseCompra | Exacta | 200 |
| marca | Marca | Exacta | 200 |
| material | Material | Exacta | 200 |
| itemCabecera | itemCabecera | Exacta | 31 |
| catven | catven | Exacta | 3 |
| catpes | catpes | Exacta | 3 |
| catfre | catfre | Exacta | 3 |
| cattuv | cattuv | Exacta | 3 |

`descripcion` busca subcadenas mediante `LIKE` parametrizado. `%`, `_`, `[` y el
carácter de escape `~` suministrados se tratan literalmente; una comilla sigue
siendo parte del parámetro. La comparación de cadenas sigue la collation del
servidor; no se cambia su tratamiento de mayúsculas, acentos o espacios.

## Paginación, orden y rendimiento

| Parámetro | Valor inicial | Valores aceptados |
|---|---|---|
| page | 0 | Entero entre 0 y 2147483647 |
| size | 100 | Entero entre 1 y 500 |
| sort | tx_itm | Una de las 37 columnas, con el nombre exacto de la tabla anterior |
| direction | ASC | ASC o DESC |

El orden es una lista cerrada de enumeraciones; no admite SQL libre. Cuando se
ordena por otra columna se añade `tx_itm ASC` como desempate. El desplazamiento se
calcula en `long` y se enlaza como `BIGINT`; el tamaño se enlaza como `INTEGER`.
La paginación ocurre en SQL Server con `OFFSET ... FETCH NEXT ... ROWS ONLY`.
No se realiza COUNT ni se recupera la vista completa para paginar en Java.

Cada statement tiene timeout de 30 segundos y máximo de filas igual a `size`, sin
modificar el template compartido. El límite de respuesta no elimina el costo de
evaluar/ordenar la vista, los desplazamientos altos ni los posibles escaneos por
`LIKE '%texto%'`. Validar rendimiento y permisos en QA antes de habilitar.
No se agregan índices, NOLOCK ni cambios de aislamiento.

**Limitación del orden:** no se ha verificado una clave única de esta vista.
Los 10 ejemplos no bastan para demostrar que `tx_itm` sea único o no nulo en toda
la vista. Si existen filas empatadas en la columna elegida y `tx_itm`, el orden
entre ellas y su ubicación en páginas no están garantizados. Debe verificarse en
QA la unicidad y, si corresponde, acordar una clave adicional real; no se inventa
una. Las modificaciones concurrentes de la vista también pueden desplazar filas
entre peticiones de páginas.

## Petición y respuesta

```http
GET /api/reportes/articulos?tx_itm=966-000235-I&page=0&size=100&sort=tx_itm&direction=ASC
```

Ejemplo completo basado en el primer registro de `detalleVista.xlsx`. Es una
referencia de formato, no una respuesta obtenida de SQL Server en esta tarea.
Los decimales JSON son números; su representación puede contener ceros finales.

```json
{
  "success": true,
  "message": "Respuesta exitosa",
  "data": [
    {
      "tx_itm": "966-000235-I",
      "codZafiro": "966-152I",
      "cod3L": "BEV",
      "Descripcion": "MECH VALVULA MARIPOSA H. DUCTIL DISCO INOX 316  ASIENTO PTFE PALANCA 250 AWWA 2 1/2\" WAFER D71X32",
      "unidadMedida": "PZAS",
      "claseItem": "VT-BNS-15",
      "precio": 85.0438,
      "peso": 3.6025,
      "costo": 37.4,
      "transito": 4,
      "cabGrupoVta": 0,
      "detGrupoVta": 0,
      "cabGrupoCom": 0,
      "detGrupoComp": 0,
      "promVtaMenAct": 0,
      "promVtaMenTeo": 0,
      "promVntaMenAnt": 0,
      "moq": 1,
      "fre": 1,
      "stokbase": 1.5,
      "exc": 0,
      "itemCabecera": "966-000235-I",
      "categoriaCompra": "03. VALVULAS",
      "lineaCompra": "0301. VALVULAS",
      "claseCompra": "0301006. MARIPOSA",
      "Marca": "MECH",
      "Material": "H.DUCTIL",
      "compras": "MECH_ACC_VALV",
      "SubCompras": "NO_TIENE",
      "Control": "NO_TIENE",
      "SubControl": "NO_TIENE",
      "fechacreacion": "2025-01-23T00:00:00",
      "catven": "R",
      "catpes": "R",
      "catfre": "R",
      "cattuv": "CR",
      "marUti": 41
    }
  ]
}
```

El envoltorio es el de `ResponseJsonUtils`: no se agregan `total`, `page` ni otros
campos. Una lista vacía devuelve HTTP 200 con `data: []`.

Una longitud, tipo, página, tamaño u orden inválido devuelve HTTP 400 antes del DAO:

```json
{"success": false, "message": "Parámetros inválidos del reporte de artículos"}
```

Un error de consulta o timeout devuelve HTTP 500:

```json
{"success": false, "message": "No se pudo consultar el reporte de artículos"}
```

## Verificación y pendientes

Las pruebas del módulo usan MockMvc, el servicio real, JDBC/ResultSet simulados y
contextos Spring sin SQL Server. Cubren filtros, longitudes, paginación y orden,
37 propiedades, NULL, precisión decimal, fechas, contrato de respuestas, errores,
selección explícita de Dynamics aun con Tuval primario y consulta sin escrituras.
No constituyen pruebas de integración contra la vista real.

Ejecutar `mvn clean test` y `git diff --check`. En Windows puede usarse
`mvnw.cmd clean test` con Java 21. Los resultados reales de la ejecución se
reportan en el PR.

Queda pendiente en QA: confirmar el catálogo configurado, existencia y permisos
SELECT de la vista, contrastar sus 37 metadatos con el Excel, verificar unicidad,
ejecutar consultas reales con caracteres especiales, decimales, NULL y fechas,
medir tiempos y verificar paginación/rendimiento. La prueba original de arranque
con bases reales permanece deshabilitada. No se leyeron credenciales para intentar
esas validaciones. También queda pendiente establecer y verificar los controles
externos de acceso descritos arriba. No hacer merge ni desplegar como parte de
esta tarea.
