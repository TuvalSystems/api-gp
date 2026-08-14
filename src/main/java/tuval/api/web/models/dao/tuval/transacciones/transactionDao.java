package tuval.api.web.models.dao.tuval.transacciones;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.Utils.InvoiceHelperUtils;
import tuval.api.web.models.dto.transacciones.facturacionVenta.responseInvoiceVtaDto;
import tuval.api.web.models.dto.transacciones.reciboCompraL.StatusReceptionOrderItemLDto;
import tuval.api.web.models.dto.transacciones.reciboCompraL.validateDto;
import tuval.api.web.models.request.transacciones.facturacionVenta.DetailInvoiceRequest;
import tuval.api.web.models.request.transacciones.facturacionVenta.HeaderInvoiceRequest;
import tuval.api.web.models.request.transacciones.facturacionVenta.ResponseConsultInvoiceRequest;
import tuval.api.web.models.request.transacciones.reciboCompraL.DetailReceivedLRequest;
import tuval.api.web.models.request.transacciones.reciboCompraL.HeaderReceivedLRequest;
import tuval.api.web.models.request.transacciones.reciboCompraL.validateReceidLRequest;

@Repository
public class transactionDao {

  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spStatusReceptionItemL;

  public transactionDao(@Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "El JdbcTemplate no puede ser null");
    this.spStatusReceptionItemL = new SimpleJdbcCall(jdbcTemplate)
        .withProcedureName("MSW_SP_ESTADO_RECEPCION_COMPRA")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(
            new SqlParameter("@docu", Types.VARCHAR), new SqlParameter("@tipo", Types.VARCHAR));
  }

  public validateDto executeSpValidateGenerateReceivedItemL(validateReceidLRequest request) {
    try {
      SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
          .withProcedureName("MSW_SP_VALIDAR_GENERACION_RECEPCION")
          .declareParameters(
              // Parámetros de entrada
              new SqlParameter("@docu", Types.VARCHAR),
              new SqlParameter("@tipo", Types.VARCHAR))
          .returningResultSet(
              "resultados",
              (rs, rowNum) -> {
                validateDto response = new validateDto();
                response.setCodigo(
                    (rs.getString("I_vCODIGO") != null
                        && !rs.getString("I_vCODIGO").trim().isEmpty())
                            ? rs.getString("I_vCODIGO").trim()
                            : "0");
                response.setMensaje(
                    (rs.getString("I_vMENSAJE") != null
                        && !rs.getString("I_vMENSAJE").trim().isEmpty())
                            ? rs.getString("I_vMENSAJE").trim()
                            : "Documento generado correctamente");
                response.setTipoDocGenerado(
                    rs.getString("TIPO_DOC_GENERADO") != null
                        ? rs.getString("TIPO_DOC_GENERADO").trim()
                        : "");
                response.setNumDocGenerado(
                    rs.getString("DOC_GENERADO") != null
                        ? rs.getString("DOC_GENERADO").trim()
                        : "");
                return response;
              });
      Map<String, Object> params = new HashMap<>();
      params.put("docu", request.getDocumento() != null ? request.getDocumento().trim() : "");
      params.put("tipo", request.getTipo() != null ? request.getTipo().trim() : "");
      Map<String, Object> out = jdbcCall.execute(params);
      @SuppressWarnings("unchecked")
      List<validateDto> resultados = (List<validateDto>) out.get("resultados");

      if (resultados == null || resultados.isEmpty()) {
        // Lanza excepción si no hay registros
        throw new RuntimeException("No existe registro para los parámetros proporcionados");
      }
      return resultados.get(0);
    } catch (RuntimeException e) {
      throw new RuntimeException(
          "Error al ejecutar el sp: MSW_SP_VALIDAR_GENERACION_RECEPCION: " + e.getMessage(), e);
    }
  }

  public validateDto executeSpDeleteReceivedHeaderAndDetail(validateReceidLRequest request) {
    try {
      SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
          .withProcedureName("MSW_SP_ELIMINACION_RECEPCION_CAB_DET")
          .declareParameters(
              // Parámetros de entrada
              new SqlParameter("@docu", Types.VARCHAR),
              new SqlParameter("@tipo", Types.INTEGER));

      Map<String, Object> params = new HashMap<>();
      params.put("docu", request.getDocumento() != null ? request.getDocumento().trim() : "");
      params.put("tipo", request.getTipo() != null ? request.getTipo().trim() : "");
      jdbcCall.execute(params);

      validateDto response = new validateDto();
      response.setCodigo("0"); // por convención, código 0 = OK
      response.setMensaje("Recibo eliminado correctamente");
      response.setTipoDocGenerado(""); // opcional
      response.setNumDocGenerado(""); // opcional
      return response;
    } catch (RuntimeException e) {
      throw new RuntimeException(
          "Error al ejecutar el SP: MSW_SP_ELIMINACION_RECEPCION_CAB_DET. " + e.getMessage(), e);
    }
  }

  public void executeSpStoreHeaderReceivedL(HeaderReceivedLRequest request) {

    SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
        .withProcedureName("MSW_SP_GUARDAR_RECEPCION_CAB")
        .declareParameters(
            // Parámetros de entrada
            new SqlParameter("@docu", Types.VARCHAR),
            new SqlParameter("@id_tipo", Types.INTEGER),
            new SqlParameter("@proveedor", Types.VARCHAR),
            new SqlParameter("@fecha", Types.DATE));

    Map<String, Object> params = new HashMap<>();
    params.put(
        "docu", request.getRecComDocuOrigen() != null ? request.getRecComDocuOrigen().trim() : "");
    params.put(
        "id_tipo", request.getRecComIdTipo() != null ? request.getRecComIdTipo().trim() : "");
    params.put(
        "proveedor",
        request.getRecComIdProveedor() != null ? request.getRecComIdProveedor().trim() : "");
    params.put("fecha", request.getRecComFecha() != null ? request.getRecComFecha() : "");
    jdbcCall.execute(params);
  }

  public void executeSpStoreDetailReceivedL(DetailReceivedLRequest request) {

    SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
        .withProcedureName("MSW_SP_GUARDAR_RECEPCION_DET")
        .declareParameters(
            // Parámetros de entrada
            new SqlParameter("@docu", Types.VARCHAR),
            new SqlParameter("@tipo", Types.INTEGER),
            new SqlParameter("@articulo", Types.VARCHAR),
            new SqlParameter("@descripcion_articulo", Types.VARCHAR),
            new SqlParameter("@unidad", Types.VARCHAR),
            new SqlParameter("@bodega", Types.VARCHAR),
            new SqlParameter("@cantidad", Types.DECIMAL));

    Map<String, Object> params = new HashMap<>();
    params.put(
        "docu", request.getRecComDocuOrigen() != null ? request.getRecComDocuOrigen().trim() : "");
    params.put("tipo", request.getRecComIdTipo() != null ? request.getRecComIdTipo() : "");
    params.put(
        "articulo",
        request.getRecCOmIdArticulo() != null ? request.getRecCOmIdArticulo().trim() : "");
    params.put(
        "descripcion_articulo",
        request.getRecComDescripcionArticulo() != null
            ? request.getRecComDescripcionArticulo()
            : "");
    params.put(
        "unidad",
        request.getRecComUnidadMedida() != null ? request.getRecComUnidadMedida().trim() : "");
    params.put("bodega", request.getRecComBodega() != null ? request.getRecComBodega().trim() : "");
    params.put("cantidad", request.getRecComCantidad() != null ? request.getRecComCantidad() : "");
    jdbcCall.execute(params);
  }

  public void executeSpGenerateProcessReceivedL(validateReceidLRequest request) {

    SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
        .withProcedureName("MSW_SP_CREACION_RECEPCION_OC")
        .declareParameters(
            // Parámetros de entrada
            new SqlParameter("@DOCUMENTO", Types.VARCHAR));

    Map<String, Object> params = new HashMap<>();
    params.put("DOCUMENTO", request.getDocumento() != null ? request.getDocumento().trim() : "");
    jdbcCall.execute(params);
  }

  public validateDto executeSpResponseReceivedL(validateReceidLRequest request) {
    try {
      SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
          .withProcedureName("MSW_SP_VALIDAR_GENERACION_RECEPCION")
          .declareParameters(
              // Parámetros de entrada
              new SqlParameter("@docu", Types.VARCHAR),
              new SqlParameter("@tipo", Types.INTEGER))
          .returningResultSet(
              "resultados",
              (rs, rowNum) -> {
                validateDto response = new validateDto();
                response.setCodigo(
                    (rs.getString("I_vCODIGO") != null
                        && !rs.getString("I_vCODIGO").trim().isEmpty())
                            ? rs.getString("I_vCODIGO").trim()
                            : "0");
                response.setMensaje(
                    (rs.getString("I_vMENSAJE") != null
                        && !rs.getString("I_vMENSAJE").trim().isEmpty())
                            ? rs.getString("I_vMENSAJE").trim()
                            : "Documento generado correctamente");
                response.setTipoDocGenerado(
                    rs.getString("TIPO_DOC_GENERADO") != null
                        ? rs.getString("TIPO_DOC_GENERADO").trim()
                        : "");
                response.setNumDocGenerado(
                    rs.getString("DOC_GENERADO") != null
                        ? rs.getString("DOC_GENERADO").trim()
                        : "");
                return response;
              });
      Map<String, Object> params = new HashMap<>();
      params.put("docu", request.getDocumento() != null ? request.getDocumento().trim() : "");
      params.put("tipo", request.getTipo() != null ? request.getTipo().trim() : "");
      Map<String, Object> out = jdbcCall.execute(params);
      @SuppressWarnings("unchecked")
      List<validateDto> resultados = (List<validateDto>) out.get("resultados");

      if (resultados == null || resultados.isEmpty()) {
        // Lanza excepción si no hay registros
        throw new RuntimeException("No existe registro para los parámetros proporcionados");
      }
      return resultados.get(0);
    } catch (RuntimeException e) {
      throw new RuntimeException(
          "Error al ejecutar el sp: MSW_SP_VALIDAR_GENERACION_RECEPCION: " + e.getMessage(), e);
    }
  }

  public StatusReceptionOrderItemLDto executeSpRepsonseStatusReceptionOrderItemLDao(
      validateReceidLRequest request) {
    StatusReceptionOrderItemLDto dto = new StatusReceptionOrderItemLDto();
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("@docu", request.getDocumento())
        .addValue("@tipo", request.getTipo());

    try {
      Map<String, Object> out = spStatusReceptionItemL.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");
      if (rs == null || rs.isEmpty()) {
        throw new RuntimeException("SP no retornó filas para cliente=" + request.getDocumento());
      }
      Map<String, Object> row = rs.get(0);
      dto.setDocuOrigen(CoerceHelperUtils.stringOrEmpty(row.get("I_vDOCU_ORIGEN")));
      dto.setIdTipo(CoerceHelperUtils.stringOrEmpty(row.get("I_vID_TIPO")));
      dto.setEstado(CoerceHelperUtils.stringOrEmpty(row.get("I_vESTADO")));
      return dto;
    } catch (Exception e) {
      throw new RuntimeException(
          "Error en dao executeSpRepsonseStatusReceptionOrderItemLDao : " + e.getMessage(), e);
    }
  }

  /** proceso de facturacion */
  public void executeSpStoreHeaderInvoice(HeaderInvoiceRequest req) {
    try {
      InvoiceHelperUtils.validateHeaderInvoiceRequired(req);
      var call = new SimpleJdbcCall(jdbcTemplate).withProcedureName("MSW_SP_GUARDAR_FACTURA_VTA_CAB");
      MapSqlParameterSource p = new MapSqlParameterSource()
          .addValue("pedido_id", req.getPedidoId())
          .addValue("docu", InvoiceHelperUtils.str(req.getFactVtaDocuOrigen(), 30))
          .addValue("id_tipo", InvoiceHelperUtils.str(req.getFactVtaIdTipo(), 15))
          .addValue("punto_venta", InvoiceHelperUtils.str(req.getFacVtaPuntoVenta(), 15))
          .addValue("doc_id", InvoiceHelperUtils.str(req.getFacVtaDocId(), 20))
          .addValue("cliente", InvoiceHelperUtils.str(req.getFactVtaIdCliente(), 15))
          .addValue("vendedor", InvoiceHelperUtils.str(req.getFactVtaIdVendedor(), 15))
          .addValue("direccion", InvoiceHelperUtils.str(req.getFacVtaIdDireccion(), 30))
          .addValue(
              "direccion_envio", InvoiceHelperUtils.str(req.getFacVtaIdDireccionEnvio(), 30))
          .addValue("cond_pago", InvoiceHelperUtils.str(req.getFacVtaCondPago(), 20))
          .addValue("forma_pago", InvoiceHelperUtils.str(req.getFacVtaFormaPago(), 30))
          .addValue("documento_compra", InvoiceHelperUtils.str(req.getFactVtaDocuCompra(), 30))
          .addValue("tipo_despacho", InvoiceHelperUtils.str(req.getFacVtaTipoDespacho(), 30))
          .addValue("nota_docu", InvoiceHelperUtils.str(req.getFacVtaNotaDocu(), 8000))
          .addValue("fecha", InvoiceHelperUtils.ts(req.getFacVtaDateDocu()), java.sql.Types.TIMESTAMP)
          .addValue("bodega", InvoiceHelperUtils.str(req.getFacVtaBodega(), 10))
          .addValue("subtotal", InvoiceHelperUtils.dec(req.getFacVtaSubtotal()), java.sql.Types.DECIMAL)
          .addValue("desc_comercial", InvoiceHelperUtils.dec(req.getFacVtaDescuentoComercial()), java.sql.Types.DECIMAL)
          .addValue("impuesto", InvoiceHelperUtils.dec(req.getFacVtaImpuesto()), java.sql.Types.DECIMAL)
          .addValue("total", InvoiceHelperUtils.dec(req.getFacVtaTotal()), java.sql.Types.DECIMAL)
          .addValue("nota_cliente", InvoiceHelperUtils.str(req.getFacVtaNotaCliente(), 300))
          .addValue("userid", InvoiceHelperUtils.str(req.getUserIdCreator(), 100))
          .addValue("cond_pago_org", InvoiceHelperUtils.str(req.getCondIdOriginal(), 100))
          .addValue("apro_comer_userid", InvoiceHelperUtils.str(req.getUserApprovalId(), 20))
          .addValue("proyecto", CoerceHelperUtils.getInt(req.getProyecto(), 30))
          ;

      call.execute(p);
    } catch (jakarta.validation.ValidationException ve) {
      // deja pasar tal cual o vuelve a lanzar para manejar 400 Bad Request arriba
      throw ve;
    } catch (Exception e) {
      throw new RuntimeException(
          "Error al ejecutar el SP de cabecera de factura: " + e.getMessage(), e);
    }
  }

  public void executeSpStoreDetailInvoice(DetailInvoiceRequest req) {
    try {

      InvoiceHelperUtils.validateDetailInvoiceRequired(req);

      var call = new SimpleJdbcCall(jdbcTemplate).withProcedureName("MSW_SP_GUARDAR_FACTURA_VTA_DET");

      MapSqlParameterSource p = new MapSqlParameterSource()
          .addValue("detalle_id", req.getDetalleId())
          .addValue("pedido_id", req.getPedidoId())
          .addValue("docu", InvoiceHelperUtils.str(req.getFacVtaDetDocuOrigen(), 30))
          .addValue("tipo", InvoiceHelperUtils.str(req.getFacVtaDetIdTipo(), 15)) // usa el mismo nombre
          .addValue("linea", InvoiceHelperUtils.str(req.getFacVtaDetLinea(), 5)) // o Types.INTEGER si el SP
          .addValue("articulo", InvoiceHelperUtils.str(req.getFacVtaDetArticulo(), 30))
          .addValue("descripcion_articulo", InvoiceHelperUtils.str(req.getFacVtaDetDescArticulo(), 255))
          .addValue("unidad", InvoiceHelperUtils.str(req.getFacVtaDetUnidadMedida(), 10)) // verifica tipo
          .addValue("bodega", InvoiceHelperUtils.str(req.getFacVtadetBodega(), 10))
          .addValue("cantidad", InvoiceHelperUtils.dec(req.getFacVtaDetCantidad()), java.sql.Types.DECIMAL)
          .addValue("costo", InvoiceHelperUtils.dec(req.getFacVtaCostoUnitario()), java.sql.Types.DECIMAL)
          .addValue("precio", InvoiceHelperUtils.dec(req.getFacVtaDetPrecioUnitario()), java.sql.Types.DECIMAL)
          .addValue("descuento", InvoiceHelperUtils.dec(req.getFacVtaDetDescuento()), java.sql.Types.DECIMAL)
          .addValue("impuesto", InvoiceHelperUtils.dec(req.getFacVtaDetImpuestoIva()), java.sql.Types.DECIMAL)
          .addValue("total", InvoiceHelperUtils.dec(req.getFacVtaDetTotal()), java.sql.Types.DECIMAL)
          .addValue("descripcion_org", InvoiceHelperUtils.str(req.getDescripcionOrginal(), 300))
          .addValue("valor_org", InvoiceHelperUtils.dec(req.getValorOrginal()), java.sql.Types.DECIMAL)
          .addValue("descuento_org", InvoiceHelperUtils.dec(req.getDescuentoOrginal()), java.sql.Types.DECIMAL)
          .addValue("descuento_final", InvoiceHelperUtils.dec(req.getDescuento()), java.sql.Types.DECIMAL);

      call.execute(p);
    } catch (jakarta.validation.ValidationException ve) {
      throw ve;
    } catch (Exception e) {
      throw new RuntimeException(
          "Error al ejecutar el SP de detalle de factura: " + e.getMessage(), e);
    }
  }

  public void executSpGenerateInvoiceVta(HeaderInvoiceRequest request) {
    try {
      SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
          .withProcedureName("MSW_SP_CREACION_FACTURA_VTA")
          .declareParameters(
              // Parámetros de entrada
              new SqlParameter("DOCU", Types.VARCHAR),
              new SqlParameter("TIPO_DOCU", Types.VARCHAR));
      MapSqlParameterSource p = new MapSqlParameterSource()
          .addValue("DOCU", InvoiceHelperUtils.str(request.getFactVtaDocuOrigen(), 30))
          .addValue("TIPO_DOCU", InvoiceHelperUtils.str(request.getFactVtaIdTipo(), 15));
      jdbcCall.execute(p);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error al ejecutar el SP de detalle de factura: " + e.getMessage(), e);
    }
  }

  public responseInvoiceVtaDto executeSpResponseInvoiceVta(ResponseConsultInvoiceRequest request) {
    try {
      SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
          .withProcedureName("MSW_SP_VALIDAR_GENERACION_FACTURA_VTA")
          .declareParameters(
              // Parámetros de entrada
              new SqlParameter("docu", Types.VARCHAR), new SqlParameter("tipo", Types.VARCHAR))
          .returningResultSet(
              "response",
              (rs, rowNum) -> {
                responseInvoiceVtaDto response = new responseInvoiceVtaDto();
                response.setDocumentoOrigen(
                    rs.getString("I_vDOCU_ORIGEN") != null
                        ? rs.getString("I_vDOCU_ORIGEN").trim()
                        : "");
                response.setIdTipoDocumento(
                    rs.getString("I_vID_TIPO") != null
                        ? rs.getString("I_vID_TIPO").trim()
                        : "");
                response.setCodigo(
                    rs.getString("I_vCODIGO") != null ? rs.getString("I_vCODIGO").trim() : "");
                response.setMensaje(
                    rs.getString("I_vMENSAJE") != null
                        ? rs.getString("I_vMENSAJE").trim()
                        : "");
                response.setTipoDocGenerado(
                    rs.getString("TIPO_DOC_GENERADO") != null
                        ? rs.getString("TIPO_DOC_GENERADO").trim()
                        : "");
                response.setNumeroDocGenerado(
                    rs.getString("DOC_GENERADO") != null
                        ? rs.getString("DOC_GENERADO").trim()
                        : "");
                return response;
              });
      MapSqlParameterSource p = new MapSqlParameterSource()
          .addValue("docu", InvoiceHelperUtils.str(request.getDocumento(), 300))
          .addValue("tipo", InvoiceHelperUtils.str(request.getTipoDocumento(), 15));
      Map<String, Object> out = jdbcCall.execute(p);

      @SuppressWarnings("unchecked")
      List<responseInvoiceVtaDto> resultados = (List<responseInvoiceVtaDto>) out.get("response");
      if (resultados == null || resultados.isEmpty()) {
        throw new RuntimeException("No existe registro para los parámetros proporcionados");
      }
      return resultados.get(0);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error al ejecutar el SP de detalle de factura: " + e.getMessage(), e);
    }
  }

  public void executeSpDeleteInvoiceVta(ResponseConsultInvoiceRequest request) {
    try {
      SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
          .withProcedureName("MSW_SP_ELIMINACION_FACTURA_VTA_CAB_DET")
          .declareParameters(
              // Parámetros de entrada
              new SqlParameter("docu", Types.VARCHAR), new SqlParameter("tipo", Types.VARCHAR));
      MapSqlParameterSource p = new MapSqlParameterSource()
          .addValue("docu", InvoiceHelperUtils.str(request.getDocumento(), 300))
          .addValue("tipo", InvoiceHelperUtils.str(request.getTipoDocumento(), 15));
      jdbcCall.execute(p);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error al ejecutar el SP de detalle de factura: " + e.getMessage(), e);
    }
  }
}
