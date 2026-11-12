> Estado actual (2026-11-12): la consulta está integrada en
> `reportes/inventario/articulos`, con método `resolverArticulos` en
> `ArticulosController`, `ArticulosService` y `ArticulosServiceImp`, y búsqueda
> `buscarCandidatosResolucion` en `ArticulosDao`. Usa `ReglasArticulo` como helper
> de interpretación y los DTO/requests específicos de resolución.
> Ruta vigente: `POST /api/reportes/inventario/articulos/resolver`.
> Se retiraron la ruta anterior de consulta y todos los puentes de creación de
> borradores. Laravel consume la consulta y crea el borrador con su propio proceso.
> Los atributos de respuesta `txAtrMar` y `txAtrMat` se devuelven en camelCase;
> las columnas SQL siguen siendo `tx_atrMar` y `tx_atrMat`.
> Las pruebas y mediciones históricas descritas más abajo corresponden a versiones
> anteriores; esta copia no contiene `src/test`.

Validación de la integración (2026-11-12): compilación y empaquetado con
`mvn -o clean verify`. Comprobación temporal con Spring y JDBC simulado, retirada
tras validar: tres rutas en `ArticulosController`, service/DAO compartidos,
resolución exacta, ambigüedad, texto no soportado, ausencia de coincidencias,
truncamiento, timeout, reutilización de búsquedas dentro del lote, parámetros y
límites SQL, JSON estricto, cantidad por defecto, atributos camelCase y respuestas
200/400/500. Maestro y listado conservan sus consultas. No se ejecutó una
creación de borrador, llamada HTTP a Laravel ni consulta a SQL Server real.
# Consulta de resolución de artículos (v1)

`POST /api/reportes/inventario/articulos/resolver` recibe líneas extraídas por el
asistente de una imagen o transcripción. Cada línea se resuelve de forma independiente,
en el orden recibido. Es una consulta: no cotiza, consulta existencias, selecciona
bodega California ni crea documentos. No usa IA externa.

## Contrato

Se utiliza POST con JSON porque el filtro es un lote estructurado. El envoltorio es
`ResponseJsonUtils`: HTTP 200 con `success`, `message`, `data` (lista de resultados);
HTTP 400 con mensaje genérico para entradas inválidas; HTTP 500 con mensaje genérico
para errores internos. Ningún error devuelve mensajes del driver, SQL o servidores.

```http
POST /api/reportes/inventario/articulos/resolver
Content-Type: application/json
```

```json
{
  "renglones": [
    {"texto": "bushing galvanizado 1 por 1/2", "cantidad": 9},
    {"texto": "neplo galvanizado 1/2 por 2", "cantidad": 2},
    {"texto": "codo galvanizado 1 por 90"},
    {"texto": "llave paso total 2"},
    {"texto": "teflón grande"}
  ],
  "maxCandidatos": 3
}
```

| Entrada | Regla |
|---|---|
| `renglones` | Obligatorio, entre 1 y 20 elementos no nulos |
| `texto` | Obligatorio, no vacío, máximo 200 caracteres |
| `cantidad` | Opcional o null: se devuelve 1 y `cantidadAsumida=true` |
| Cantidad explícita | Mayor que 0, máximo 1000000, hasta 3 decimales; BigDecimal |
| `maxCandidatos` | Opcional o null: 3; entero de 1 a 5 |

La cantidad debe enviarse por separado. No se interpreta el primer número del texto
como cantidad: podría ser un diámetro. La API no convierte unidades de venta ni
extrae texto de imágenes/audio. Una cantidad inválida rechaza el lote antes de consultar.

Cada resultado incluye:

