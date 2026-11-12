package tuval.api.web.reportes.inventario.articulos.service.dynamics.imp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import tuval.api.web.reportes.inventario.articulos.dao.dynamics.ArticulosDao.Familia;

/** Small, closed vocabulary. Unknown text is retained as a reason to ask, never a score. */
final class ReglasArticulo {
  private static final String NUMERO = "(?<![A-Z0-9/])(?:\\d{1,4}\\s+\\d{1,3}/\\d{1,3}"
      + "|\\d{1,4}/\\d{1,3}|\\d{1,4}(?:[.,]\\d{1,3})?)(?![A-Z0-9/])";
  private static final Pattern NUMEROS = Pattern.compile(NUMERO);
  private static final Set<String> VOCABULARIO = Set.of(
      "BUSHING", "BUSHINGS", "NIPLE", "NIPLES", "NEPLO", "NEPLOS", "CODO", "CODOS",
      "NUDO", "NUDOS", "UNION", "UNIVERSAL", "VALVULA", "VALVULAS", "LLAVE", "LLAVES",
      "ESFERICA", "ESFERICAS", "BOLA", "PASO", "TOTAL", "TEFLON", "PTFE", "CINTA",
      "GALVANIZADO", "GALVANIZADOS", "GALV", "GALVANIZADA", "INOX", "INOXIDABLE",
      "ACERO", "BRONCE", "LATON", "PVC", "NEGRO", "NEGRA", "REFORZADO", "REFORZADA",
      "ESTANDAR", "COSTURA", "NPT", "BSP", "BSPT", "BSPP", "ROSCA",
      "CLASE", "CEDULA", "SCH", "PULGADA", "PULGADAS", "PULG", "IN", "MM",
      "GRADO", "GRADOS", "DE", "POR", "X", "Y", "EL", "LA", "PARA", "DIAMETRO", "LARGO");

  record Especificacion(Familia familia, Map<String, String> atributos, List<String> pendientes) {}

