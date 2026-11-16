package tuval.api.web.reportes.inventario.articulos.service.dynamics.imp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tuval.api.web.reportes.inventario.articulos.dto.CatalogoArticuloDto;

/** Request-independent inverted index; BM25 ranks words and edit distance tolerates typos. */
final class IndiceArticulos {
  private record Aparicion(int documento, int frecuencia) {}
  private record Resultado(int documento, double puntaje, double porcentaje) {}
  record CoincidenciaTexto(CatalogoArticuloDto articulo, double porcentaje) {}
  private final List<CatalogoArticuloDto> articulos;
  private final Map<String, List<Aparicion>> indice = new HashMap<>();
  private final int[] longitudes;
  private final double longitudMedia;

  IndiceArticulos(List<CatalogoArticuloDto> filas) {
    // Preserve different codes, but avoid duplicate suggestions for the same article.
    var unicos = new LinkedHashMap<List<String>, CatalogoArticuloDto>();
    for (var fila : filas) {
      unicos.putIfAbsent(Arrays.asList(fila.codigoGP(), fila.codigo3L(), fila.descripcion()), fila);
    }
    articulos = List.copyOf(unicos.values());
    longitudes = new int[articulos.size()];
    long total = 0;
    for (int i = 0; i < articulos.size(); i++) {
      var palabras = BusquedaTextoArticulo.palabras(articulos.get(i).descripcion());
      for (var palabra : palabras.entrySet()) {
        indice.computeIfAbsent(palabra.getKey(), key -> new ArrayList<>()).add(new Aparicion(i, palabra.getValue()));
        longitudes[i] += palabra.getValue();
      }
      total += longitudes[i];
    }
    longitudMedia = articulos.isEmpty() ? 1 : Math.max(1, (double) total / articulos.size());
  }

  List<CoincidenciaTexto> buscar(BusquedaTextoArticulo.Consulta consulta, int maximo) {
    if (consulta.terminos().isEmpty()) return List.of();
    Map<Integer, Double> relevancia = new HashMap<>();
    Map<Integer, Double> cobertura = new HashMap<>();
    Map<Integer, Integer> coincidencias = new HashMap<>();
    Set<Integer> principales = Set.of();
    int total = consulta.terminos().stream().mapToInt(BusquedaTextoArticulo.TerminoResolucion::peso).sum();
    for (var termino : consulta.terminos()) {
      var alternativas = alternativas(termino);
      Map<Integer, Double> mejor = new HashMap<>();
      Map<Integer, Double> calidad = new HashMap<>();
      for (var alternativa : alternativas.entrySet()) {
        var apariciones = indice.get(alternativa.getKey());
        double idf = Math.log(1 + (articulos.size() - apariciones.size() + 0.5) / (apariciones.size() + 0.5));
        for (var aparicion : apariciones) {
          double tf = aparicion.frecuencia();
          double bm25 = idf * tf * 2.2 / (tf + 1.2 * (0.25 + 0.75 * longitudes[aparicion.documento()] / longitudMedia));
          mejor.merge(aparicion.documento(), bm25 * alternativa.getValue(), Math::max);
          calidad.merge(aparicion.documento(), alternativa.getValue(), Math::max);
        }
      }
      if (termino.principal()) principales = Set.copyOf(mejor.keySet());
      for (var entrada : mejor.entrySet()) {
        relevancia.merge(entrada.getKey(), entrada.getValue() * termino.peso(), Double::sum);
        cobertura.merge(entrada.getKey(), termino.peso() * calidad.get(entrada.getKey()), Double::sum);
        coincidencias.merge(entrada.getKey(), 1, Integer::sum);
      }
    }
    List<Resultado> resultados = new ArrayList<>();
    for (int documento : principales) {
      // One isolated product word in a longer query is not enough (e.g. contact cleaner).
      if (consulta.terminos().size() > 1 && coincidencias.getOrDefault(documento, 0) < 2) continue;
      double proporcion = cobertura.getOrDefault(documento, 0.0) / total;
      // Expose text coverage separately from the unbounded BM25 ranking score.
      resultados.add(new Resultado(documento, relevancia.getOrDefault(documento, 0.0),
          proporcion * 100));
    }
    resultados.sort(Comparator.comparingDouble(Resultado::porcentaje).reversed()
        .thenComparing(Comparator.comparingDouble(Resultado::puntaje).reversed())
        .thenComparingInt(Resultado::documento));
    return resultados.stream().limit(maximo)
        .map(r -> new CoincidenciaTexto(articulos.get(r.documento()), r.porcentaje())).toList();
  }

  private Map<String, Double> alternativas(BusquedaTextoArticulo.TerminoResolucion termino) {
    Map<String, Double> resultado = new HashMap<>();
    for (String variante : termino.variantes()) {
      String canonico = BusquedaTextoArticulo.canonico(variante);
      if (indice.containsKey(canonico)) resultado.put(canonico, 1.0);
    }
    // Nearby unit-normalized text is a lower-ranked suggestion, not an equivalence.
    if (termino.texto().matches("[0-9]+(?:[.][0-9]+)?MM")) {
      double solicitado = Double.parseDouble(termino.texto().replace("MM", ""));
      indice.keySet().stream().filter(p -> p.matches("[0-9]+(?:[.][0-9]+)?MM"))
          .map(p -> Map.entry(p, Math.abs(Double.parseDouble(p.replace("MM", "")) - solicitado) / solicitado))
          .filter(p -> p.getValue() > 0 && p.getValue() <= 0.20)
          .sorted(Map.Entry.comparingByValue()).limit(5)
          .forEach(p -> resultado.put(p.getKey(), 0.65 - p.getValue()));
    }
    // Never edit-distance-match a plain number or a manufacturer's model number.
    if (resultado.isEmpty() && termino.texto().matches("[A-Z]{4,}")) {
      int limite = termino.texto().length() >= 7 ? 2 : 1;
      for (String palabra : indice.keySet()) {
        if (palabra.matches("[A-Z]{4,}") && Math.abs(palabra.length() - termino.texto().length()) <= limite) {
          int distancia = distancia(termino.texto(), palabra);
          if (distancia <= limite) resultado.put(palabra, 1.0 - 0.15 * distancia);
        }
      }
    }
    return resultado;
  }

  private static int distancia(String a, String b) {
    int[] anterior = new int[b.length() + 1];
    for (int j = 0; j <= b.length(); j++) anterior[j] = j;
    for (int i = 1; i <= a.length(); i++) {
      int[] actual = new int[b.length() + 1];
      actual[0] = i;
      for (int j = 1; j <= b.length(); j++) {
        actual[j] = Math.min(Math.min(actual[j - 1] + 1, anterior[j] + 1),
            anterior[j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1));
      }
      anterior = actual;
    }
    return anterior[b.length()];
  }
}
