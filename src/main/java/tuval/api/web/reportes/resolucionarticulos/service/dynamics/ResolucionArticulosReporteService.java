package tuval.api.web.reportes.resolucionarticulos.service.dynamics;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.resolucionarticulos.dao.dynamics.ResolucionArticulosReporteDao;
import tuval.api.web.reportes.resolucionarticulos.dto.CatalogoArticuloDto;
import tuval.api.web.reportes.resolucionarticulos.dto.ResolucionArticulosReporteDto;
import tuval.api.web.reportes.resolucionarticulos.dto.ResolucionArticulosReporteDto.Candidato;
import tuval.api.web.reportes.resolucionarticulos.request.ResolucionArticulosReporteRequest;

@Service
public class ResolucionArticulosReporteService {
  private final ResolucionArticulosReporteDao dao;
  private static final long PRESUPUESTO_NANOS = 10_000_000_000L;
  private static final int MAX_BUSQUEDAS = 6;
  private record Fuente(List<CatalogoArticuloDto> filas, boolean completa, boolean truncada, String motivo) {}

  public ResolucionArticulosReporteService(ResolucionArticulosReporteDao dao) { this.dao = dao; }

  public List<ResolucionArticulosReporteDto> consultar(ResolucionArticulosReporteRequest request) {
    validar(request);
    int max = request.maxCandidatos() == null ? 3 : request.maxCandidatos();
    long inicio = System.nanoTime();
    // Request-scoped reuse: no persistent cache, freshness assumptions or infrastructure.
    Map<String, Fuente> fuentes = new HashMap<>();
    List<ResolucionArticulosReporteDto> results = new ArrayList<>();
    for (var linea : request.renglones()) {
      var spec = ReglasArticulo.interpretar(linea.texto());
      String codigo = codigo(linea.texto());
      String key = codigo != null ? "codigo:" + codigo : String.valueOf(spec.familia());
      Fuente fuente = fuentes.get(key);
      if (fuente == null) {
        if (codigo == null && spec.familia() == null) {
          fuente = new Fuente(List.of(), false, false, "fuera_del_vocabulario_soportado");
        } else if (fuentes.size() >= MAX_BUSQUEDAS || System.nanoTime() - inicio >= PRESUPUESTO_NANOS) {
          fuente = new Fuente(List.of(), false, false, "presupuesto_de_busqueda_agotado");
        } else {
          try {
            var filas = dao.buscar(codigo == null ? spec.familia() : null, codigo);
            boolean truncada = filas.size() > ResolucionArticulosReporteDao.LIMITE;
            fuente = new Fuente(List.copyOf(filas.subList(0, Math.min(filas.size(),
                ResolucionArticulosReporteDao.LIMITE))), !truncada, truncada,
                truncada ? "limite_de_exploracion" : "");
          } catch (QueryTimeoutException exception) {
            fuente = new Fuente(List.of(), false, false, "timeout_de_busqueda");
          }
        }
        fuentes.put(key, fuente);
      }
      results.add(resolver(results.size(), linea, spec, codigo, fuente, max));
    }
    return results;
  }

