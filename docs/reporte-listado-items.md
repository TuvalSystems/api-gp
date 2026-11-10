# Reporte de listado ligero de items

## Alcance y conexion

`GET /api/reportes/listado-items` consulta exclusivamente `dbo.vw_listadoItems`
mediante `dynamicsNamedParameterJdbcTemplate`. Usa el catalogo del DataSource,
sin crear conexiones, entidades JPA, objetos SQL ni modificar configuracion privada.
El modulo esta en `reportes/listadoitems`.

Destino verificado el 10 de noviembre de 2026: el ambiente de pruebas indicado
por el solicitante, catalogo `Dynamics2021`, perfil existente `dev`.
El perfil `qa` corresponde a otro ambiente y no se usa para esta entrega.
No se consulta ni despliega produccion. No se publican direcciones ni nombres de
servidor; no se codifican destinos en el DAO ni se pueden elegir desde HTTP.

La API no reemplaza el endpoint de resolucion de articulos. El resolvedor consulta
directamente esta misma vista, sin llamar por HTTP a este listado.
Se preservan los campos reales de la vista; no se afirma equivalencia con el maestro.
En particular, `unidadMedida` proviene de `IV00101.UOMSCHDL` (esquema de unidades)
y no de la unidad de compra. `tx_atrMar` y `tx_atrMat` son atributos de `tbm_inv`,
sin renombrarlos ni traducirlos a las etiquetas Marca/Material del maestro.

## Origen verificado

Definicion leida con permiso SELECT y metadatos del servidor de pruebas:

```sql
CREATE VIEW vw_listadoItems AS
SELECT TRIM(a1.ITEMNMBR) AS tx_itm,
       TRIM(a1.ITMSHNAM) AS codZafiro,
       TRIM(a1.ITMGEDSC) AS cod3L,
       TRIM(a1.ITEMDESC) AS Descripcion,
       TRIM(a1.UOMSCHDL) AS unidadMedida,
       a2.tx_atrMar, a2.tx_atrMat
FROM TUVAL.dbo.IV00101 AS a1
INNER JOIN dbo.tbm_inv AS a2 ON a1.ITEMNMBR = a2.tx_itm
WHERE a1.ITEMTYPE = 1
  AND (a1.ITEMNMBR LIKE '%I' OR a1.ITEMNMBR LIKE '%S');
```

La definicion se documenta; no se incluye migracion ni se ejecuta DDL.

| Columna SQL y propiedad JSON | Tipo SQL | Java | Nullable |
|---|---|---|---|
| tx_itm | varchar(31) | String | si |
| codZafiro | varchar(15) | String | si |
| cod3L | varchar(11) | String | si |
| Descripcion | varchar(101) | String | si |
| unidadMedida | varchar(11) | String | si |
| tx_atrMar | varchar(100) | String | si |
| tx_atrMat | varchar(100) | String | si |

Los NULL se preservan en JSON. La muestra completa de pruebas tenia 12.310 filas,
12.310 codigos distintos y ningun codigo nulo. Esto no demuestra una restriccion
de unicidad futura: `tbm_inv` tiene clave compuesta `(tx_itm, fe_ultmod)`.
No se inventa un identificador JPA ni se eliminan duplicados.

## Parametros

Todos son query parameters opcionales. Los filtros presentes se combinan con AND.

| Parametro | Regla |
|---|---|
| codigo | Igualdad con tx_itm, codZafiro o cod3L; maximo 31 caracteres |
| descripcion | Contiene texto literal; maximo 101 caracteres |
| marca | Igualdad con tx_atrMar; maximo 100 caracteres |
| material | Igualdad con tx_atrMat; maximo 100 caracteres |
| page | Entero de 0 a 10000; por defecto 0 |
| size | Entero de 1 a 500; por defecto 100 |