  static String normalizar(String text) {
    String result = text == null ? "" : text;
    String[] fracciones = {"¼", "1/4", "½", "1/2", "¾", "3/4", "⅛", "1/8", "⅜", "3/8",
        "⅝", "5/8", "⅞", "7/8", "⅓", "1/3", "⅔", "2/3"};
    for (int i = 0; i < fracciones.length; i += 2) {
      result = result.replaceAll("(?<=\\d)" + fracciones[i], " " + fracciones[i + 1]);
      result = result.replace(fracciones[i], fracciones[i + 1]);
    }
    return Normalizer.normalize(result, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
        .toUpperCase(Locale.ROOT).replace('⁄', '/').replace('″', '"').replace('”', '"')
        .replace('“', '"').replace('×', 'X')
        .replaceAll("(?<=[\\d\"°])\\s*X\\s*(?=\\d)", " X ")
        .replaceAll("(?<=\\d)\\s*/\\s*(?=\\d)", "/")
        .replaceAll("(?<=\\d)-(?=\\d+/\\d)", " ")
        .replaceAll("\\bPOR(?=\\d)", "POR ")
        .replaceAll("\\s+", " ").trim();
  }

  static Especificacion interpretar(String texto) {
    String normalized = normalizar(texto);
    Familia familia = familia(normalized);
    var attrs = new LinkedHashMap<String, String>();
    List<String> pending = new ArrayList<>();
    if (normalized.matches(".*(?:\\d[.,]\\d+[.,]\\d|[-+]\\s*\\d|\\d\\s*-\\s*\\d).*")) {
      pending.add("formato_medidas_no_soportado");
    }
    long families = List.of("BUSHINGS?", "NIPLES?|NEPLOS?", "CODOS?", "NUDOS?|UNION UNIVERSAL",
        "TEFLON|CINTA PTFE", "VALVULAS?|LLAVES?").stream()
        .filter(f -> normalized.matches(".*\\b(?:" + f + ")\\b.*")).count();
    if (families > 1) pending.add("tipos_articulo_multiples");
    for (String regex : List.of("\\b(NPT|BSPT|BSPP|BSP)\\b", "\\b(45|90)\\b",
        "\\b(REFORZAD[OA]|ESTANDAR)\\b", "\\b(SIN COSTURA|CON COSTURA)\\b",
        "\\b(?:SCH|CEDULA)\\s*(\\d+)\\b", "\\b(304|316)\\b")) {
      var values = new HashSet<String>();
      var matches = Pattern.compile(regex).matcher(normalized);
      while (matches.find()) values.add(matches.group(1));
      if (values.size() > 1) pending.add("atributos_multiples");
    }
    long materials = List.of("GALV(?:ANIZAD[OA]S?)?", "INOX(?:IDABLE)?", "BRONCE", "LATON", "PVC", "ACERO NEGRO")
        .stream().filter(m -> normalized.matches(".*\\b" + m + "\\b.*")).count();
    if (materials > 1) pending.add("materiales_multiples");
    String rest = normalized;
    put(attrs, "material", material(normalized));
    put(attrs, "rosca", valor(normalized, "\\b(NPT|BSPT|BSPP|BSP)\\b"));
    put(attrs, "refuerzo", valor(normalized, "\\b(REFORZAD[OA]|ESTANDAR)\\b")
        .replace("REFORZADA", "REFORZADO"));
    put(attrs, "construccion", valor(normalized, "\\b(SIN COSTURA|CON COSTURA)\\b"));
    put(attrs, "cedula", valor(normalized, "\\b(?:SCH|CEDULA)\\s*(\\d+)\\b"));
    if (familia != Familia.TEFLON) {
      var classes = Pattern.compile("\\b(150|300)\\b").matcher(normalized);
      if (classes.find()) {
        attrs.put("clase", classes.group(1));
        if (classes.find()) pending.add("clases_multiples");
      }
      rest = rest.replaceAll("\\b(?:CLASE\\s*)?(?:150|300)\\b", " ");
    }
    if (familia == Familia.CODO) {
      put(attrs, "angulo", valor(normalized, "\\b(45|90)\\b"));
      rest = rest.replaceAll("\\b(?:45|90)\\b", " ");
    }
    if (normalized.contains("PASO TOTAL")) attrs.put("paso", "TOTAL");
    boolean pulgadas = normalized.contains("\"") || Pattern.compile("\\b(PULGADAS?|PULG|IN)\\b")
        .matcher(normalized).find();
    if (Pattern.compile("\\bMM\\b").matcher(normalized).find()) {
      attrs.put("unidadMedidas", pulgadas ? "MIXTAS" : "MM");
    } else if (pulgadas) attrs.put("unidadMedidas", "PULGADAS");
    rest = rest.replaceAll("\\b(?:SCH|CEDULA)\\s*\\d+\\b", " ");
    if ("INOXIDABLE".equals(attrs.get("material"))) {
      put(attrs, "gradoMaterial", valor(rest, "\\b(304|316)\\b"));
      rest = rest.replaceAll("\\b(?:304|316)\\b", " ");
    }
    var numbers = NUMEROS.matcher(rest);
    List<String> medidas = new ArrayList<>();
    while (numbers.find()) medidas.add(fraccion(numbers.group()));
    if (!medidas.isEmpty()) attrs.put("medidas", String.join(" x ", medidas));
    int expected = familia == Familia.BUSHING || familia == Familia.NIPLE ? 2 : 1;
    if (medidas.size() != expected || medidas.contains("INVALIDA")) pending.add("medidas");
    if (familia == Familia.TEFLON) pending.add("ancho_largo_espesor_teflon");
    if (familia == null) pending.add("tipo_articulo_soportado");
    if ("MIXTAS".equals(attrs.get("unidadMedidas"))) pending.add("unidades_mixtas");
    rest = NUMEROS.matcher(rest).replaceAll(" ");
    rest = rest.replaceAll("\\b(?:SIN COSTURA|CON COSTURA)\\b", " ");
    if (rest.matches(".*[^A-Z\\s._/\\-\"°#(),:;=+].*")) pending.add("simbolos_no_interpretados");
    var words = Pattern.compile("[A-Z]+(?:[._-][A-Z]+)*").matcher(rest);
    while (words.find()) {
      String word = words.group();
      if (!VOCABULARIO.contains(word)) pending.add("termino:" + word);
    }
    return new Especificacion(familia, attrs, pending);
  }

  static List<String> faltantes(Especificacion spec) {
    var missing = new ArrayList<>(spec.pendientes());
    if (spec.familia() == null) return missing;
    for (String attr : List.of("material", "unidadMedidas")) {
      if (!spec.atributos().containsKey(attr)) missing.add(attr);
    }
    if (spec.familia() != Familia.TEFLON && !spec.atributos().containsKey("rosca")) missing.add("rosca");
    if (Set.of(Familia.BUSHING, Familia.CODO, Familia.NUDO, Familia.NIPLE).contains(spec.familia())
        && !spec.atributos().containsKey("clase") && !spec.atributos().containsKey("refuerzo")) {
      missing.add("clase_o_refuerzo");
    }
    if (spec.familia() == Familia.CODO && !spec.atributos().containsKey("angulo")) missing.add("angulo");
    if (spec.familia() == Familia.VALVULA_BOLA && !spec.atributos().containsKey("marca")) missing.add("marca");
    return missing;
  }

  private static Familia familia(String text) {
    if (text.matches(".*\\bBUSHINGS?\\b.*")) return Familia.BUSHING;
    if (text.matches(".*\\b(?:NIPLES?|NEPLOS?)\\b.*")) return Familia.NIPLE;
    if (text.matches(".*\\bCODOS?\\b.*")) return Familia.CODO;
    if (text.matches(".*\\b(?:NUDOS?|UNION UNIVERSAL)\\b.*")) return Familia.NUDO;
    if (text.matches(".*\\b(?:TEFLON|CINTA PTFE)\\b.*")) return Familia.TEFLON;
    if (text.matches(".*\\b(?:VALVULAS?|LLAVES?)\\b.*")
        && text.matches(".*\\b(?:BOLA|ESFERICAS?|PASO TOTAL)\\b.*")) return Familia.VALVULA_BOLA;
    return null;
  }

  private static String material(String text) {
    if (text.matches(".*\\bGALV(?:ANIZAD[OA]S?)?\\b.*")) return "GALVANIZADO";
    if (text.matches(".*\\bINOX(?:IDABLE)?\\b.*")) return "INOXIDABLE";
    if (text.matches(".*\\bBRONCE\\b.*")) return "BRONCE";
    if (text.matches(".*\\bLATON\\b.*")) return "LATON";
    if (text.matches(".*\\bPVC\\b.*")) return "PVC";
    if (text.matches(".*\\bACERO NEGRO\\b.*")) return "ACERO NEGRO";
    return "";
  }

  private static String valor(String text, String regex) {
    var matcher = Pattern.compile(regex).matcher(text);
    return matcher.find() ? matcher.group(1) : "";
  }

  private static void put(Map<String, String> attrs, String key, String value) {
    if (!value.isEmpty()) attrs.put(key, value);
  }

  private static String fraccion(String number) {
    try {
      number = number.trim().replaceAll("\\s*/\\s*", "/");
      String[] mixed = number.trim().split("\\s+", 2);
      BigInteger numerator, denominator;
      if (number.contains("/")) {
        String fraction = mixed.length == 2 && mixed[1].contains("/")
            ? mixed[1] : number.trim();
        String[] parts = fraction.replace(" ", "").split("/");
        numerator = new BigInteger(parts[0]);
        denominator = new BigInteger(parts[1]);
        if (mixed.length == 2 && mixed[1].contains("/")) {
          numerator = numerator.add(new BigInteger(mixed[0]).multiply(denominator));
        }
      } else {
        BigDecimal decimal = new BigDecimal(number.replace(',', '.'));
        numerator = decimal.unscaledValue();
        denominator = BigInteger.TEN.pow(decimal.scale());
      }
      if (numerator.signum() <= 0 || denominator.signum() <= 0) return "INVALIDA";
      BigInteger gcd = numerator.gcd(denominator);
      numerator = numerator.divide(gcd);
      denominator = denominator.divide(gcd);
      return denominator.equals(BigInteger.ONE) ? numerator.toString() : numerator + "/" + denominator;
    } catch (NumberFormatException | IndexOutOfBoundsException exception) {
      return "INVALIDA";
    }
  }
}
