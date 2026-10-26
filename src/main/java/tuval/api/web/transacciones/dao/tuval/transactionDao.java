package tuval.api.web.transacciones.dao.tuval;

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

import tuval.api.web.bodega.recepciones.dto.StatusReceptionOrderItemLDto;
import tuval.api.web.bodega.recepciones.dto.validateDto;
import tuval.api.web.bodega.recepciones.request.DetailReceivedLRequest;
import tuval.api.web.bodega.recepciones.request.HeaderReceivedLRequest;
import tuval.api.web.bodega.recepciones.request.validateReceidLRequest;
import tuval.api.web.common.util.CoerceHelperUtils;
import tuval.api.web.ventas.facturacion.dto.responseInvoiceVtaDto;
import tuval.api.web.ventas.facturacion.request.DetailInvoiceRequest;
import tuval.api.web.ventas.facturacion.request.HeaderInvoiceRequest;
import tuval.api.web.ventas.facturacion.request.ResponseConsultInvoiceRequest;

@Repository
public class transactionDao {

  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spStatusReceptionItemL;

  public transactionDao(@Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "El JdbcTemplate no puede ser null");
    this.spStatusReceptionItemL =
        new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("MSW_SP_ESTADO_RECEPCION_COMPRA")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("@docu", Types.VARCHAR), new SqlParameter("@tipo", Types.VARCHAR));
  }

  public validateDto executeSpValidateGenerateReceivedItemL(validateReceidLRequest request) {
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
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
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
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

    SimpleJdbcCall jdbcCall =
        new SimpleJdbcCall(jdbcTemplate)
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

    SimpleJdbcCall jdbcCall =
        new SimpleJdbcCall(jdbcTemplate)
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

    SimpleJdbcCall jdbcCall =
        new SimpleJdbcCall(jdbcTemplate)
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
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
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
    MapSqlParameterSource in =
        new MapSqlParameterSource()
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
      CoerceHelperUtils.validateHeaderInvoiceRequired(req);
      var call =
          new SimpleJdbcCall(jdbcTemplate).withProcedureName("MSW_SP_GUARDAR_FACTURA_VTA_CAB");
      MapSqlParameterSource p =
          new MapSqlParameterSource()
              .addValue("pedido_id", req.getPedidoId(), Types.BIGINT)
              .addValue("docu", CoerceHelperUtils.str(req.getFactVtaDocuOrigen(), 30))
              .addValue("id_tipo", CoerceHelperUtils.str(req.getFactVtaIdTipo(), 15))
              .addValue("punto_venta", CoerceHelperUtils.str(req.getFacVtaPuntoVenta(), 15))
              .addValue("doc_id", CoerceHelperUtils.str(req.getFacVtaDocId(), 20))
              .addValue("cliente", CoerceHelperUtils.str(req.getFactVtaIdCliente(), 15))
              .addValue("vendedor", CoerceHelperUtils.str(req.getFactVtaIdVendedor(), 15))
              .addValue("direccion", CoerceHelperUtils.str(req.getFacVtaIdDireccion(), 30))
              .addValue("direccion_envio", CoerceHelperUtils.str(req.getFacVtaIdDireccionEnvio(), 30))
              .addValue("cond_pago", CoerceHelperUtils.str(req.getFacVtaCondPago(), 20))
              .addValue("forma_pago", CoerceHelperUtils.str(req.getFacVtaFormaPago(), 30))
              .addValue("documento_compra", CoerceHelperUtils.str(req.getFactVtaDocuCompra(), 30))
              .addValue("tipo_despacho", CoerceHelperUtils.str(req.getFacVtaTipoDespacho(), 30))
              .addValue("nota_docu", CoerceHelperUtils.str(req.getFacVtaNotaDocu(), 8000))
              .addValue("fecha", CoerceHelperUtils.ts(req.getFacVtaDateDocu()), java.sql.Types.TIMESTAMP)
              .addValue("bodega", CoerceHelperUtils.str(req.getFacVtaBodega(), 10))
              .addValue("subtotal", CoerceHelperUtils.dec(req.getFacVtaSubtotal(), 5), java.sql.Types.DECIMAL)
              .addValue("desc_comercial", CoerceHelperUtils.dec(req.getFacVtaDescuentoComercial(), 5),java.sql.Types.DECIMAL)
              .addValue("impuesto", CoerceHelperUtils.dec(req.getFacVtaImpuesto(), 5), java.sql.Types.DECIMAL)
              .addValue("total", CoerceHelperUtils.dec(req.getFacVtaTotal(), 5), java.sql.Types.DECIMAL)
              .addValue("nota_cliente", CoerceHelperUtils.str(req.getFacVtaNotaCliente(), 300))
              .addValue("userid", CoerceHelperUtils.str(req.getUserIdCreator(), 20))
              .addValue("cond_pago_org", CoerceHelperUtils.str(req.getCondIdOriginal(), 21))
              .addValue("apro_comer_userid", CoerceHelperUtils.str(req.getUserApprovalId(), 20))
              .addValue("co_motproven", CoerceHelperUtils.toSmallInt(req.getProyecto(), 30), Types.SMALLINT)
              .addValue("peso", CoerceHelperUtils.dec(req.getPeso(), 5), Types.DECIMAL)
              .addValue("fle_valor", CoerceHelperUtils.dec(req.getFleValor(), 5), Types.DECIMAL)
              .addValue("fle_peso", CoerceHelperUtils.dec(req.getFlePeso(), 5), Types.DECIMAL)
              .addValue("fle_proveedor_id", CoerceHelperUtils.toSmallInt(req.getFleProveedorId(), 0), Types.SMALLINT)
              .addValue("fle_usuario_id", CoerceHelperUtils.toSmallInt(req.getFleUsuarioId(), 0), Types.SMALLINT)
              .addValue("fle_fecha", CoerceHelperUtils.toTimestamp(req.getFleFecha()), Types.TIMESTAMP);

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

      CoerceHelperUtils.validateDetailInvoiceRequired(req);

      var call =
          new SimpleJdbcCall(jdbcTemplate).withProcedureName("MSW_SP_GUARDAR_FACTURA_VTA_DET");

      MapSqlParameterSource p =
          new MapSqlParameterSource()
              .addValue("detalle_id", req.getDetalleId())
              .addValue("pedido_id", req.getPedidoId())
              .addValue("docu", CoerceHelperUtils.str(req.getFacVtaDetDocuOrigen(), 30))
              .addValue("tipo", CoerceHelperUtils.str(req.getFacVtaDetIdTipo(), 15)) // usa el mismo nombre
              .addValue("linea", CoerceHelperUtils.str(req.getFacVtaDetLinea(), 5)) // o Types.INTEGER si el SP
              .addValue("articulo", CoerceHelperUtils.str(req.getFacVtaDetArticulo(), 30))
              .addValue("descripcion_articulo", CoerceHelperUtils.str(req.getFacVtaDetDescArticulo(), 255))
              .addValue("unidad", CoerceHelperUtils.str(req.getFacVtaDetUnidadMedida(), 10)) // verifica tipo
              .addValue("bodega", CoerceHelperUtils.str(req.getFacVtadetBodega(), 10))
              .addValue("cantidad", CoerceHelperUtils.dec(req.getFacVtaDetCantidad()),java.sql.Types.DECIMAL)
              .addValue("costo",CoerceHelperUtils.dec(req.getFacVtaCostoUnitario()), java.sql.Types.DECIMAL)
              .addValue("precio", CoerceHelperUtils.dec(req.getFacVtaDetPrecioUnitario()), java.sql.Types.DECIMAL)
              .addValue("descuento", CoerceHelperUtils.dec(req.getFacVtaDetDescuento()),java.sql.Types.DECIMAL)
              .addValue("impuesto", CoerceHelperUtils.dec(req.getFacVtaDetImpuestoIva()), java.sql.Types.DECIMAL)
              .addValue("total", CoerceHelperUtils.dec(req.getFacVtaDetTotal()), java.sql.Types.DECIMAL)
              .addValue("descripcion_org", CoerceHelperUtils.str(req.getDescripcionOrginal(), 300))
              .addValue("valor_org", CoerceHelperUtils.dec(req.getValorOrginal()),java.sql.Types.DECIMAL)
              .addValue("descuento_org", CoerceHelperUtils.dec(req.getDescuentoOrginal()), java.sql.Types.DECIMAL)
              .addValue( "descuento_final",   CoerceHelperUtils.dec(req.getDescuento()), java.sql.Types.DECIMAL)
              .addValue("txCat", CoerceHelperUtils.str(req.getCategoria(), 300))
              .addValue("txLin", CoerceHelperUtils.str(req.getLinea(), 300))
              .addValue("txCla", CoerceHelperUtils.str(req.getClase(), 300))
              .addValue("txCfgItmL", CoerceHelperUtils.str(req.getConfiguracionItemL(), 300))
              .addValue("fle_valor", CoerceHelperUtils.dec(req.getFleValor()), java.sql.Types.DECIMAL)
              ;

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
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_CREACION_FACTURA_VTA")
              .declareParameters(
                  // Parámetros de entrada
                  new SqlParameter("DOCU", Types.VARCHAR),
                  new SqlParameter("TIPO_DOCU", Types.VARCHAR));
      MapSqlParameterSource p =
          new MapSqlParameterSource()
              .addValue("DOCU", CoerceHelperUtils.str(request.getFactVtaDocuOrigen(), 30))
              .addValue("TIPO_DOCU", CoerceHelperUtils.str(request.getFactVtaIdTipo(), 15));
      jdbcCall.execute(p);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error al ejecutar el SP de detalle de factura: " + e.getMessage(), e);
    }
  }

  public responseInvoiceVtaDto executeSpResponseInvoiceVta(ResponseConsultInvoiceRequest request) {
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
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
      MapSqlParameterSource p =
          new MapSqlParameterSource()
              .addValue("docu", CoerceHelperUtils.str(request.getDocumento(), 300))
              .addValue("tipo", CoerceHelperUtils.str(request.getTipoDocumento(), 15));
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
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_ELIMINACION_FACTURA_VTA_CAB_DET")
              .declareParameters(
                  // Parámetros de entrada
                  new SqlParameter("docu", Types.VARCHAR), new SqlParameter("tipo", Types.VARCHAR));
      MapSqlParameterSource p =
          new MapSqlParameterSource()
              .addValue("docu", CoerceHelperUtils.str(request.getDocumento(), 300))
              .addValue("tipo", CoerceHelperUtils.str(request.getTipoDocumento(), 15));
      jdbcCall.execute(p);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error al ejecutar el SP de detalle de factura: " + e.getMessage(), e);
    }
  }
}
