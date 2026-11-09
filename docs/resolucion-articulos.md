# Consulta de resolución de artículos (v1)

`POST /api/reportes/resolucion-articulos/consultar` recibe líneas extraídas por el
asistente de una imagen o transcripción. Cada línea se resuelve de forma independiente,
en el orden recibido. Es una consulta: no cotiza, consulta existencias, selecciona
bodega California ni crea documentos. No usa IA externa.

## Contrato

Se utiliza POST con JSON porque el filtro es un lote estructurado. El envoltorio es
`ResponseJsonUtils`: HTTP 200 con `success`, `message`, `data` (lista de resultados);
HTTP 400 con mensaje genérico para entradas inválidas; HTTP 500 con mensaje genérico
para errores internos. Ningún error devuelve mensajes del driver, SQL o servidores.

```http
POST /api/reportes/resolucion-articulos/consultar
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

Los códigos y la unidad conservan NULL si la vista no los informa. `atributos` solo
contiene datos reconocidos: `medidas`, `unidadMedidas`, `material`, `clase`, `refuerzo`,
`angulo`, `rosca`, `cedula`, `construccion`, `gradoMaterial`, `marca`, `paso`.
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
       "unidad":"PZAS", "atributos":{"medidas":"1 x 1/2","clase":"150"}},
      {"codigoGP":"022-000084-I", "codigoZafiro":null, "codigo3L":null,
       "descripcion":"MECH BUSHING GALVANIZADO 300 UL/FM 1\" X 1/2\" NPT",
       "unidad":"PZAS", "atributos":{"medidas":"1 x 1/2","clase":"300"}}
    ],
    "datosFaltantes":["unidadMedidas","rosca","clase_o_refuerzo"],
    "diferencias":{"clase":["150","300"]},
    "busquedaCompleta":true,
    "busquedaTruncada":false,
    "candidatosTruncados":false,
    "observaciones":["reglas_limitadas_al_vocabulario_documentado"]
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
un codo 150 también puede ser reforzado. Las marcas solo se reconocen si aparecen
literalmente en la petición y en el campo Marca de las filas exploradas.

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

Código en `tuval.api.web.reportes.resolucionarticulos`, con Controller →
Service/dynamics → DAO/dynamics, DTO y Request. Conexión exclusiva mediante
`@Qualifier("dynamicsNamedParameterJdbcTemplate")`; origen `dbo.vw_MaestroArticulo`.
Se seleccionan explícitamente diez campos descriptivos conocidos: tx_itm, codZafiro,
cod3L, Descripcion, unidadMedida, Marca, Material, categoriaCompra, lineaCompra y
claseCompra. El DTO público solo expone identificadores, descripción, unidad y
atributos interpretados. No reutiliza el DTO financiero de ReporteArticulos.

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
DataSource/driver. Como máximo se inician seis statements. El orden SQL usa los diez
campos proyectados; empates idénticos o cambios concurrentes no prueban unicidad.

La recuperación por familia usa anclas cerradas de descripción (incluidos los
sinónimos documentados y las grafías VÁLVULA, UNIÓN UNIVERSAL y TEFLÓN), no la frase
entera de la petición. Evita enviar el listado
al asistente, pero `UPPER(Descripcion) LIKE '%ancla%'` y el orden todavía pueden
escanear la vista. El límite de filas y el timeout no garantizan una consulta rápida.
No se agregan tablas, índices, objetos SQL, hints, cambios de aislamiento, conexiones
ni cachés persistentes. Tampoco se precarga el maestro al arrancar.

Si las familias frecuentes truncan o expiran, el pendiente es medir en DEV/QA y
acordar una fuente de búsqueda propia: copia descriptiva controlada y actualizable,
con cobertura/antigüedad explícitas. Eso requiere una decisión posterior; esta v1
mantiene la respuesta incompleta y no fabrica certeza para compensar el rendimiento.

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
Esto valida muestras de formato, **no** la nueva consulta SQL ni su rendimiento.
Pendientes en DEV/QA: ejecutar esta API y contrastar cobertura por familia,
timeout/cancelación, collation/acentos, permisos SELECT, NULL/duplicados reales,
presupuesto bajo carga y controles de acceso externos. No hay prueba de despliegue
ni de reglas de habilitación comercial.

### Comprobación real de QA, 2026-11-09

Se ejecutó el DAO de esta rama mediante un harness Java independiente, sin
SpringApplication, contexto Spring, Eureka, jobs ni servidor HTTP. Se reutilizó
exclusivamente el perfil QA existente y se verificó antes de las consultas el
destino configurado, distinto de producción. SQL Server devolvió la identidad
`SRV-SQL2-ENTORN\PRUEBAS`, catálogo `Dynamics2021`, y permiso SELECT sobre la vista.
Solo se ejecutaron SELECT; no se cambiaron bases, objetos, permisos ni aislamiento.

El SQL nuevo **no pudo ejecutarse satisfactoriamente en QA**: devolvió
`UncategorizedSQLException`, SQLState `S0001`, código SQL Server `207`. Una consulta
posterior a `sys.columns` confirmó que esa versión de `dbo.vw_MaestroArticulo`
contiene tx_itm, codZafiro, cod3L, Descripcion, unidadMedida, Marca y Material, pero
no contiene **categoriaCompra, lineaCompra ni claseCompra**, requeridas por la
proyección actual. No se sustituyeron campos ni se modificó la vista.

Se intentaron las seis familias y un lote mixto; todos fallaron antes de devolver
filas. Las latencias por intento fueron: bushing 7.6–103.1 ms (dos intentos), codo
8.5 ms, niple 7.9 ms, nudo 9.4 ms, válvula/llave 11.0 ms y teflón 6.7 ms. También
fallaron con el mismo código las 22 consultas de referencia por código, en
5.0–10.2 ms. **Son latencias de error de compilación, no de búsquedas exitosas**.
Ocho comprobaciones `SELECT 1` posteriores respondieron correctamente.

No se observaron timeouts; por tanto, no se comprobaron cancelación del statement,
filas reales, ambigüedades, fracciones ni truncamiento contra el SQL nuevo. Esos
casos sí están cubiertos por las pruebas locales sin base, pero no sustituyen la
integración. La comprobación directa de DEV queda pendiente; las muestras HTTP
del reporte existente descritas arriba no ejecutan este DAO y no demuestran su
rendimiento. El PR se entrega en borrador para revisar este bloqueo, sin afirmar
que esté listo para producción.

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