  private ResolucionArticulosReporteDto resolver(int indice,
      ResolucionArticulosReporteRequest.Renglon linea, ReglasArticulo.Especificacion spec,
      String codigo, Fuente fuente, int max) {
    var attrs = new LinkedHashMap<>(spec.atributos());
    var pending = new ArrayList<>(spec.pendientes());
    // Unvalidated source attributes must not complete or contradict a specification.
    var interpreted = new ReglasArticulo.Especificacion(spec.familia(), attrs, pending);
    Set<String> faltantes = codigo == null ? new LinkedHashSet<>(ReglasArticulo.faltantes(interpreted))
        : new LinkedHashSet<>();
    List<Candidato> compatibles = new ArrayList<>();
    for (var row : fuente.filas()) {
      var observed = ReglasArticulo.interpretar(row.descripcion());
      if (codigo == null && observed.familia() != null && observed.familia() != spec.familia()
          && !observed.pendientes().contains("tipos_articulo_multiples")) continue;
      var candidateAttrs = new LinkedHashMap<>(observed.atributos());
      boolean contradice = false;
      Set<String> desconocidos = new LinkedHashSet<>();
      if (codigo == null) {
        if (observed.familia() == null) desconocidos.add("catalogo:tipo_articulo");
        for (String pendiente : observed.pendientes()) {
          if (!pendiente.startsWith("termino:")) desconocidos.add("catalogo:" + pendiente);
        }
        for (var attr : attrs.entrySet()) {
          String actual = candidateAttrs.get(attr.getKey());
          if (actual == null || atributoIncierto(attr.getKey(), observed.pendientes())) {
            desconocidos.add("catalogo:" + attr.getKey());
          }
          else if (!actual.equals(attr.getValue())) contradice = true;
        }
      }
      if (!contradice) {
        faltantes.addAll(desconocidos);
        if (row.codigoGP() == null || row.codigoGP().isBlank()) faltantes.add("catalogo:codigoGP");
        // Preserve the original raw names separately from inferred business attributes.
        if (row.esquemaUnidad() != null) candidateAttrs.put("esquemaUnidad", row.esquemaUnidad());
        if (row.atributoMarca() != null) candidateAttrs.put("tx_atrMar", row.atributoMarca());
        if (row.atributoMaterial() != null) candidateAttrs.put("tx_atrMat", row.atributoMaterial());
        compatibles.add(new Candidato(row.codigoGP(), row.codigoZafiro(), row.codigo3L(),
            row.descripcion(), null, candidateAttrs));
      }
    }
    String estado = !fuente.completa() ? "ambiguo" : compatibles.isEmpty() ? "sin_coincidencia"
        : compatibles.size() == 1 && faltantes.isEmpty() ? "identificado" : "ambiguo";
    List<String> obs = new ArrayList<>();
    obs.add("fuente_listado_items");
    obs.add("unidad_comercial_no_disponible");
    obs.add("atributos_fuente_sin_equivalencia_comercial");
    if (!fuente.motivo().isEmpty()) obs.add(fuente.motivo());
    if (codigo == null) obs.add("reglas_limitadas_al_vocabulario_documentado");
    boolean recortados = compatibles.size() > max;
    if (recortados) obs.add("limite_de_candidatos");
    return new ResolucionArticulosReporteDto(indice, linea.texto(),
        linea.cantidad() == null ? BigDecimal.ONE : linea.cantidad(), linea.cantidad() == null,
        estado, estado.equals("identificado") ? compatibles.getFirst() : null,
        List.copyOf(compatibles.subList(0, Math.min(max, compatibles.size()))),
        List.copyOf(faltantes), diferencias(compatibles), fuente.completa(), fuente.truncada(),
        recortados, List.copyOf(obs));
  }

  private static boolean atributoIncierto(String key, List<String> pendientes) {
    return (key.equals("medidas") && (pendientes.contains("medidas")
            || pendientes.contains("formato_medidas_no_soportado")))
        || (key.equals("clase") && pendientes.contains("clases_multiples"))
        || (key.equals("material") && pendientes.contains("materiales_multiples"))
        || (key.equals("unidadMedidas") && pendientes.contains("unidades_mixtas"))
        || (Set.of("angulo", "refuerzo", "rosca", "construccion", "cedula", "gradoMaterial")
            .contains(key) && pendientes.contains("atributos_multiples"));
  }

  private static Map<String, List<String>> diferencias(List<Candidato> candidatos) {
    Map<String, List<String>> result = new LinkedHashMap<>();
    for (String key : List.of("medidas", "material", "clase", "refuerzo", "angulo", "rosca",
        "cedula", "construccion", "gradoMaterial", "marca", "unidadMedidas", "paso")) {
      Set<String> values = new LinkedHashSet<>();
      for (var candidato : candidatos) values.add(candidato.atributos().getOrDefault(key, "no_especificado"));
      if (values.size() > 1) {
        var shown = new ArrayList<>(values.stream().limit(5).toList());
        if (values.size() > 5) shown.add("otros_valores_no_mostrados");
        result.put(key, List.copyOf(shown));
      }
    }
    return result;
  }

  private static String codigo(String text) {
    String value = ReglasArticulo.normalizar(text);
    if (value.matches("\\d{3}-\\d{6}-[A-Z]")) return value;
    if (value.matches("CODIGO [A-Z0-9][A-Z0-9._/-]{0,30}")) return value.substring(7);
    return null;
  }

  private static void validar(ResolucionArticulosReporteRequest request) {
    if (request == null || request.renglones() == null || request.renglones().isEmpty()
        || request.renglones().size() > 20 || (request.maxCandidatos() != null &&
        (request.maxCandidatos() < 1 || request.maxCandidatos() > 5))) throw new IllegalArgumentException();
    for (var line : request.renglones()) {
      if (line == null || line.texto() == null || line.texto().isBlank() || line.texto().length() > 200
          || (line.cantidad() != null && (line.cantidad().signum() <= 0
          || line.cantidad().compareTo(new BigDecimal("1000000")) > 0
          || line.cantidad().scale() > 3))) throw new IllegalArgumentException();
    }
  }
}