| Campo | Significado |
|---|---|
| `indice`, `texto` | Índice desde 0 y texto original, para correlacionar la línea |
| `cantidad`, `cantidadAsumida` | Cantidad efectiva y si se aplicó el valor predeterminado |
| `estado` | `identificado`, `ambiguo` o `sin_coincidencia` |
| `articulo` | Candidato único identificado; null en los demás estados |
| `candidatos` | Hasta `maxCandidatos`, sin puntuación de similitud |
| Candidato | `codigoGP`, `codigoZafiro`, `codigo3L`, `descripcion`, `unidad`, `atributos` |
| `datosFaltantes` | Atributos que requieren aclaración, términos no interpretados o datos desconocidos del catálogo |
| `diferencias` | Valores distintos observados entre candidatos compatibles, antes del recorte de respuesta |
| `busquedaCompleta` | Se terminó la exploración de la familia/código dentro del vocabulario documentado |
| `busquedaTruncada` | Se superó el límite de exploración de 500 filas |
| `candidatosTruncados` | Se omitieron candidatos compatibles al limitar la respuesta |
| `observaciones` | Motivos de búsqueda incompleta y límites de interpretación |

Los códigos conservan NULL si la vista no los informa. `unidad` se devuelve siempre
null: la fuente aporta un esquema de unidades (UOMSCHDL), no una unidad comercial.
`atributos` contiene datos interpretados de la descripción: `medidas`, `unidadMedidas`,
`material`, `clase`, `refuerzo`, `angulo`, `rosca`, `cedula`, `construccion`,
`gradoMaterial`, `paso`. Si no son NULL, agrega por separado los valores crudos
`esquemaUnidad`, `txAtrMar` y `txAtrMat`, sin equivalencia comercial.
Las medidas se expresan como fracciones reducidas y en el orden escrito; `1½` pasa
a `3/2`, nunca a `1/2`. También se reconoce la fracción mixta `1-1/2`.
Las diferencias muestran hasta cinco valores por atributo;
`otros_valores_no_mostrados` anuncia que hay más. `no_especificado` significa desconocido.

Ejemplo abreviado de resultado, suponiendo que la exploración completa encontró las
dos variantes conocidas (los alias no se inventan en este ejemplo):

```json
{
  "success": true,
  "message": "Respuesta exitosa",
  "data": [{
    "indice": 0,
    "texto": "bushing galvanizado 1 por 1/2",
    "cantidad": 9,
    "cantidadAsumida": false,
    "estado": "ambiguo",
    "articulo": null,
    "candidatos": [
      {"codigoGP":"020-000006-I", "codigoZafiro":null, "codigo3L":null,
       "descripcion":"MECH BUSHING GALVANIZADO 150 UL/FM 1\" X 1/2\" NPT",
       "unidad":null, "atributos":{"medidas":"1 x 1/2","clase":"150"}},
      {"codigoGP":"022-000084-I", "codigoZafiro":null, "codigo3L":null,
       "descripcion":"MECH BUSHING GALVANIZADO 300 UL/FM 1\" X 1/2\" NPT",
       "unidad":null, "atributos":{"medidas":"1 x 1/2","clase":"300"}}
    ],
    "datosFaltantes":["unidadMedidas","rosca","clase_o_refuerzo"],
    "diferencias":{"clase":["150","300"]},
    "busquedaCompleta":true,
    "busquedaTruncada":false,
    "candidatosTruncados":false,
    "observaciones":["fuente_listado_items","unidad_comercial_no_disponible",
      "atributos_fuente_sin_equivalencia_comercial","reglas_limitadas_al_vocabulario_documentado"]
  }]
}
```

Los atributos se abrevian en el ejemplo; el servidor incluye todos los reconocidos.
Si la familia excede 500 filas, la misma entrada puede devolver una lista parcial
o vacía con `estado=ambiguo`, `busquedaCompleta=false` y `busquedaTruncada=true`.
Nunca debe interpretarse esa lista como el conjunto completo de alternativas.

## Reglas conservadoras y alcance semántico

