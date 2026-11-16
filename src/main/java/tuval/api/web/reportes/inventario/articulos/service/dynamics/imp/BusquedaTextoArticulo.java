package tuval.api.web.reportes.inventario.articulos.service.dynamics.imp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Text retrieval and weighted overlap, independent of product families or attribute schemas. */
final class BusquedaTextoArticulo {
  private static final Pattern TOKENS = Pattern.compile(
      "[A-Z]+\\d+[A-Z0-9]*(?:[-./][A-Z0-9]+)*|\\d{1,2}\\s+\\d+/\\d+|\\d+/\\d+|\\d+(?:[.,]\\d+)?|[A-Z]+");
  private static final Pattern MEDIDA = Pattern.compile(
      "(?<![A-Z0-9./])(?<numero>\\d{1,2}\\s+\\d+/\\d+|\\d+/\\d+|\\d+(?:[.,]\\d+)?)\\s*"
          + "(?<unidad>MM|CM|METROS?|MTS|MTR|MT|M|PULGADAS?|PULG|IN|\")?(?![A-Z0-9./])");
  private static final Set<String> OMITIDOS = Set.of("DE", "DEL", "EL", "LA", "LOS", "LAS", "UN", "UNA",
      "UNOS", "UNAS", "PARA", "POR", "CON", "EN", "Y", "X", "MM", "CM", "M", "MT", "MTS", "MTR",
      "METRO", "METROS", "PULGADA", "PULGADAS", "PULG", "IN", "ROLLO", "ROLLOS", "PAQ", "PAQUETE",
      "PAQUETES", "CAJA", "CAJAS", "PIEZA", "PIEZAS", "UNIDAD", "UNIDADES", "KG", "KILO", "KILOS");
  private static final Set<String> GENERICOS = Set.of("ACERO", "NEGRO", "LISO", "GRANO");
  // Linguistic aliases, not a list of supported products.
  private static final Map<String, List<String>> ALIAS = Map.ofEntries(
      Map.entry("ACERO", List.of("ACERO", "HIERRO", "AC")),
      Map.entry("NEGRO", List.of("NEGRO", "NEGRA", "NEG")),
      Map.entry("LISO", List.of("LISO", "LISA")),
      Map.entry("INOX", List.of("INOX", "INOXIDABLE", "INOXID")),
      Map.entry("GALV", List.of("GALV", "GALVANIZADO", "GALVANIZADA")),
      Map.entry("ANTIDESL", List.of("ANTIDESL", "ANTIDESLIZANTE", "CORRUGADO", "CORRUGADA", "CORRUG")),
      Map.entry("PULIR", List.of("PULIR", "DESBASTE")),
      Map.entry("CONTACTIP", List.of("CONTACTIP", "CONTACTIG", "CONTACTO")),
      Map.entry("NIPLE", List.of("NIPLE", "NEPLO")),
      Map.entry("NUDO", List.of("NUDO", "UNION UNIVERSAL")),
      Map.entry("TEFLON", List.of("TEFLON", "PTFE")),
      Map.entry("PLANCHA", List.of("PLANCHA", "PLANCHON")));

  record TerminoResolucion(String texto, List<String> variantes, int peso, boolean principal) {}
  record Consulta(List<TerminoResolucion> terminos) {}
  private record MedidaTexto(String original, String normalizada) {}

  static String normalizar(String texto) {
    String valor = texto == null ? "" : texto;
    String[] fracciones = {"¼", "1/4", "½", "1/2", "¾", "3/4", "⅛", "1/8", "⅜", "3/8", "⅝", "5/8", "⅞", "7/8"};
    for (int i = 0; i < fracciones.length; i += 2) {
      valor = valor.replaceAll("(?<=\\d)" + fracciones[i], " " + fracciones[i + 1]).replace(fracciones[i], fracciones[i + 1]);
    }
    return Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
        .toUpperCase(Locale.ROOT).replace('×', 'X').replace('⁄', '/').replace('″', '"').replace('”', '"')
        .replaceAll("(?<=\\d)\\s*/\\s*(?=\\d)", "/")
        .replaceAll("(?<=\\d)-(?=\\d+/\\d)", " ")
        .replaceAll("(?<=\\d)X(?=\\d)", " X ")
        .replaceAll("\\bG(\\d{2,3})\\b", "GRANO $1")
        .replace("UNION UNIVERSAL", "NUDO").replaceAll("\\s+", " ").trim();
  }

  static String canonico(String token) {
    if (token.matches("\\d.*")) {
      try {
        String[] mixto = token.trim().split("\\s+");
        String[] fraccion = mixto[mixto.length - 1].split("/");
        BigDecimal valor = new BigDecimal(fraccion[0].replace(',', '.'));
        if (fraccion.length == 2) valor = valor.divide(new BigDecimal(fraccion[1]), 9, RoundingMode.HALF_UP);
        if (mixto.length == 2) valor = valor.add(new BigDecimal(mixto[0]));
        return valor.stripTrailingZeros().toPlainString();
      } catch (ArithmeticException | NumberFormatException exception) { return token; }
    }
    String singular = token.matches(".*[LRN]ES$") ? token.substring(0, token.length() - 2)
        : token.endsWith("S") && token.length() > 4 ? token.substring(0, token.length() - 1) : token;
    for (var entrada : ALIAS.entrySet()) if (entrada.getValue().contains(singular)) return entrada.getKey();
    return singular;
  }

