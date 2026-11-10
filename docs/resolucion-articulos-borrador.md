# Resolver articulos y crear un borrador de prueba

## Alcance y contrato comprobado

Nueva operacion explicita: `POST /api/reportes/resolucion-articulos/crear-borrador`.
`POST /api/reportes/resolucion-articulos/consultar` conserva su contrato y nunca crea borradores.
La nueva operacion reutiliza el resolver, su DAO Dynamics y sus limites. No agrega SQL,
conexiones, precios, descuentos, reservas, aprobaciones ni sincronizacion a GP.

Se verifico el 2026-11-10 el codigo de tuvalBackend/develop:
- [Ruta web](https://github.com/TuvalSystems/tuvalBackend/blob/develop/routes/web.php):
  `GET /cotizaciones/borrador-items`, sin middleware web.
- [Request](https://github.com/TuvalSystems/tuvalBackend/blob/develop/app/Http/Requests/Venta/Cotizaciones/StoreQuotationItemsDraftRequest.php):
  cuerpo JSON con `cliente_ruc`, `pv_id`, `items`; solo local/testing/qa/staging.
- [Controller](https://github.com/TuvalSystems/tuvalBackend/blob/develop/app/Http/Controllers/Ventas/Cotizaciones/Borradores/CotizacionBorradorController.php):
  usuario fijo 6, creacion 201 con success/message/data.
- [Servicio](https://github.com/TuvalSystems/tuvalBackend/blob/develop/app/Services/Ventas/Cotizaciones/Borradores/QuotationItemsDraftService.php):
  valida articulos/unidad/precision y guarda borrador en transaccion. No acepta request_id ni tiene idempotencia persistente.

Este contrato NO es el anterior `POST /api/cotizaciones/borrador-items` con Bearer/UUID.
El puente anterior `/api/reportes/cotizacion-borrador` queda intacto y no se usa como fallback.
No se verifico el despliegue remoto ni se creo un borrador real en esta tarea.

## Peticion

```json
{
  "request_id": "e0f7a456-2026-4000-8000-000000000001",
  "renglones": [
    {"texto": "020-000006-I", "cantidad": 2}
  ]
}
```

Usar una UUID nueva para una nueva operacion intencional. La UUID de arriba es solo un ejemplo.
Cliente omitido: `0990007020001`. Punto de venta omitido: `2` (California).
Cantidad omitida: `1`, conservando `cantidadAsumida` en la resolucion.
`cliente_ruc` y `pv_id` explicitos se respetan. El usuario 6 lo fija Laravel; no se envia ni acepta `user_id`.

La creacion admite 1-20 renglones, texto de hasta 200 caracteres, cantidades positivas
hasta 1000000 con maximo 2 decimales (limite real del destino), sin exponentes ni redondeo.
Se rechazan null explicitos, precios, unidades, URLs y campos desconocidos. El contrato
original de consulta sigue admitiendo 3 decimales; no significa que se puedan persistir sin redondear.

Solo se crea cuando TODOS los renglones quedan resueltos. Un resultado `identificado`
con busqueda completa y sin datos faltantes puede usarse. Un ambiguo, incompleto o
sin coincidencias nunca se selecciona automaticamente y devuelve 422 sin llamada de escritura.
Los codigos repetidos se rechazan, no se suman cantidades automaticamente.

Para una seleccion humana explicita:

```json
{
  "request_id": "e0f7a456-2026-4000-8000-000000000002",
  "cliente_ruc": "0990007020001",
  "pv_id": 2,
  "renglones": [
    {"texto": "bushing galvanizado 1 por 1/2", "cantidad": 1,
     "codigoSeleccionado": "020-000006-I"}
  ]
}
```

El ejemplo no asegura que ese codigo aparezca en ese catalogo. La seleccion debe aparecer
exactamente una vez entre los candidatos de la consulta actual (maximo 5 por renglon).
Si no aparece, refinar el texto o consultar el codigo exacto; nunca se acepta un codigo arbitrario.
Una seleccion explicita puede resolver una busqueda truncada, pero no inventa unidad
comercial: Laravel sigue validando unidad base, precision y elegibilidad. No se envian precios.

## Envio remoto y respuesta

Solo se envia este cuerpo, por GET al path fijo `/cotizaciones/borrador-items`:

```json
{"cliente_ruc":"0990007020001","pv_id":2,"items":[{"codigo":"020-000006-I","cantidad":2}]}
```

El GET mutante es compatibilidad temporal de pruebas, no un patron recomendado.
El nuevo endpoint publico es POST y no convierte GET entrantes en escrituras.
El cliente Apache HttpClient 5 deshabilita reintentos automaticos, redirecciones,
autenticacion, cookies y descompresion. No usa proxy/credenciales del sistema ni
reenvia cabeceras recibidas. Conexion hasta 5 segundos, operacion total 30 segundos
(incluye lectura lenta), respuesta hasta 64 KiB, cancelacion al exceder el limite.
Se agrega solo la dependencia gestionada por Spring Boot, sin cambiar versiones existentes.
No se usa java.net.http.HttpClient para este GET: sus reintentos internos consideran GET idempotente.

- 201: creacion confirmada por respuesta valida de Laravel. `data.borrador` contiene
  id, numero, estado, usuario, punto de venta, cliente e incluidos; `data.resoluciones`
  conserva los candidatos, cantidades y omisiones asumidas.
- 200: la misma instancia devuelve un exito anterior de la misma UUID/contenido,
  con `data.repeticion_local=true`. NO implica idempotencia en Laravel.
- 400: entrada invalida, sin consultas ni escritura.
- 422 con `data.creado=false`: resolver incompleto, seleccion faltante/invalida o duplicados;
  se incluyen resoluciones para corregir el lote. Tambien se conserva 422 remoto, sin errores internos.
- 409: UUID en curso/incierta o reutilizada con contenido distinto.
- 401/403/429: rechazo remoto, sin transmitir su cuerpo.
- 503: disabled, entorno/destino no permitido, o registro local lleno.
- 502/504: resultado remoto no confirmable, sin afirmar rollback o no creacion.
- 500: error inesperado, sin SQL, direcciones internas ni trazas.

Todas las respuestas incluyen Cache-Control: no-store. Los errores remotos/incertidumbre
indican `reintentable:false`. Ante respuesta incierta revisar Laravel antes de otro intento,
no generar otra UUID para eludir el bloqueo. Un timeout no revierte una creacion ya guardada.

## Proteccion local y limites de duplicados

Una reserva atomica por UUID y fingerprint evita envios simultaneos o repetidos en la
misma instancia. Guarda exitos para reproducirlos y retiene indefinidamente resultados
inciertos. No expulsa entradas: a 1000 UUID retenidas bloquea nuevas operaciones con 503.
Las resoluciones rechazadas antes del envio liberan su reserva para corregirlas.

NO hay garantia exactamente-una-vez: el registro se pierde al reiniciar y no se comparte
entre replicas. Cambiar UUID, repetir a otra instancia o un retry de proxy puede duplicar
borradores. No habilitar balanceo/reintentos de esta ruta ni reiniciar para sortear un 409/503.
Ante registro lleno, conciliar las operaciones antes de cualquier reinicio. Una evolucion
productiva exige POST y clave idempotente persistente en Laravel; no se agrego infraestructura
ni persistencia nueva sin autorizacion. El flujo consumidor debe desactivar doble envio.

## Habilitacion solo en pruebas

Deshabilitado por defecto; no se modificaron archivos de propiedades privados ni servidores.
Tras verificar explicitamente que api-gp, Laravel y ambas conexiones apuntan a pruebas,
un administrador puede configurar:

```properties
reportes.resolucion-borrador.enabled=true
reportes.resolucion-borrador.backend-origin=https://HOST_QA_VERIFICADO
```

El origen es configuracion de servidor, no entrada HTTP, sin path, credenciales, query o fragmento.
Exige perfiles activos no vacios y todos en `local`, `testing`, `qa`, `staging`.
`dev`, `prod`, desconocidos y mezclas con produccion se bloquean. No renombrar perfiles
compartidos para eludirlo: un nombre de perfil no demuestra el destino de las bases.

La URL que entrego el usuario usa HTTP. Solo para el piloto y con verificacion previa del
destino de pruebas, puede configurarse `backend-origin=http://tuvalapi.cosenco.ec` y
`reportes.resolucion-borrador.allow-insecure-http=true`. Este flag no envia credenciales,
pero RUC y articulos seguirian viajando sin cifrar. HTTPS valido es preferible. No se
habilito este permiso ni se transmitieron datos reales durante las pruebas automatizadas.

La ruta de Laravel carece de autenticacion y actua como usuario 6. Mantener ambos extremos
restringidos por los controles externos de acceso ya existentes; no exponerlos a Internet.
No se crea autenticacion ficticia en api-gp ni se amplian permisos existentes.

## Verificacion

JDK 21 y Maven 3.9.9, `mvn -o -B clean test` y luego `mvn -o -B clean verify`.
Pruebas permanentes ampliadas en los archivos existentes de HTTP del resolver y cliente
borrador; mocks del DAO/servicio y servidores loopback, sin bases ni backend reales.
Cubren seleccion, defaults, precision, campos desconocidos, lotes parciales, concurrencia,
replays locales, corte tras recibir el cuerpo (un solo envio), redirect, timeout de cuerpo,
maximo de bytes y guards de configuracion. Se actualiza inventario de componentes.

Se registro un unico temporal: `src/test/java/tuval/api/web/reportes/resolucionarticulos/ResolucionBorradorTemporalTest.java`,
para el limite de 1000 reservas. Tras pasar se retira solo ese archivo y se repite la suite
limpia. No se eliminan tests preexistentes. Resultados exactos en el PR.

Pendientes antes de una prueba real autorizada: confirmar deployment/path exacto, ambiente
y catalogos, unidad/precision real de cada articulo y permisos del usuario 6 para cliente/PV.
Las pruebas locales no certifican esos puntos. No hay merge, despliegue ni migracion.

Resultados locales finales (2026-11-10): clean test con temporal, 321 casos,
0 fallos, 0 errores, 1 omitido preexistente. Tras retirar solo el temporal,
clean test y clean verify: 320 casos, 0 fallos, 0 errores, 1 omitido en ambas
ejecuciones. JAR construido y verificado sin perfiles privados. Inventario de
tests previos conservado y git diff --check correcto.