Se normalizan mayúsculas, acentos, espacios, `×`, comillas de pulgadas y fracciones
Unicode `¼ ½ ¾ ⅛ ⅜ ⅝ ⅞ ⅓ ⅔`, incluidas fracciones mixtas y decimales hasta tres cifras.
`1 1/2` es una sola medida; para dos diámetros debe usarse `1 x 1/2` o `1 por 1/2`.
Se admiten espacios alrededor de `/`. No se convierten mm a pulgadas; sin indicación
de unidad se presentan candidatos pero se pide confirmar `unidadMedidas`.

Familias acotadas:

- Bushing: dos medidas ordenadas.
- Niple/neplo: diámetro y largo, en ese orden.
- Codo: diámetro y ángulo 45/90, separado de la clase 150/300.
- Nudo/unión universal: una medida.
- Llave esférica, válvula bola o llave paso total: una medida. No se equiparan
  válvulas de compuerta, mariposa u otros mecanismos con una válvula bola.
- Teflón/cinta PTFE: se ofrecen candidatos; siempre se pide confirmar ancho, largo
  y espesor. “Grande” no significa 3/4 ni 1 pulgada.

Se reconocen galvanizado/galv, inoxidable/inox (grado 304/316 si está explícito),
bronce, latón, PVC, acero negro; clase 150/300, reforzado/estándar, sin/con costura,
NPT/BSP/BSPT/BSPP y cédula/SCH explícita. Clase y refuerzo son atributos diferentes:
un codo 150 también puede ser reforzado. No se infieren marcas desde `tx_atrMar` ni se completa el material desde
`tx_atrMat`: su equivalencia con las etiquetas del maestro no está validada.
Una marca escrita en la petición permanece como término pendiente; las válvulas
no pueden identificarse automáticamente por texto mientras falte ese dato.

Una contradicción explícita descarta un candidato. La ausencia de un atributo en
su descripción es desconocida: no equivale a estándar, NPT, sin refuerzo u otra
variante. Los datos faltantes del catálogo impiden una identificación automática.
Una fila con atributos contradictorios tampoco se descarta usando solamente el
primer valor interpretado: permanece como alternativa incierta. Una descripción
genérica de válvula sin mecanismo no demuestra que se trate de otra familia.
Los términos sin interpretar, unidades mixtas y varios tipos/materiales/atributos
contradictorios también impiden identificar. Los términos desconocidos no se
traducen a filtros inventados; pueden quedar candidatos que requieran revisión.

Para identificar por texto deben estar completos los atributos obligatorios:
medidas, material, unidad de medidas y rosca; clase o refuerzo en accesorios,
ángulo en codos y marca en válvulas. Además debe existir exactamente un candidato
compatible y una búsqueda completa. No hay ranking ni selección del “más parecido”.
Incluso `maxCandidatos=1` conserva la ambigüedad si existen más candidatos compatibles.

También se acepta un código GP completo (`020-000006-I`) o `codigo <valor>` para
buscar por igualdad en GP, Zafiro o 3L. No es una búsqueda por prefijo. La comparación
SQL sigue la collation de la base. Colisiones de alias o filas duplicadas no se
deduplican y permanecen ambiguas. No se presupone una clave única de la vista.

`sin_coincidencia` solo significa cero candidatos compatibles en una exploración
completa de la familia/código y reglas soportadas. No demuestra que un artículo
no exista con otra denominación. No se interpreta lenguaje universal, OCR erróneo,
números escritos en palabras, normas no declaradas ni todas las abreviaturas del
maestro. No se afirma que un artículo esté activo o habilitado para venta: esa
regla todavía no está demostrada en la fuente.

## Datos, límites y rendimiento

Código en `tuval.api.web.reportes.inventario.articulos`, con Controller →
Service/dynamics → DAO/dynamics, DTO y Request. Conexión exclusiva mediante
`@Qualifier("dynamicsNamedParameterJdbcTemplate")`; origen `dbo.vw_listadoItems`.
Se seleccionan explícitamente los siete campos descriptivos necesarios: tx_itm,
codZafiro, cod3L, Descripcion, unidadMedida, tx_atrMar y tx_atrMat. El DTO público solo
expone identificadores, descripción, unidad y
atributos interpretados y crudos diferenciados. No reutiliza el DTO financiero
de ReporteArticulos. Comparte la vista con `/api/reportes/inventario/articulos/listado-items`, sin una
llamada HTTP entre APIs, y conserva el contrato de resolución.

