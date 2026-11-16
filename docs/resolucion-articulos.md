# Resolución de artículos por similitud de texto

Estado local: 2026-11-13. Sin despliegue.

## Porcentaje visible, sin corte mínimo

Cada coincidencia incluye `porcentajeCoincidencia`, numérico de 0 a 100 y
redondeado a dos decimales. Se muestran también sugerencias inferiores al 50%.
No se aplica el umbral anterior del 80% ni otro mínimo de porcentaje.

La cobertura ponderada del texto solicitado determina el porcentaje; BM25
solo desempata. Los resultados se ordenan de mayor a menor porcentaje.
Se mantiene la coincidencia con el término principal y, cuando hay varios
términos, al menos dos coincidencias: compartir una fracción no convierte
un rollo en una plancha. Estas condiciones no son un corte de porcentaje.

El valor literal de una medida y su conversión a milímetros comparten un peso
total de dos puntos; no se penaliza dos veces la misma medida. Para
`disco de zirconio de 4 pulgadas grano 40`, los artículos `002-000041-S` y
`002-000045-S` tienen **88,24%** (15/17), frente al 78,95% anterior (15/19).
Norton al principio y otras palabras adicionales del catálogo no penalizan
la cobertura del texto pedido. Se conserva la descripción real de 115 mm.

El porcentaje mide similitud textual, no una probabilidad ni equivalencia
técnica. Las sugerencias por texto conservan `estado=ambiguo`; un código exacto
tiene porcentaje 100 y solo se identifica automáticamente si devuelve una fila.
No se exponen el puntaje BM25 ni otros metadatos internos.

## Endpoint y entrada

`POST /api/reportes/inventario/articulos/resolver`

Se mantiene el request: `detalle` con 1–100 líneas, `texto` de hasta 200
caracteres y `cantidad` positiva (máximo 1.000.000, hasta tres decimales).
La cantidad omitida se devuelve como 1. `maxCandidatos` admite 1–6 y su
valor predeterminado es 3. La cantidad no interviene en la búsqueda.

```json
{
  "detalle": [
    {
      "texto": "disco de corte de 7 pulgadas x 1/16 Norton",
      "cantidad": 24
    }
  ],
  "maxCandidatos": 1
}
```

## Respuesta simplificada

Se conserva el envoltorio común `success/message/data`. Cada línea de `data`
contiene únicamente `texto`, `cantidad`, `estado` y `coincidencias`.
Cada coincidencia contiene únicamente `descripcion`, `codigoGP`, `codigo3L` y
`porcentajeCoincidencia`.

Ejemplo comprobado contra el maestro de pruebas:

```json
{
  "success": true,
  "message": "Respuesta exitosa",
  "data": [
    {
      "texto": "disco de corte de 7 pulgadas x 1/16 Norton",
      "cantidad": 24,
      "estado": "ambiguo",
      "coincidencias": [
        {
          "descripcion": "Disco corte 7\" x 1/16\" NORTON",
          "codigoGP": "002-000012-S",
          "codigo3L": "SPE",
          "porcentajeCoincidencia": 100.00
        }
      ]
    }
  ]
}
```

Estados:
- `ambiguo`: hay sugerencias por similitud, que deben revisarse. También se usa
  si una búsqueda por código encuentra más de una fila.
- `identificado`: exclusivamente un código exacto con una sola fila.
- `sin_coincidencia`: no hay resultados suficientemente relacionados;
  `coincidencias` es `[]`.

No se devuelven `candidatos`, `articulo`, `indice`, `cantidadAsumida`,
`codigoZafiro`, atributos, diferencias, puntajes ni observaciones.
Los consumidores que leían `candidatos` deben leer ahora `coincidencias`.
Un código 3L nulo permanece nulo. Las líneas conservan el orden de entrada.

## Algoritmo

La búsqueda textual no ejecuta `LIKE`, no filtra por atributos y no depende
de un enum de familias. Se retiró `ReglasArticulo`. La fuente sigue siendo
`dbo.vw_MaestroArticulo`, la misma del reporte del maestro, pero solo se
seleccionan código GP, código 3L y descripción.

1. Divide la descripción en palabras, números, fracciones y referencias.
2. Normaliza acentos, mayúsculas, plurales, presentaciones y abreviaturas.
   Incluye equivalencias textuales como hierro/acero, pulir/desbaste y
   contactig/contactip. No es una lista de productos permitidos.
3. Construye un índice invertido de las descripciones. Usa cobertura ponderada
   de términos y BM25 (frecuencia e importancia de palabras) para ordenar.
4. Si una palabra no existe en el índice, admite hasta un error de escritura
   en palabras de 4–6 letras y dos en palabras de 7 o más letras.
   No aplica distancia de edición a números ni referencias del fabricante.
5. Normaliza unidades en cadenas de texto sin asignar roles como diámetro o
   espesor. Las medidas cercanas, hasta 20% de diferencia y como máximo cinco
   variantes, aportan menos puntuación. No se consideran técnicamente iguales.
6. Exige coincidencia con la primera palabra informativa, omitiendo términos
   genéricos/presentación; en consultas de varios términos exige al menos dos
   coincidencias, sin corte de porcentaje. Esto evita devolver un
   motor solo porque comparte la marca Lincoln con un difusor.
7. Devuelve las mejores sugerencias, hasta `maxCandidatos`. Una diferencia
   de forma, espesor, material o marca reduce relevancia; no es un veto automático.

