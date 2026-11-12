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
    if (familia != null && ampliada(familia)) return interpretarProducto(normalized, familia);
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
    long materials = List.of("GALV(?:ANIZAD[OA]S?)?", "INOX(?:IDABLE|ID)?", "BRONCE", "LATON", "PVC", "ACERO NEGRO")
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
    if (ampliada(spec.familia())) return faltantesProducto(spec);
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
    // A mention of an accessory does not describe the main product.
    if (text.matches(".*\\b(?:SERVICIO|BRIDA|FLOTADOR|ACOPLE|BASE|SOPORTE)\\b.*")) return null;
    if (text.matches(".*\\b(?:PLANCHAS?|PLANCHONES?|PLANCHON)\\b.*")) return Familia.PLANCHA;
    if (text.matches(".*\\bANGULOS?\\b.*")) return Familia.ANGULO;
    if (text.matches(".*\\bPERNOS?\\b.*")) return Familia.PERNO;
    if (text.matches(".*\\bTUBOS?\\b.*")) return Familia.TUBO;
    if (text.matches(".*\\bCHUMACERAS?\\b.*")) return Familia.CHUMACERA;
    if (text.matches(".*\\bBRONCE\\b.*")) return Familia.BRONCE;
    if (text.matches(".*\\b(?:EJES?|BARRAS?)\\b.*")) return Familia.EJE;
    return null;
  }

  static boolean ampliada(Familia familia) {
    return Set.of(Familia.PLANCHA, Familia.ANGULO, Familia.TUBO, Familia.PERNO,
        Familia.EJE, Familia.CHUMACERA, Familia.BRONCE).contains(familia);
  }

  static Map<String, String> filtros(Especificacion spec) {
    var filtros = new LinkedHashMap<String, String>();
    if (spec.familia() == null || !ampliada(spec.familia())) return filtros;
    for (String key : List.of("material", "cedula")) {
      if (spec.atributos().containsKey(key)) filtros.put(key, spec.atributos().get(key));
    }
    return filtros;
  }

  private record Dimension(String valor, String unidad) {}
  private static final Pattern DIMENSION = Pattern.compile(
      "(?<![A-Z0-9/.,])(?<valor>\\d{1,5}\\s+\\d{1,3}/\\d{1,3}|\\d{1,5}/\\d{1,3}"
      + "|\\d{1,5}(?:[.,]\\d{1,3})?)\\s*(?<unidad>MM|CM|METROS?|MTR|MTS|MT|M|PULGADAS?|PULG|IN|\")?"
      + "(?![A-Z0-9/.,])");

  private static Especificacion interpretarProducto(String text, Familia familia) {
    var attrs = new LinkedHashMap<String, String>();
    var pendientes = new ArrayList<String>();
    put(attrs, "material", material(text));
    if (text.matches(".*\\bINOX(?:IDABLE|ID)?\\b.*")) {
      // PVC/papel describes protection on stainless sheets, not their base material.
      attrs.put("material", "INOXIDABLE");
      put(attrs, "gradoMaterial", valor(text, "\\b(201L?|304L?|316L?|430)\\b"));
    }
    put(attrs, "cedula", valor(text, "(?<![A-Z/])(?:CEDULA\\s*|CED\\s*|SCH\\s*|C)(\\d{1,3})S?\\b"));
    put(attrs, "rosca", valor(text, "\\b(UNC|UNF|NPT|BSPT|BSPP|BSP)\\b"));
    if (text.contains("SIN COSTURA") || text.contains("S/C")) attrs.put("construccion", "SIN COSTURA");
    if (text.contains("CON COSTURA") || text.contains("C/C")) attrs.put("construccion", "CON COSTURA");
    if (text.matches(".*\\b(?:GOLPEAD[OA]|GOLPE|RAYAD[OA]|MANCHAD[OA]|DOBLAD[OA]|CURVEAD[OA]|DANAD[OA])\\b.*")) {
      attrs.put("condicion", "OBSERVADO");
    }
    put(attrs, "acabado", valor(text, "\\bACABADO\\s+(2B|BA|[1-48])\\b"));
    if (!attrs.containsKey("acabado") && familia == Familia.PLANCHA) {
      put(attrs, "acabado", valor(text, "#(2B|BA|[1-48])\\b"));
    }
    if (familia == Familia.PERNO && text.matches(".*\\bCOMPLETOS?\\b.*")) pendientes.add("componentes_de_perno_completo");
    if (familia == Familia.CHUMACERA) {
      put(attrs, "montaje", valor(text, "\\b(PARED|PEDESTAL)\\b"));
      put(attrs, "agujeros", valor(text, "\\b([24])H\\b"));
    }
    if (familia == Familia.BRONCE) {
      put(attrs, "forma", valor(text, "\\b(REDONDA|HEXAGONAL|PLATINA|BUJE|BOCIN)\\b"));
    }
    String rest = text.replaceAll("\\(\\s*(MTS?|METROS?)\\s*\\)", " M")
        .replaceAll("\\b(?:201L?|304L?|316L?|430)\\b", " ")
        .replaceAll("(?<![A-Z/])(?:CEDULA\\s*|CED\\s*|SCH\\s*|C)\\d{1,3}S?\\b", " ")
        .replaceAll("\\bACABADO\\s+(?:2B|BA|[1-48])\\b|#(?:2B|BA|[1-48])\\b", " ")
        .replaceAll("\\b(?:PAQUETE|PAQ\\.?)\\s*X?\\s*\\d+", " ")
        .replaceAll("\\([^)]*\\)", " ")
        .replaceAll("\\bA\\d+|\\bAPI\\s*\\d+[A-Z]*|\\b[24]H\\b", " ")
        .replaceAll("(?<![\\d\\s])-(?=\\s*\\d)", " ");
    // A length range must not be read as two independent exact dimensions.
    if (rest.matches(".*\\d[.,]?\\d*\\s*-\\s*\\d.*")) {
      pendientes.add("largo_variable");
      rest = rest.replaceAll("\\d+(?:[.,]\\d+)?\\s*-\\s*\\d+(?:[.,]\\d+)?\\s*M\\b", " ");
    }
    var matcher = DIMENSION.matcher(rest);
    var dimensiones = new ArrayList<Dimension>();
    while (matcher.find()) dimensiones.add(new Dimension(fraccion(matcher.group("valor")), matcher.group("unidad")));
    var valores = new ArrayList<String>();
    var unidades = new LinkedHashSet<String>();
    for (int i = 0; i < dimensiones.size(); i++) {
      var dim = dimensiones.get(i);
      String unidad = dim.unidad();
      // In a dimension chain such as 6 x 1500 x 6000mm the suffix applies to the chain.
      if (unidad == null) {
        for (int j = i + 1; j < dimensiones.size(); j++) {
          if (dimensiones.get(j).unidad() != null) { unidad = dimensiones.get(j).unidad(); break; }
        }
      }
      valores.add(dim.valor());
      String key = switch (familia) {
        case PLANCHA -> i == 0 ? "espesorMm" : i == 1 ? "anchoMm" : i == 2 ? "largoMm" : null;
        case ANGULO -> i == 0 ? "alaMm" : i == 1 ? "espesorMm" : i == 2 ? "largoMm" : null;
        case TUBO -> i == 0 ? "diametroNominalMm" : i == 1 ? "largoMm" : null;
        case PERNO, EJE -> i == 0 ? "diametroMm" : i == 1 ? "largoMm" : null;
        case CHUMACERA -> i == 0 ? "diametroMm" : null;
        case BRONCE -> i < 3 ? "dimension" + (i + 1) + "Mm" : null;
        default -> null;
      };
      if (key != null) {
        if (unidad == null || dim.valor().equals("INVALIDA")) pendientes.add(key);
        else {
          unidades.add(unidad.equals("\"") || unidad.startsWith("PULG") || unidad.equals("IN") ? "PULGADAS"
              : unidad.equals("MM") ? "MM" : unidad.equals("CM") ? "CM" : "M");
          attrs.put(key, milimetros(dim.valor(), unidad));
        }
      }
    }
    if (!valores.isEmpty()) attrs.put("medidas", String.join(" x ", valores));
    if (!unidades.isEmpty()) attrs.put("unidadMedidas", unidades.size() == 1 ? unidades.iterator().next() : "MIXTAS");
    // Retain unsupported request words as clarification; catalog brands do not fill attributes.
    String words = DIMENSION.matcher(rest).replaceAll(" ");
    var tokens = Pattern.compile("[A-Z]+(?:[._-][A-Z]+)*").matcher(words);
    Set<String> conocidos = Set.of("PLANCHA", "PLANCHAS", "PLANCHON", "PLANCHONES", "ANGULO", "ANGULOS",
        "TUBO", "TUBOS", "PERNO", "PERNOS", "EJE", "EJES", "BARRA", "BARRAS", "REDONDA", "CHUMACERA",
        "CHUMACERAS", "PARED", "PEDESTAL", "COMPLETO", "COMPLETOS", "METRO", "METROS", "MT", "MTS", "MTR",
        "CM", "M", "INOXID", "UNC", "UNF", "HILO", "ESPESOR", "ANCHO", "ALA", "HEXAGONAL", "BUJE", "BOCIN");
    while (tokens.find()) {
      String word = tokens.group().replaceAll("[.]$", "");
      if (!VOCABULARIO.contains(word) && !conocidos.contains(word)) pendientes.add("termino:" + word);
    }
    return new Especificacion(familia, attrs, List.copyOf(new LinkedHashSet<>(pendientes)));
  }

  private static String milimetros(String fraccion, String unidad) {
    String[] partes = fraccion.split("/");
    BigDecimal valor = new BigDecimal(partes[0]);
    BigDecimal factor = unidad.equals("\"") || unidad.startsWith("PULG") || unidad.equals("IN")
        ? new BigDecimal("25.4") : unidad.equals("MM") ? BigDecimal.ONE
        : unidad.equals("CM") ? BigDecimal.TEN : new BigDecimal("1000");
    if (partes.length > 1) valor = valor.multiply(factor).divide(new BigDecimal(partes[1]), 9, java.math.RoundingMode.HALF_UP);
    else valor = valor.multiply(factor);
    return valor.stripTrailingZeros().toPlainString();
  }

  private static List<String> faltantesProducto(Especificacion spec) {
    var missing = new LinkedHashSet<>(spec.pendientes());
    List<String> requeridos = switch (spec.familia()) {
      case PLANCHA -> List.of("material", "espesorMm", "anchoMm", "largoMm", "gradoMaterial", "acabado");
      case ANGULO -> List.of("material", "alaMm", "espesorMm", "largoMm", "gradoMaterial");
      case TUBO -> List.of("material", "diametroNominalMm", "cedula", "largoMm", "construccion");
      case PERNO -> List.of("material", "diametroMm", "largoMm", "rosca", "tipo_cabeza");
      case EJE -> List.of("material", "diametroMm", "largoMm", "gradoMaterial");
      case CHUMACERA -> List.of("diametroMm", "montaje", "agujeros");
      case BRONCE -> List.of("forma", "tipo_bronce");
      default -> List.of();
    };
    for (String key : requeridos) if (!spec.atributos().containsKey(key)) missing.add(key);
    if (spec.familia() == Familia.EJE && spec.atributos().containsKey("largoMm")) missing.add("corte_y_unidad_venta");
    return List.copyOf(missing);
  }

  private static String material(String text) {
    if (text.matches(".*\\bGALV(?:ANIZAD[OA]S?)?\\b.*")) return "GALVANIZADO";
    if (text.matches(".*\\bINOX(?:IDABLE|ID)?\\b.*")) return "INOXIDABLE";
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