Por lote hay como máximo seis exploraciones distintas y un presupuesto de diez
segundos comprobado antes de iniciar cada búsqueda. Se reutiliza una familia/código
solo dentro del lote. Cada SELECT lleva `TOP (501)`, máximo JDBC 501 y timeout local
de dos segundos; la fila adicional prueba truncamiento, se descarta y se conservan
como máximo 500 filas por exploración. El timeout devuelve ambigüedad explícita;
un error de acceso distinto devuelve HTTP 500 para el lote, sin resultados parciales.

Al agotar el presupuesto, las líneas no consultadas reciben `ambiguo`, búsqueda
incompleta y `presupuesto_de_busqueda_agotado`. El límite temporal es una protección
de admisión, **no un SLA total de diez segundos**: una consulta ya iniciada puede
terminar después y los tiempos de obtención de conexión/cancelación dependen del
DataSource/driver. Como máximo se inician seis statements. El orden SQL usa los siete
campos proyectados con collation binaria y DATALENGTH para desempatar espacios
finales; empates idénticos o cambios concurrentes no prueban unicidad.

La recuperación por familia usa anclas cerradas de descripción (incluidos los
sinónimos documentados y las grafías VÁLVULA, UNIÓN UNIVERSAL y TEFLÓN), no la frase
entera de la petición. Evita enviar el listado
al asistente, pero `UPPER(Descripcion) LIKE '%ancla%'` y el orden todavía pueden
escanear la vista. El límite de filas y el timeout no garantizan una consulta rápida.
No se agregan tablas, índices, objetos SQL, hints, cambios de aislamiento, conexiones
ni cachés persistentes. Tampoco se precarga el maestro al arrancar.

La vista ligera reduce el costo de la consulta, pero no amplía el límite de 500
filas ni el vocabulario del resolvedor. Una familia amplia puede seguir truncada.
No se afirma equivalencia de cobertura o de atributos con el maestro anterior.
`identificado` identifica un código dentro del alcance descrito: no significa que
la unidad comercial esté disponible ni que se pueda cotizar automáticamente.
Validar la unidad y los atributos antes de usarlos en la creación de cotizaciones.

## Acceso y seguridad

El proyecto actual no incorpora Spring Security ni filtros de autenticación para
los reportes; este módulo respeta ese mecanismo y no amplía permisos ni introduce
credenciales. Los controles externos de gateway/red no han sido verificados para
esta nueva ruta. Deben validarse antes de habilitarla; la ruta no se publica ni
despliega como parte de esta entrega.

Solo el DAO contiene SQL. Los identificadores y anclas son constantes internas;
los códigos y límites se enlazan con tipos JDBC explícitos. El texto libre nunca
se interpola en SQL. No hay selección de base/esquema desde HTTP ni consultas sin
ancla. No se leen ni devuelven precio, costo, margen, existencias o stock por bodega.
No se registran textos de usuario ni resultados. Los límites lógicos se aplican
antes de consultar; el límite HTTP de tamaño de body/concurrencia debe establecerse
en el mecanismo de entrada existente y verificarse en QA.

## Verificación

Las pruebas de regresión del módulo no abren SQL Server. Cubren fracciones mixtas,
pulgadas/mm, orden de medidas, cantidad predeterminada y explícita, las variantes
indicadas, términos desconocidos/contradictorios, código exacto/duplicados, búsqueda
vacía, timeout, exploración truncada y respuesta recortada; también validación 400
sin DAO, 500 genérico, lista permitida del DTO, parámetros/tipos SQL, NULL, límite
de statements y selección de Dynamics aun con Tuval primario. Se registra cada
componente nuevo en el inventario de arquitectura; no cambia el escaneo JPA.

