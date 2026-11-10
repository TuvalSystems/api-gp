# Puente temporal de cotización borrador, solo pruebas

`POST /api/reportes/cotizacion-borrador` delega exclusivamente a
`POST /api/cotizaciones/borrador-items` de tuvalBackend. No es un reporte de
lectura: crea un borrador en Laravel cuando este lo autoriza. La ubicación en
`reportes/cotizacionborrador` es la excepción temporal solicitada por el usuario;
posteriormente se podrá trasladar a su módulo definitivo.

## Excepción acotada al prompt de reportes

Se conserva la ruta bajo `/api/reportes`, controller/service, validación local,
respuesta `success/message/data`, errores públicos sin detalles, inventario de
componentes y revisión CODEOWNERS. Se exceptúan las reglas de solo lectura,
GET/POST consultar y selección de una conexión JDBC porque el trabajo solicitado
es un puente HTTP de escritura a la implementación Laravel existente.

No se añade DAO, SQL, SP, entidad JPA, DataSource, transacción ni credencial de
base. `service/tuvalbackend` identifica el servicio remoto, no una nueva base.
Las conexiones Dynamics/Tuval, sus permisos y escaneos quedan intactos.

## Habilitación y autenticación

Está deshabilitado por defecto, sin URL ni token predeterminados. No se habilitó
ningún ambiente ni se creó un borrador real durante este trabajo.

Para una prueba posterior, un administrador debe verificar primero que tanto
api-gp como Laravel y sus conexiones apuntan a **QA, nunca producción**, y solo
entonces configurar en el servidor:

```properties
reportes.cotizacion-borrador.enabled=true
reportes.cotizacion-borrador.backend-origin=https://HOST_QA_VERIFICADO
```

El ejemplo es un marcador, no un destino utilizable. La URL solo puede ser un
origen HTTPS, con puerto opcional, sin usuario, contraseña, path salvo `/`, query
ni fragmento. La ruta de Laravel se fija en código. El llamante no puede cambiar
el destino mediante parámetros, cabeceras o cuerpo.

Debe haber al menos un perfil Spring activo y todos deben pertenecer a
`local`, `testing`, `qa` o `staging`. Cualquier otro, incluidos `dev`, `prod`,
`production`, perfiles mixtos con producción o ausencia de perfiles, bloquea el
puente con 503 aunque el flag sea true. El nombre de un perfil **no prueba** que
el servidor o la BD sean QA: la verificación del destino sigue siendo obligatoria.
No modificar perfiles ni permisos de un ambiente compartido para eludir este control.

El llamante aporta un único `Authorization: Bearer ...` existente de Laravel.
api-gp valida su forma y lo transmite solo al destino HTTPS configurado, sin
redirecciones, cookies, cabeceras de suplantación o token de servicio. Laravel
valida Sanctum, ability `web`, cuenta/perfil activos, contraseña sin cambio
pendiente y acceso al punto de venta. El proxy no concede esos permisos ni
crea tokens. Exponer también api-gp solo mediante HTTPS y controles de acceso
externos ya aprobados; no registrar Authorization ni cuerpos sensibles en proxies.

## Petición

```json
{
  "request_id": "5af09cd3-c181-461f-8637-c468f02d9638",
  "cliente_ruc": "CLIENTE_QA_CONFIRMADO",
  "pv_id": 123,
  "items": [{"codigo": "CODIGO_GP_CONFIRMADO", "cantidad": 2, "unidad": "UN"}]
}
```

Los valores son marcadores, no una selección aprobada. Requiere UUID, cliente y
PV en modo normal, y 1–20 artículos con códigos GP exactos y distintos de hasta
30 caracteres. No admite candidatos, textos de búsqueda, precios, descuentos,
estado ni campos desconocidos. No selecciona automáticamente artículos.

Cantidad opcional, mayor que cero, máximo 1000000, hasta dos decimales, sin
exponentes; admite número JSON o texto decimal. Omitirla conserva la omisión para
que Laravel aplique 1 y marque `cantidad_asumida`. `null` explícito se rechaza,
también para los demás campos opcionales. Unidad opcional hasta 30 caracteres;
Laravel verifica unidad/precisión real, actividad y elegibilidad del artículo.
PV/usuario deben ser enteros JSON positivos; `modo_prueba` es booleano JSON.

`modo_prueba=true` puede delegar `user_id` y omisiones de cliente/PV al piloto
restringido de Laravel. No se insertan defaults en api-gp ni se reemplazan datos
explícitos. Laravel continúa exigiendo su propia habilitación de piloto en QA,
actor autorizado, ability `quotation-items:pilot`, propietario configurado y
permisos de PV de ambos. El Bearer siempre identifica al actor real.

## Respuesta e idempotencia

- 201: creado; 200: reutilizado por Laravel, con el mismo UUID y contenido.
- 400: validación local antes de invocar Laravel.
- 401/403: autenticación o autorización rechazada.
- 409: conflicto de idempotencia o concurrencia en Laravel.
- 422: validación/configuración rechazada por Laravel; mensaje público genérico.
- 429: límite de solicitudes de Laravel.
- 503: puente deshabilitado, perfil o destino no permitido/configurado.
- 502: error de transporte, redirección o respuesta de Laravel no válida.
- 504: plazo total agotado, incluso durante lectura de cuerpo.
- 500: error inesperado, sin detalles internos.

