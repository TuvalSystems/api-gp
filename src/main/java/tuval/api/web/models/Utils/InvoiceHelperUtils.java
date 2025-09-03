package tuval.api.web.models.Utils;

import jakarta.validation.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import tuval.api.web.models.dto.transacciones.facturacionVenta.DetailInvoiceRequest;
import tuval.api.web.models.dto.transacciones.facturacionVenta.HeaderInvoiceRequest;

public class InvoiceHelperUtils {

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
    // checkBlank(r.getFactVtaDocuCompra(), "FAC_VTA_DOC_COMPRA", faltantes);
    checkBlank(r.getFacVtaTipoDespacho(), "FAC_VTA_TIPO_DESPACHO", faltantes);
    checkBlank(r.getFacVtaBodega(), "FAC_VTA_BODEGA", faltantes);

    // DATETIME requerido
    checkNull(r.getFacVtaDateDocu(), "FAC_VTA_DATE_DOCU", faltantes);

    // DECIMAL(19,5) requeridos
    checkNull(r.getFacVtaSubtotal(), "FAC_VTA_SUBTOTAL", faltantes);
    // checkNull(r.getFacVtaDescuentoComercial(), "FAC_VTA_DESCUENTO_COMERCIAL", faltantes);
    checkNull(r.getFacVtaImpuesto(), "FAC_VTA_IMPUESTO", faltantes);
    checkNull(r.getFacVtaTotal(), "FAC_VTA_TOTAL", faltantes);

    // Opcionales: nota_docu / nota_cliente (comenta estas dos si quieres que sean
    // obligatorias)
    // checkBlank(r.getFacVtaNotaDocu(), "FAC_VTA_NOTA_DOCU", faltantes);
    // checkBlank(r.getFacVtaNotaCliente(), "FAC_VTA_NOTA_CLIENTE", faltantes);

    if (!faltantes.isEmpty()) {
      throw new ValidationException(
          "Campos requeridos vacíos o nulos: " + String.join(", ", faltantes));
    }
  }

  public static void validateDetailInvoiceRequired(DetailInvoiceRequest r) {
    List<String> faltantes = new ArrayList<>();

    // VARCHAR requeridos (no nulos ni en blanco)
    // checkBlank(r.getFacVtaDetLinea(), "FAC_VTA_DET_LINEA", faltantes);
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
}
