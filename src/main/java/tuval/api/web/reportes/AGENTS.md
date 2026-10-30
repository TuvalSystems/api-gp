# APIs del módulo de reportes

Antes de crear o modificar un reporte, leer `docs/prompt-apis-reportes.md` y
`docs/estructura-modular.md` desde la raíz del repositorio.

- Ubicar cada funcionalidad bajo `tuval.api.web.reportes.<funcionalidad>` y sus
  rutas bajo `/api/reportes/<reporte>`.
- Mantener controller -> service/<base> -> dao/<base>, con dto y request cuando
  hagan falta; entity/<base> solo para JPA justificado. No crear capas vacías.
- Exigir selección explícita `tuval` o `dynamics` y definición SQL verificada.
  No inventar objetos ni contratos de resultados.
- Inyectar el template con `@Qualifier` explícito. Dynamics es `@Primary`;
  nunca usar la conexión por omisión.
- Consultar únicamente: verificar que los SP no modifican datos persistentes.
  Parametrizar valores, fijar identificadores SQL en código y limitar resultados.
- Reutilizar `common.response.ResponseJsonUtils` y no exponer SQL en errores.
- No cambiar conexiones ni módulos ajenos. Registrar escaneo JPA únicamente en
  la configuración de la base elegida si se agregan repositorios/entidades JPA.
- Crear pruebas en `src/test/java/tuval/api/web/reportes/<funcionalidad>` y mantener
  `src/test/resources/architecture/components.txt`; actualizar inventarios JPA
  solo cuando cambie su escaneo.
- Ejecutar `mvn clean test` y `git diff --check` antes de entregar cambios de código.
  Indicar por separado cualquier validación pendiente en SQL Server/QA.