```json
{
  "success": true,
  "message": "Cotizacion borrador disponible para revision.",
  "data": {
    "id": 42,
    "numero": "COT00000042",
    "estado": "borrador_app",
    "request_id": "5af09cd3-c181-461f-8637-c468f02d9638",
    "reutilizado": false,
    "usuario": {"id": 30, "codigo": "USUARIO_QA"},
    "actor": {"id": 30, "codigo": "USUARIO_QA"},
    "modo_prueba": false,
    "punto_venta": {"id": 123, "codigo": "PV_QA", "descripcion": "PV de prueba"},
    "cliente": {"id": 456, "ruc": "CLIENTE_QA_CONFIRMADO"},
    "incluidos": [{"indice": 0, "codigo": "CODIGO_GP_CONFIRMADO", "art_id": 789,
      "cantidad": 2, "cantidad_asumida": false, "unidad": "UN"}],
    "advertencias": []
  }
}
```

Se proyecta una lista permitida de campos del contrato Laravel; no se reenvían
cabeceras, errores, trazas ni campos adicionales de la respuesta remota.
Conexión máxima 5 s, operación completa máxima 30 s, respuesta máxima 64 KiB.
No hay bucle de reintentos de aplicación, fallback de escritura ni redirecciones.

Un timeout/error puede ocurrir **después de que Laravel haya guardado**. Cancelar
la solicitud HTTP no deshace una transacción remota. Reintentar con el mismo
`request_id`, actor y contenido, nunca generar otra clave para resolver una
respuesta incierta. La idempotencia y atomicidad residen únicamente en Laravel.
El puente no agrega precios, aprobación, reservas, correos ni sincronización a GP.

## Dependencia y pendientes de QA

Contrato basado en [tuvalBackend PR #250](https://github.com/TuvalSystems/tuvalBackend/pull/250),
commit `3b8e5c9c79863ee9179282ca390738bee5b80947`, aún abierto y sin merge al implementar.
Antes de usar el puente se requiere revisión y despliegue autorizado de esa ruta,
revisar DDL real (`borrador_app`, MySQL/InnoDB) y aplicar, con autorización y destino
verificado, la migración `2026_11_09_000001_create_quotation_item_draft_requests_table.php`.
No ejecutar todas las migraciones pendientes de forma indiscriminada.

No se hizo merge, despliegue, migración, cambio de credenciales/abilities ni prueba
contra sistemas reales. Las pruebas locales usan mocks y datos sintéticos: no
certifican disponibilidad de Laravel, certificados del destino, permisos reales,
DDL, idempotencia o transacciones sobre la BD de QA.

## Verificación reproducible

Con JDK 21 y Maven, ejecutar `bash ./mvnw clean test` y
`bash ./mvnw clean verify`. CI ejecuta verify para PR hacia develop, sin desplegar.

Se conservaron pruebas permanentes HTTP/cliente para autenticación, validación,
campos desconocidos, null vs omisión, cantidades, límites, denegaciones Laravel,
proyección de respuestas, redirecciones, bloqueo de producción, cancelación y
timeout total. `ModuleDatabaseWiringTest` verifica los tres componentes nuevos
sin conexiones reales. No se cambió ningún test preexistente; solo su inventario.

Se registró explícitamente un único test temporal,
`src/test/java/tuval/api/web/reportes/cotizacionborrador/CotizacionBorradorTemporalTest.java`,
para comprobar serialización decimal explícita. Se ejecuta con la suite completa,
se elimina únicamente ese archivo tras pasar, y se repite `clean test` antes del
commit. El PR registra resultados de ambas ejecuciones y de la suite final.

Resultados locales (2026-11-09), Maven 3.9.9 y JDK 21:

- `clean test` con temporal: 239 casos, 238 aprobados, 1 omitido preexistente.
- Después de eliminar solo el temporal: `clean test`, 238 casos, 237 aprobados,
  1 omitido preexistente, sin fallos ni errores.
- `clean verify`: los mismos 238 casos, sin fallos ni errores; JAR construido.
- La prueba omitida es `WebApplicationTests`, ya deshabilitada en develop porque
  necesita bases reales. No se cambió esa decisión.
- En el entorno aislado se añadió al comando
  `-DargLine=-javaagent:/ruta/mockito-core-5.14.2.jar` para inicializar el Mockito
  existente sin depender de su auto-adjunción. No se cambiaron dependencias ni
  configuración de pruebas del repositorio para resolverlo.

## Contrato posterior de pruebas (2026-11-10)

El contrato real actual de tuvalBackend/develop es GET /cotizaciones/borrador-items
con usuario 6 fijo y sin idempotencia. Este puente antiguo se conserva sin cambios
y NO es un fallback para el contrato nuevo. La integracion del resolver usa un
cliente separado: [resolver y crear borrador](resolucion-articulos-borrador.md).
