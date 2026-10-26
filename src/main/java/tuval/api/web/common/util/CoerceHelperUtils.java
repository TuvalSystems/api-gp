package tuval.api.web.common.util;

import jakarta.validation.ValidationException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import tuval.api.web.ventas.facturacion.request.DetailInvoiceRequest;
import tuval.api.web.ventas.facturacion.request.HeaderInvoiceRequest;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CoerceHelperUtils {
  private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Guayaquil");

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
      try {
        return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
      } catch (Exception ignore) {
      }
      try {
        return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .toLocalDate();
      } catch (Exception ignore) {
      }
      try {
        return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      } catch (Exception ignore) {
      }
      try {
        return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyyMMdd"));
      } catch (Exception ignore) {
      }
    }
    throw new IllegalArgumentException(
        "No puedo convertir a LocalDate: " + v + " (" + v.getClass().getName() + ")");
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
        s = s.replace(".", ""); // por si hay miles como "1.234,56"
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

  public static BigDecimal toBigDecimalOrZero(Object v) {
    BigDecimal value = toBigDecimal(v);
    return value == null ? BigDecimal.ZERO : value;
  }

  public static Timestamp toTimestamp(Object v) {
    return toTimestamp(v, DEFAULT_ZONE);
  }

  public static Timestamp toTimestamp(Object v, ZoneId zoneId) {
    if (v == null) return null;
    ZoneId targetZone = zoneId == null ? DEFAULT_ZONE : zoneId;
    if (v instanceof Timestamp timestamp) return timestamp;
    if (v instanceof java.sql.Date date) return Timestamp.valueOf(date.toLocalDate().atStartOfDay());
    if (v instanceof java.util.Date date) return new Timestamp(date.getTime());
    if (v instanceof LocalDate localDate) return Timestamp.valueOf(localDate.atStartOfDay());
    if (v instanceof LocalDateTime localDateTime) return Timestamp.valueOf(localDateTime);
    if (v instanceof OffsetDateTime offsetDateTime) {
      return Timestamp.valueOf(offsetDateTime.atZoneSameInstant(targetZone).toLocalDateTime());
    }
    if (v instanceof java.time.ZonedDateTime zonedDateTime) {
      return Timestamp.valueOf(zonedDateTime.withZoneSameInstant(targetZone).toLocalDateTime());
    }

    String text = v.toString().trim();
    if (text.isEmpty()) return null;

    try {
      return Timestamp.valueOf(OffsetDateTime.parse(text).atZoneSameInstant(targetZone).toLocalDateTime());
    } catch (DateTimeParseException ignored) {
    }

    try {
      return Timestamp.valueOf(LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    } catch (DateTimeParseException ignored) {
    }

    try {
      return Timestamp.valueOf(LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay());
    } catch (DateTimeParseException ignored) {
    }

    try {
      return Timestamp.valueOf(LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    } catch (DateTimeParseException ignored) {
    }

    try {
      return Timestamp.valueOf(LocalDate.parse(text, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay());
    } catch (DateTimeParseException ignored) {
    }

    try {
      return Timestamp.valueOf(LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay());
    } catch (DateTimeParseException ignored) {
    }

    throw new IllegalArgumentException(
        "No puedo convertir a Timestamp: " + v + " (" + v.getClass().getName() + ")");
  }

  public static String emptyIfNull(String s) {
    return (s == null || s.trim().isEmpty()) ? "" : s.trim();
  }

  public static String defaultIfBlank(String s, String def) {
    return (s == null || s.trim().isEmpty()) ? emptyIfNull(def) : s.trim();
  }
  private static final int MONEY_SCALE = 4;
  private static final RoundingMode MONEY_ROUND = RoundingMode.HALF_UP;

  /** para validacion de Invoice */
  public static String str(Object v, int maxLen) {
    if (v == null) return "";
    String s = String.valueOf(v).trim();
    return s.length() > maxLen ? s.substring(0, maxLen) : s;
  }

  public static java.sql.Timestamp ts(java.time.LocalDateTime dt) {
    return dt == null ? null : java.sql.Timestamp.valueOf(dt);
  }

  public static void validateHeaderInvoiceRequired(HeaderInvoiceRequest r) {
    List<String> faltantes = new ArrayList<>();

    // VARCHAR requeridos (no nulos ni en blanco)
    checkBlank(r.getFactVtaDocuOrigen(), "FAC_VTA_DOCU_ORIGEN", faltantes);
    checkBlank(r.getFactVtaIdTipo(), "FAC_VTA_ID_TIPO", faltantes);
    checkBlank(r.getFacVtaPuntoVenta(), "FAC_VTA_PUNTO_VENTA", faltantes);
    checkBlank(r.getFacVtaDocId(), "FAC_VTA_DOC_ID", faltantes);
    checkBlank(r.getFactVtaIdCliente(), "FAC_VTA_ID_CLIENTE", faltantes);
    checkBlank(r.getFactVtaIdVendedor(), "FAC_VTA_ID_VENDEDOR", faltantes);
    checkBlank(r.getFacVtaIdDireccion(), "FAC_VTA_ID_DIRECCION", faltantes);
    checkBlank(r.getFacVtaIdDireccionEnvio(), "FAC_VTA_ID_DIRECCION_ENVIO", faltantes);
    checkBlank(r.getFacVtaCondPago(), "FAC_VTA_COND_PAGO", faltantes);
    checkBlank(r.getFacVtaFormaPago(), "FAC_VTA_FORMA_PAGO", faltantes);
    checkBlank(r.getFacVtaTipoDespacho(), "FAC_VTA_TIPO_DESPACHO", faltantes);
    checkBlank(r.getFacVtaBodega(), "FAC_VTA_BODEGA", faltantes);

    // DATETIME requerido
    checkNull(r.getFacVtaDateDocu(), "FAC_VTA_DATE_DOCU", faltantes);

    // DECIMAL(19,5) requeridos
    checkNull(r.getFacVtaSubtotal(), "FAC_VTA_SUBTOTAL", faltantes);
    checkNull(r.getFacVtaImpuesto(), "FAC_VTA_IMPUESTO", faltantes);
    checkNull(r.getFacVtaTotal(), "FAC_VTA_TOTAL", faltantes);


    if (!faltantes.isEmpty()) {
      throw new ValidationException(
          "Campos requeridos vacíos o nulos: " + String.join(", ", faltantes));
    }
  }

  public static void validateDetailInvoiceRequired(DetailInvoiceRequest r) {
    List<String> faltantes = new ArrayList<>();


    checkBlank(r.getFacVtaDetDocuOrigen(), "FAC_VTA_DET_DOCU_ORIGEN", faltantes);
    // INT requerido
    if (r.getFacVtaDetIdTipo() == null) {
      faltantes.add("FAC_VTA_DET_ID_TIPO");
    }
    checkBlank(r.getFacVtaDetArticulo(), "FAC_VTA_DET_ARTICULO", faltantes);
    checkBlank(r.getFacVtaDetDescArticulo(), "FAC_VTA_DET_DESC_ARTICULO", faltantes);
    checkBlank(r.getFacVtadetBodega(), "FAC_VTA_DET_BODEGA", faltantes);

    // DECIMAL(19,5) requeridos
    checkNull(r.getFacVtaDetCantidad(), "FAC_VTA_DET_CANTIDAD", faltantes);
    checkNull(r.getFacVtaDetUnidadMedida(), "FAC_VTA_DET_UNIDAD_MEDIDA", faltantes);
    checkNull(r.getFacVtaCostoUnitario(), "FAC_VTA_DET_COSTO_UNITARIO", faltantes);
    checkNull(r.getFacVtaDetPrecioUnitario(), "FAC_VTA_DET_PRECIO_UNITARIO", faltantes);
    checkNull(r.getFacVtaDetDescuento(), "FAC_VTA_DET_DESCUENTO", faltantes);
    checkNull(r.getFacVtaDetImpuestoIva(), "FAC_VTA_DET_IMPUESTO_IVA", faltantes);
    checkNull(r.getFacVtaDetTotal(), "FAC_VTA_DET_TOTAL", faltantes);

    if (!faltantes.isEmpty()) {
      throw new ValidationException(
          "Campos requeridos vacíos o nulos: " + String.join(", ", faltantes));
    }
  }

  private static void checkBlank(String v, String nombre, List<String> faltantes) {
    if (v == null || v.trim().isEmpty()) faltantes.add(nombre);
  }

  public static void checkNull(Object v, String nombre, List<String> faltantes) {
    if (v == null) {
      faltantes.add(nombre);
      return;
    }

    if (v instanceof Number n) {
      boolean invalid;
      if (n instanceof java.math.BigDecimal bd) {
        invalid = bd.signum() < 0; // solo negativos
      } else if (n instanceof java.math.BigInteger bi) {
        invalid = bi.signum() < 0; // solo negativos
      } else if (n instanceof Double || n instanceof Float) {
        double d = n.doubleValue();
        invalid = Double.isNaN(d) || Double.isInfinite(d) || d < 0d; // 0 permitido
      } else {
        invalid = n.longValue() < 0L; // 0 permitido
      }
      if (invalid) faltantes.add(nombre);
      return;
    }

    if (v instanceof CharSequence cs) {
      if (cs.toString().trim().isEmpty()) {
        faltantes.add(nombre);
      }
    }
    // otros tipos no se marcan
  }

  public static BigDecimal dec(BigDecimal value) {
    if (value == null) return BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUND);
    return value.setScale(MONEY_SCALE, MONEY_ROUND);
  }
  public static BigDecimal dec(BigDecimal value, int scale) {
    return (value == null ? BigDecimal.ZERO : value).setScale(scale, MONEY_ROUND);
  }

  public static short toSmallInt(Integer value, int defaultValue) {
    int number = value == null ? defaultValue : value;
    if (number < Short.MIN_VALUE || number > Short.MAX_VALUE) {
      throw new ValidationException("Valor fuera del rango SQL SMALLINT: " + number);
    }
    return (short) number;
  }
}
