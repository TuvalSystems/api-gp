# Reporte spVen11

`GET /api/reportes/ven11` consulta `dbo.spVen11` a través del bean existente
`dynamicsJdbcTemplate`. La API devuelve los 15 campos solicitados, sin `co_docGP`,
y ordena el resultado completo por `tx_nom` ascendente.

## Fuente y alcance

La referencia es el script de `Dynamics2021.dbo.spVen11` entregado el 30/10/2026.
Su cuerpo contiene un SELECT con agregaciones y joins, sin escrituras persistentes,
SQL dinámico ni llamadas a otros procedimientos. Se invoca el procedimiento
existente: esta implementación no instala ni modifica objetos SQL.

El script consulta `Dynamics2021.dbo.tbm_loc`, `Dynamics2021..tbm_empREl`,
`DGP01.dbo.SOP30200`, `TUVAL.dbo.SOP30200` y `fnConvertirListaStringATable`.
Aunque el SP consulta esos catálogos internamente, la API utiliza una única
conexión, Dynamics. No admite seleccionar base, esquema, objeto ni SQL desde HTTP.
La llamada usa `dbo.spVen11` en el catálogo configurado para esa conexión; se debe
confirmar en QA que resuelve el objeto esperado en `Dynamics2021`.

La firma de referencia, corroborada por la captura proporcionada, es:

| Parámetro SQL | Tipo verificado | Uso HTTP |
| --- | --- | --- |
| `@FechaIni` | `date` | `FechaIni`, obligatorio |
| `@FechaFin` | `date` | `FechaFin`, obligatorio |
| `@Sucursales` | `varchar(max)` | `Sucursales`, obligatorio |
| `@isRelacionada` | `varchar(max)`, predeterminado `'N'` | Se omite de la llamada |

El usuario confirmó usar el valor predeterminado del SP para `isRelacionada`.
No se envía un cuarto argumento ni se fija `'N'` en Java: rige el predeterminado
del procedimiento instalado. La llamada JDBC declara el retorno estándar de
SQL Server como `INTEGER` y enlaza los tres argumentos con tipos `DATE`, `DATE`
y `VARCHAR`. No consulta metadatos para inferir la firma.

## Petición

```http
GET /api/reportes/ven11?FechaIni=2026-01-01&FechaFin=2026-01-30&Sucursales=FAC-CAL,FAC-QNT
Accept: application/json
```

```bash
curl --get 'http://localhost:8080/api/reportes/ven11' \
  --data-urlencode 'FechaIni=2026-01-01' \
  --data-urlencode 'FechaFin=2026-01-30' \
  --data-urlencode 'Sucursales=FAC-CAL,FAC-QNT'
```

Los nombres de los tres parámetros distinguen mayúsculas y minúsculas. Las fechas
deben tener formato ISO `AAAA-MM-DD`, estar entre `0001-01-01` y `9999-12-31`,
y cumplir `FechaIni <= FechaFin`. La lista de sucursales debe contener códigos
no vacíos separados por comas, sin espacios al principio o al final de cada código
ni caracteres de control. El ejemplo `FAC-CAL,FAC-QNT` fue corroborado con la
captura de una ejecución suministrada por el usuario. No se asumen otras sintaxis
de `fnConvertirListaStringATable`, cuya definición no se suministró.

Se aplica un máximo de 8000 caracteres al parámetro HTTP `Sucursales` como límite
de entrada de la API; no representa un límite de columna SQL ni modifica
`varchar(max)`. Los valores se enlazan, nunca se concatenan con SQL. No se
normalizan, expanden ni sustituyen códigos de sucursal. No hay parámetros de
paginación, orden configurable ni selector de `isRelacionada`.

## Respuesta

```json
{
  "success": true,
  "message": "Respuesta exitosa",
  "data": [
    {
      "tx_nom": "CALDERÓN",
      "subTotalDiaHoy": 1234.5600,
      "extCostDiaHoy": 900.1200,
      "subTotalAyer": null,
      "extCostAyer": null,
      "subTotalMes": 1234.5600,
      "extCostMes": 900.1200,
      "subtotalMesAnterior": null,
      "extCostMesAnterior": null,
      "subtotalMesAnterior2": null,
      "extCostMesAnterior2": null,
      "subtotalAnioActual": 1234.5600,
      "extCostAnioActual": 900.1200,
      "subtotalAnioAtras": null,
      "extCostAnioAtras": null
    }
  ]
}
```