  static Consulta preparar(String texto) {
    var terminos = new LinkedHashMap<String, TerminoResolucion>();
    var matcher = TOKENS.matcher(normalizar(texto));
    while (matcher.find() && terminos.size() < 24) {
      String token = matcher.group();
      if (OMITIDOS.contains(token)) continue;
      String canonico = canonico(token);
      var variantes = new LinkedHashSet<>(ALIAS.getOrDefault(canonico, List.of(canonico)));
      variantes.add(token);
      // Keep literal and decimal spellings as text alternatives, not technical equivalence.
      if (token.matches("0[1-9]")) variantes.add("0." + token.substring(1));
      int peso = token.matches("\\d.*") ? 2 : canonico.equals("GRANO") ? 1 : GENERICOS.contains(canonico) ? 3 : 4;
      if (token.matches("[A-Z]+\\d.*")) peso = 6;
      terminos.putIfAbsent(canonico, new TerminoResolucion(canonico, List.copyOf(variantes), peso, false));
    }
    for (var medida : medidas(texto)) {
      if (terminos.size() >= 24) break;
      // A literal value and its unit conversion describe one measure, not two requirements.
      var original = terminos.get(medida.original());
      if (original != null) terminos.put(medida.original(), new TerminoResolucion(
          original.texto(), original.variantes(), 1, original.principal()));
      terminos.putIfAbsent(medida.normalizada(), new TerminoResolucion(
          medida.normalizada(), List.of(medida.normalizada()), 1, false));
    }
    var resultado = new ArrayList<>(terminos.values());
    // Require an informative word, so a brand/measurement alone cannot replace a missing product.
    for (int i = 0; i < resultado.size(); i++) {
      var termino = resultado.get(i);
      if (termino.texto().matches("[A-Z]{3,}") && !GENERICOS.contains(termino.texto())) {
        resultado.set(i, new TerminoResolucion(termino.texto(), termino.variantes(), 8, true));
        return new Consulta(List.copyOf(resultado));
      }
    }
    return new Consulta(List.of());
  }

  static Map<String, Integer> palabras(String descripcion) {
    Map<String, Integer> palabras = new LinkedHashMap<>();
    var matcher = TOKENS.matcher(normalizar(descripcion));
    while (matcher.find()) {
      if (!OMITIDOS.contains(matcher.group())) palabras.merge(canonico(matcher.group()), 1, Integer::sum);
    }
    for (var medida : medidas(descripcion)) palabras.put(medida.normalizada(), 1);
    return palabras;
  }

  // Normalize units in numeric text chains without assigning diameter/width/thickness.
  private static Set<MedidaTexto> medidas(String descripcion) {
    record Parte(String numero, String unidad, int inicio, int fin) {}
    String texto = normalizar(descripcion);
    var partes = new ArrayList<Parte>();
    var matcher = MEDIDA.matcher(texto);
    while (matcher.find()) partes.add(new Parte(matcher.group("numero"), matcher.group("unidad"), matcher.start(), matcher.end()));
    Set<MedidaTexto> resultado = new LinkedHashSet<>();
    for (int i = 0; i < partes.size(); i++) {
      String unidad = partes.get(i).unidad();
      for (int j = i + 1; unidad == null && j < partes.size(); j++) {
        if (!texto.substring(partes.get(j - 1).fin(), partes.get(j).inicio()).matches("[\\sX-]*")) break;
        unidad = partes.get(j).unidad();
      }
      for (int j = i - 1; unidad == null && j >= 0; j--) {
        if (!texto.substring(partes.get(j).fin(), partes.get(j + 1).inicio()).matches("[\\sX-]*")) break;
        unidad = partes.get(j).unidad();
      }
      if (unidad == null) continue;
      try {
        BigDecimal valor = new BigDecimal(canonico(partes.get(i).numero()));
        BigDecimal factor = unidad.equals("MM") ? BigDecimal.ONE : unidad.equals("CM") ? BigDecimal.TEN
            : unidad.equals("\"") || unidad.startsWith("PULG") || unidad.equals("IN") ? new BigDecimal("25.4")
            : new BigDecimal("1000");
        if (valor.signum() > 0) resultado.add(new MedidaTexto(canonico(partes.get(i).numero()),
            valor.multiply(factor).stripTrailingZeros().toPlainString() + "MM"));
      } catch (NumberFormatException exception) {
        // An invalid fraction remains ordinary text, never an inferred measurement.
      }
    }
    return resultado;
  }
}
