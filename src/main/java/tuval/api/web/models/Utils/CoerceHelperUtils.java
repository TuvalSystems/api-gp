package tuval.api.web.models.Utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CoerceHelperUtils {

  public static int getInt(Object o, int dflt) {
    if (o == null) return dflt;
    if (o instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(o.toString().trim());
    } catch (Exception e) {
      return dflt;
    }
  }

  public static int toInt(Object o, int dflt) {
    if (o == null) return dflt;
    if (o instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(o.toString().trim());
    } catch (Exception ignored) {
      return dflt;
    }
  }

  public static String stringOrEmpty(Object v) {
    return v == null ? "" : v.toString().trim();
  }


  public static double toDouble(Object v) {
  if (v == null) return 0d;
  if (v instanceof java.math.BigDecimal bd) return bd.doubleValue();
  if (v instanceof Number n) return n.doubleValue();
  return new java.math.BigDecimal(v.toString()).doubleValue();
}

public static int toIntObject(Object v) {
  if (v == null) return 0;
  if (v instanceof Boolean b) return b ? 1 : 0;
  if (v instanceof Number n) return n.intValue();
  return Integer.parseInt(v.toString().trim());
}


public static LocalDate toLocalDate(Object v) {
  if (v == null) return null;
  if (v instanceof LocalDate ld) return ld;
  if (v instanceof java.sql.Date d) return d.toLocalDate();
  if (v instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
  if (v instanceof java.time.LocalDateTime ldt) return ldt.toLocalDate();
  if (v instanceof java.time.OffsetDateTime odt) return odt.toLocalDate();
  if (v instanceof java.time.ZonedDateTime zdt) return zdt.toLocalDate();

  if (v instanceof CharSequence cs) {
    String s = cs.toString().trim();
    if (s.isEmpty()) return null;
    // intenta formatos comunes
    try { return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE); } catch (Exception ignore) {}
    try { return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate(); } catch (Exception ignore) {}
    try { return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy")); } catch (Exception ignore) {}
    try { return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyyMMdd")); } catch (Exception ignore) {}
  }
  throw new IllegalArgumentException("No puedo convertir a LocalDate: " + v + " (" + v.getClass().getName() + ")");
}

public static BigDecimal toBigDecimal(Object v) {
  if (v == null) return null;

  if (v instanceof BigDecimal bd) return bd;
  if (v instanceof java.math.BigInteger bi) return new BigDecimal(bi);

  if (v instanceof Byte || v instanceof Short || v instanceof Integer || v instanceof Long) {
    return BigDecimal.valueOf(((Number) v).longValue());
  }
  if (v instanceof Float || v instanceof Double) {
    // evita new BigDecimal(double) directo
    return BigDecimal.valueOf(((Number) v).doubleValue());
  }

  if (v instanceof CharSequence cs) {
    String s = cs.toString().trim();
    if (s.isEmpty()) return null;
    // normaliza posibles formatos "1.234,56" o "1,234.56"
    if (s.contains(",") && !s.contains(".")) {
      // caso latino: "1234,56" -> "1234.56"
      s = s.replace(".", "");     // por si hay miles como "1.234,56"
      s = s.replace(",", ".");
    } else {
      // caso en-us: "1,234.56" -> "1234.56"
      s = s.replace(",", "");
    }
    return new BigDecimal(s);
  }

  // último recurso: intenta por String
  return new BigDecimal(v.toString().trim());
}

}