Ejecutar con Java 21: `mvn clean test` y `mvn -DskipTests package`; revisar
`git diff --check`. Maven puede ejecutarse con el wrapper del repositorio.

Se comprobaron por HTTP DEV las descripciones de 020-000006-I, 022-000084-I,
037-000014-S, 037-000014-I, 020-000068-I, 020-000069-I y 022-000115-I mediante el
reporte existente, con consultas por código y sin mostrar campos comerciales.
Esto valida muestras de formato; la comprobación del DAO real en QA se detalla a
continuación. Siguen pendientes DEV directo, cancelación, cobertura completa,
NULL/duplicados adicionales, presupuesto bajo carga y controles externos de acceso.
No hay prueba de despliegue ni de reglas de habilitación comercial.

### Histórico: comprobación de la fuente anterior en QA, 2026-11-09

Se ejecutó el DAO de esta rama mediante un harness Java independiente, sin
SpringApplication, contexto Spring, Eureka, jobs ni servidor HTTP. Se reutilizó
exclusivamente el perfil QA existente y se verificó antes de las consultas el
destino configurado, distinto de producción. SQL Server devolvió la identidad
`SRV-SQL2-ENTORN\PRUEBAS`, catálogo `Dynamics2021`, y permiso SELECT sobre la vista.
Solo se ejecutaron SELECT; no se cambiaron bases, objetos, permisos ni aislamiento.

El primer intento devolvió SQLState `S0001`, error `207`: `sys.columns` confirmó
que QA no tiene categoriaCompra, lineaCompra ni claseCompra. Esos campos no se
usaban en la resolución ni en el contrato público; se retiraron del DTO interno,
SELECT y orden. No se sustituyeron columnas ni se modificó la vista. Se repitieron
las pruebas locales y el harness con el SQL final de siete campos.

La repetición ejecutó satisfactoriamente dos consultas por familia (una aislada y
otra dentro del lote mixto), con los límites reales del DAO:

| Familia | Filas por exploración | Latencia observada del DAO |
|---|---:|---:|
| Bushing | 212 | 72.6–998.2 ms |
| Codo | 501, incluye centinela de truncamiento | 102.5–139.9 ms |
| Niple/neplo | 501, incluye centinela de truncamiento | 128.8–416.1 ms |
| Nudo | 155 | 107.2–489.4 ms |
| Válvula/llave | 501, incluye centinela de truncamiento | 174.3–222.7 ms |
| Teflón | 104 | 116.6–147.7 ms |

Las 22 consultas por código devolvieron exactamente una fila cada una en
36.2–290.2 ms. No hubo errores ni timeouts en esta repetición. Los seis lotes
aislados tardaron 130.0–1299.6 ms, incluida interpretación y primer uso de clases;
el lote mixto de seis renglones tardó 948.7 ms. Es una muestra local contra QA,
sin calentamiento controlado ni carga concurrente; no es un SLA ni un percentil.

Se verificaron 24 resultados del servicio real: todos ambiguos, hasta tres
candidatos, cantidades predeterminadas correctas y ninguna identificación única
con búsqueda incompleta. Los bushings solicitados incluyeron los pares esperados
150/300; los nudos incluyeron los tres códigos esperados. `½` produjo los mismos
pares que `1/2`, y `1½` produjo `3/2`, sin confundirse con media pulgada. Los códigos
de codos y neplos con sus variantes 150/300/reforzado se confirmaron en las lecturas
exactas de referencia. Se observó truncamiento real en codos, neplos y válvulas;
el servicio mantuvo `busquedaCompleta=false`, `busquedaTruncada=true` y `ambiguo`.