Los importes del ejemplo son ilustrativos. `tx_nom` se obtiene mediante
`ResultSet.getString`; los 14 agregados se obtienen con `getBigDecimal` y se
serializan como números JSON, sin conversión intermedia a `float` o `double`,
redondeos ni sustitución de `NULL` por cero. No se afirma un tipo SQL, precisión,
escala, longitud ni nulabilidad específicos para las columnas: esos metadatos
no fueron proporcionados y su validación real sigue pendiente.

El SP también devuelve `co_docGP`. Por decisión expresa del usuario no se mapea
ni se incluye en la respuesta pública. Una consulta sin coincidencias responde
200 con `data: []`.

## Orden, límite y errores

El SP de referencia no tiene `ORDER BY`, `TOP` ni paginación. El usuario confirmó
el máximo de 500 filas y un error cuando se supere. El DAO detecta la fila 501,
cierra los recursos y rechaza la consulta completa con 400. No devuelve las
primeras 500 filas como una respuesta exitosa ni utiliza `setMaxRows`, que podría
ocultar el exceso. Este control no es paginación SQL y no limita el trabajo del
SP en el servidor.

Solo después de comprobar el tamaño se ordena la lista completa por `tx_nom`
en Java, mediante orden natural de `String` (sensible a mayúsculas y acentos),
con nombres `NULL` primero. No se reproduce la collation de SQL Server.
Entre nombres idénticos se conserva el orden recibido del SP; no se ofrece
un desempate único ni paginación. La llamada tiene timeout local de 30 segundos
y fetch size de 100, sin modificar el `JdbcTemplate` compartido.

| Situación | HTTP | Contrato |
| --- | --- | --- |
| Consulta válida, con o sin filas | 200 | `success: true`, `message`, `data` |
| Filtro ausente, fecha/lista inválida o entrada demasiado larga | 400 | `success: false`, mensaje genérico de parámetros |
| Más de 500 filas | 400 | `success: false`, mensaje para reducir sucursales; sin datos parciales |
| Fallo JDBC, timeout, retorno no cero/NULL, ausencia o exceso de result sets | 500 | `success: false`, mensaje público genérico |

Se procesa un único result set y se toleran contadores de actualización JDBC
antes o después de este, porque el SP de referencia no usa `SET NOCOUNT ON`.
Nunca se incluyen mensajes de excepciones, SQL ni servidores en la respuesta.
Se reutiliza `ResponseJsonUtils` y el mecanismo de acceso existente.

## Semántica temporal conservada y validación en QA

Las fechas de entrada solo filtran `subTotalMes` y `extCostMes` en el script
proporcionado. Hoy, ayer, los dos meses anteriores y los años se calculan a partir
de `GETDATE()` del servidor SQL, no de `FechaIni` o `FechaFin`. El año actual
filtra por año completo; la API no lo redefine como un acumulado hasta hoy.
El período del año anterior termina en el mismo mes/día del año anterior.
La construcción de esa fecha requiere comprobar el caso 29 de febrero.
Se conservan la lógica de notas de crédito, el corte de catálogos de 2023 y las
lecturas `NOLOCK` del SP, con sus posibles inconsistencias bajo concurrencia.

No se ejecutó el SP desde el entorno de desarrollo de esta tarea. La definición
y la captura suministradas no prueban que la versión instalada sea idéntica.
Antes de habilitar el reporte en un ambiente, verificar:

- Resolución del catálogo y versión instalada, predeterminado de `isRelacionada`,
  permisos de ejecución y acceso a los tres catálogos internos.
- Tipos y nulabilidad de la salida, aliases y conversiones JDBC sin pérdida de
  precisión; configuración de serialización y consumidores de importes grandes.
- Definición de `fnConvertirListaStringATable`, códigos reales y su formato CSV.
- Exactitud de importes y períodos contra el reporte original, comportamiento de
  fechas con hora, 29 de febrero y concurrencia con `NOLOCK`.
- Rendimiento, plan de ejecución, timeout, recursos y respuesta al exceder 500 filas.

La validación automática usa dobles JDBC y HTTP sin conexiones reales. Según
`docs/prompt-apis-reportes.md`, las pruebas nuevas de esta tarea son temporales,
se ejecutan junto con la suite existente y se retiran antes del commit. Se mantiene
el inventario permanente de componentes para cubrir el nuevo módulo en la suite
final. Los comandos y resultados de ambas ejecuciones se documentan en el PR.