La búsqueda es léxica aproximada, sin IA externa, embeddings ni llamadas a un
proveedor. No interpreta todos los sinónimos posibles ni garantiza comprensión
semántica. La primera palabra informativa dirige la recuperación; descripciones
con un orden atípico o palabras muy genéricas pueden necesitar ajuste.
El puntaje BM25 es interno. El porcentaje público representa cobertura textual,
no una probabilidad de equivalencia.

## Índice, límites y errores

- Índice en memoria por instancia del servicio, construido en la primera consulta.
- Se reutiliza durante 60 segundos; la siguiente consulta tras vencer ese plazo
  lo renueva. Una modificación del catálogo puede tardar ese intervalo en verse.
- Se carga una sola vez ante solicitudes concurrentes, mediante publicación
  de un índice completo. No hay proceso en segundo plano ni persistencia local.
- Límite de 50.000 filas; se consulta una fila adicional como centinela. Si se
  excede el límite, se devuelve error, nunca una búsqueda silenciosamente parcial.
- Timeout JDBC de 10 segundos para cargar el catálogo. Los errores de carga
  siguen el HTTP 500 genérico existente, no se convierten en “sin coincidencia”.
- Los códigos GP completos y `codigo <valor>` usan igualdad contra GP/Zafiro/3L,
  directamente en SQL, con timeout de dos segundos. No dependen de la caché.
- Los listados `consultar` y `listado-items` conservan su comportamiento.
- No se leen precios, costos, existencias ni credenciales en la respuesta.
  No se modifican objetos SQL, índices de base de datos ni datos del catálogo.

## Histórico: prueba inicial con las 17 líneas (umbral del 30%)

Se usaron las mismas descripciones, cantidades y `maxCandidatos=6`, mediante
servicio/DAO reales contra DEV, verificando el servidor de pruebas y
`Dynamics2021`. El maestro devolvió 12.584 filas. Solo se ejecutaron lecturas,
sin iniciar SpringApplication, Eureka, jobs ni un servidor HTTP.

| Línea | Descripción resumida | Antes, filtros de atributos | Ahora, similitud |
|---|---|---:|---:|
| 1 | Canal U 200 × 50 × 4 | 0 | 6 |
| 2 | Canal U 200 × 50 × 3 | 0 | 6 |
| 3 | Platina negra 3 × 100 × 6000 | 0 | 6 |
| 4 | Tubo redondo negro 50 × 2 A36 | 0 | 6 |
| 5 | Ángulo negro 50 × 4 | 4 | 6 |
| 6 | Ángulo negro 50 × 6 | 4 | 6 |
| 7 | Tubo rectangular negro 100 × 50 × 3 | 0 | 6 |
| 8 | Alambre sólido 09 | 1 | 3 |
| 9 | Contactig 09 KP14-35 Lincoln | 0 | 0 |
| 10 | Difusor Lincoln MB15 002 007 | 0 | 0 |
| 11 | Tobera bronce KP23 H62 Lincoln | 0 | 0 |
| 12 | Disco corte Norton 7" × 1/16 | 1 | 6 |
| 13 | Disco zirconio 4" grano 40 | 0 | 6 |
| 14 | Soldadura 6011 1/8" | 3 | 6 |
| 15 | Disco pulir 4" | 0 | 6 |
| 16 | Plancha antideslizante 2440 × 1220 × 4 | 3 | 6 |
| 17 | Plancha lisa negra 2 mm | 3 | 6 |

Cobertura de sugerencias: **7/17 → 14/17**. Son 81 sugerencias devueltas,
no 81 equivalentes técnicos confirmados. La comparación anterior daba
19 candidatos con las reglas estrictas.

Ejemplos del primer resultado: canal C `522-000021-S` para la solicitud U
200 × 50 × 4; alambre de 0,9 mm `801-000036-S`; disco Norton exacto
`002-000012-S`; zirconio G40 de 115 mm `002-000041-S`; plancha negra
2 mm `680-000009-S`. Las tres referencias Lincoln continúan sin sugerencias
suficientemente relacionadas.

La ampliación es deliberada: pueden aparecer canales C frente a U, espesores
diferentes, inoxidable frente a negro, artículos con daño declarado y discos
de 4 1/2" frente a 4". La descripción completa y `estado=ambiguo` permiten
revisarlos antes de elegir. El aumento de cobertura no demuestra que todos
los resultados sean adecuados para la solicitud.

Medición final del lote: **1.912 ms** incluyendo carga/construcción del índice;
**153 ms** al reutilizarlo en el mismo servicio. Son muestras locales sin carga
concurrente de usuarios, no tiempos HTTP ni un SLA. La versión previa que
consultaba el maestro por cada búsqueda tardó 6,99 s en su muestra.

## Validación local actual

17 comprobaciones adicionales: los dos zirconios al 88,24%, sugerencias al
28,21% conservadas, exclusión de rollos al pedir planchas, orden descendente,
límite de candidatos, códigos exactos al 100%, cantidad y JSON con exactamente
cuatro campos por línea y cuatro por coincidencia. Se repitió el lote real contra
DEV/pruebas con el nuevo contrato. Sin despliegue.

### Histórico de comprobaciones anteriores

48 comprobaciones temporales: relevancia, sinónimos, plurales, acentos, errores
de escritura, fracciones, unidades, no confundir 6011 con una fracción mixta,
coincidencia más allá de las primeras 500 filas, duplicados, código exacto,
cantidades, validaciones, JSON reducido, caché/reconstrucción/concurrencia,
timeouts y rechazo de catálogo incompleto.

Compilación: `mvn -o clean verify`, Java 21. No se agregó `src/test`;
las comprobaciones se ejecutan por separado de Maven. No se desplegó.