Un filtro omitido no se aplica. Un filtro vacio, solo espacios o demasiado largo
devuelve 400, igual que paginacion no numerica o fuera de limites. Se conserva el
texto recibido, sin eliminar espacios de los datos. Los valores se enlazan como
VARCHAR, offset como BIGINT y size como INTEGER. En descripcion, %, _, [ y ~
son literales escapados con `ESCAPE '~'`; no se permite SQL libre.

El orden fijo usa las siete columnas en el orden del SELECT con
`COLLATE Latin1_General_100_BIN2`, para desempatar variantes de mayusculas o acentos.
Se agregan DATALENGTH de las siete columnas para desempatar espacios finales,
que la comparacion SQL rellena incluso con BIN2. Filas iguales son indistinguibles. Se usa OFFSET/FETCH en SQL,
sin COUNT adicional, recorte en memoria ni suponer que tx_itm es clave unica.
Como cualquier paginacion offset sin snapshot, cambios concurrentes del catalogo
pueden mover filas entre peticiones. No se ofrece total de filas.
Timeout de sentencia: 10 segundos. MaxRows y fetchSize se fijan localmente al size;
no se modifican templates compartidos.

## Ejemplos

```http
GET /api/reportes/listado-items?descripcion=bushing&page=0&size=100
GET /api/reportes/listado-items?codigo=EJEMPLO001I
GET /api/reportes/listado-items?descripcion=codo&marca=EJEMPLO&size=50
```

Respuesta sintetica de ejemplo, no extraida del catalogo:

```json
{
  "success": true,
  "message": "Respuesta exitosa",
  "data": [
    {
      "tx_itm": "EJEMPLO001I",
      "codZafiro": "EJ001",
      "cod3L": "EJ",
      "Descripcion": "ARTICULO DE EJEMPLO",
      "unidadMedida": "PZA",
      "tx_atrMar": "EJEMPLO",
      "tx_atrMat": null
    }
  ]
}
```

- Lista vacia: 200, success=true, data=[].
- Validacion: 400, success=false, message="Parametros invalidos del listado de items".
- Error interno: 500, success=false, message="No se pudo consultar el listado de items".
- SQL, servidores, credenciales y mensajes internos nunca se envian al cliente.

## Acceso y ejecucion

Se conserva el mecanismo de acceso del proyecto; no se agrega autenticacion ni se
amplian permisos SQL. Antes de exponerlo fuera del ambiente interno, mantener los
controles de red/gateway del servicio. Esta entrega se prueba localmente con
escucha solo en 127.0.0.1 y Eureka deshabilitado, sin sustituir la API existente.

El pipeline documentado despliega desde develop. Abrir este PR no despliega.
No se hace merge ni se modifica el pipeline para saltar esa condicion.

## Verificacion

36 pruebas permanentes nuevas cubren SQL, tipos, limites, seleccion explicita
Dynamics incluso si Tuval es Primary, NULL, orden, cierre de recursos, filtros,
respuestas 200/400/500 y ausencia de detalles SQL. No abren conexiones reales.

Primera suite completa con Java 21 y Maven 3.9.9:
274 pruebas, 0 fallos, 0 errores, 1 omitida preexistente (WebApplicationTests).
Se conservaron todas las pruebas existentes y las 36 pruebas permanentes nuevas.
Tras eliminar unicamente diagnosticos temporales propios, clean verify repitio
la suite completa con el mismo resultado, antes del commit.

Comprobacion real de solo lectura en pruebas, tres repeticiones por consulta,
pagina de 100 filas, 10 de noviembre de 2026:

| Filtro descripcion | Vista nueva | Maestro anterior |
|---|---|---|
| BUSHING | 55-76 ms | 4210-4769 ms |
| CODO | 59-72 ms | 4207-4311 ms |
| NIPLE (sin resultados) | 55-63 ms | 3737-3867 ms |

Son mediciones puntuales de consulta, no SLA ni comparacion de equivalencia
funcional: las vistas difieren en unidad y atributos. La nueva consulta incluye
el mismo ORDER BY binario y paginacion usados por el DAO. No se usa el plazo
de 2 segundos del resolvedor anterior para estas mediciones.

El resolvedor conserva por separado esquemaUnidad, tx_atrMar y tx_atrMat y deja
unidad=null; no los usa como unidad comercial ni como etiquetas equivalentes
al maestro. Validar su significado antes de usarlos para crear cotizaciones.
Ver [contrato de resolucion](resolucion-articulos.md).

Comprobacion HTTP local del build final contra pruebas: busquedas bushing/codo
200; codigo exacto 200 con una fila; codigo inexistente y texto literal con %/[
200 con lista vacia; size=501 devuelve 400. Pagina0 repetida produjo el mismo
JSON y las paginas0/1 (20 filas cada una) no compartieron codigos en esa muestra.