**Límite de utilidad confirmado:** filas con atributos no interpretados permanecen
como alternativas inciertas y pueden ocupar la lista compacta antes que variantes
conocidas. En esta muestra, los primeros tres candidatos de codos, neplos y llaves
no mostraron todos los códigos de referencia. También teflón recuperó familias con
PTFE en la descripción, además de cinta. Los indicadores de truncamiento evitan
afirmar certeza, pero se requiere mejorar cobertura y presentación de candidatos
antes de usar esta v1 para completar automáticamente una cotización.

Ocho `SELECT 1` posteriores respondieron correctamente. Al no haber timeouts, no
se comprobó cancelación del statement; no se forzaron esperas, trazas ni cambios
de servidor para provocarla. DEV directo queda pendiente. Las muestras HTTP del
reporte existente no ejecutan este DAO. El PR queda en borrador, sin afirmar que
esté listo para producción.

La comprobación de compatibilidad puede reproducirse sin leer datos comerciales:

```sql
SELECT DB_NAME() AS catalogo,
       CAST(SERVERPROPERTY('ServerName') AS nvarchar(128)) AS servidor,
       HAS_PERMS_BY_NAME('dbo.vw_MaestroArticulo','OBJECT','SELECT') AS puedeConsultar;
SELECT name FROM sys.columns
WHERE object_id = OBJECT_ID('dbo.vw_MaestroArticulo')
  AND name IN ('tx_itm','codZafiro','cod3L','Descripcion','unidadMedida',
               'Marca','Material','categoriaCompra','lineaCompra','claseCompra')
ORDER BY column_id;
```

Usar únicamente una conexión DEV/QA verificada, timeout de dos segundos y un
harness sin arranque de aplicación. Las credenciales y perfiles privados no se
incluyen en el repositorio. El código final pasó `mvn clean verify` con Java 21:
145 casos, 0 fallos, 0 errores y 1 omitido preexistente que requiere bases reales.

### Integración con listado ligero en pruebas, 2026-11-10

Se verificó exclusivamente el perfil DEV que corresponde al ambiente de pruebas
indicado por el solicitante, catálogo Dynamics2021. No se usó el perfil QA ni
producción, ni se modificaron configuración, objetos o permisos.

El harness temporal ejecutó el DAO y servicio reales, sin SpringApplication,
Eureka, tareas programadas ni servidor de red. Verificó destino, identidad del
servidor, catálogo y SELECT antes de consultar. Solo ejecutó lecturas.
Dos ejecuciones por familia, con el SQL final y timeout de dos segundos:

| Familia | Filas | Tiempo DAO observado |
|---|---:|---:|
| Bushing | 212 | 106–178 ms |
| Niple/neplo | 501 | 184–214 ms |
| Codo | 501 | 120–151 ms |
| Nudo | 155 | 225–232 ms |
| Válvula/llave | 501 | 228–229 ms |
| Teflón/PTFE | 104 | 206–230 ms |

Una búsqueda exacta devolvió una fila en 53 ms. Las 501 filas incluyen el centinela;
no se eliminó el límite ni se ocultó truncamiento en codos, neplos o válvulas.

MockMvc con controller, servicio y DAO reales contra la base de pruebas verificó:
- Un lote de seis familias: HTTP 200, seis resultados ambiguos y hasta tres
  candidatos por línea; 1913 ms incluyendo serialización e interpretación.
- Código exacto: identificado; código sintético inexistente: sin_coincidencia.
- Entrada inválida: HTTP 400. Todos los candidatos conservaron unidad=null.

Estas mediciones no son un SLA ni una comparación de equivalencia con la fuente
anterior. MockMvc no prueba el despliegue, gateway o red del endpoint compartido.
No se ejecutó ningún creador de cotizaciones. Se retiró únicamente el harness
temporal creado para esta comprobación antes del commit. Se conservaron las pruebas
existentes y sus regresiones de mapeo, unidad desconocida, atributos crudos,
ambigüedad, NULL y contrato HTTP. Suite final: Java 21, Maven 3.9.9, clean verify,
281 pruebas, 0 fallos, 0 errores y 1 omitida preexistente (requiere bases reales).
