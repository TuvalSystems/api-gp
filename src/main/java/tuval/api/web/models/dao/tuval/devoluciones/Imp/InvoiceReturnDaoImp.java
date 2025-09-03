package tuval.api.web.models.dao.tuval.devoluciones.Imp;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.dao.tuval.devoluciones.InvoiceReturnDao;
import tuval.api.web.models.dto.devolucion.DetailDevolutionRequest;
import tuval.api.web.models.dto.devolucion.HeaderDevolutionRequest;
import tuval.api.web.models.entity.tuval.devoluciones.DevolutionResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;

@Repository
public class InvoiceReturnDaoImp implements InvoiceReturnDao {

  @Autowired
  @Qualifier("tuvalJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  private SimpleJdbcCall spDevolucionIv;

  @PostConstruct
  public void init() {
    validateConfiguration();
    this.spDevolucionIv =
        new SimpleJdbcCall(jdbcTemplate)
            // .withSchemaName("dbo") // descomenta si tu SP está en dbo y tu usuario no
            // tiene default schema
            .withProcedureName("sp_msw_movimientos_iv")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("tipotrx", Types.VARCHAR),
                new SqlParameter("numdoc", Types.VARCHAR),
                new SqlParameter("numdocori", Types.VARCHAR),
                new SqlParameter("bodorigen", Types.VARCHAR),
                new SqlParameter("boddestino", Types.VARCHAR),
                new SqlParameter("binitem", Types.VARCHAR),
                new SqlParameter("binori", Types.VARCHAR),
                new SqlParameter("bindes", Types.VARCHAR),
                new SqlParameter("observacion", Types.VARCHAR),
                new SqlParameter("item", Types.VARCHAR),
                new SqlParameter("cantidad", Types.DECIMAL), // numeric(19,5)
                new SqlParameter("secitem", Types.INTEGER),
                new SqlParameter("compartimento", Types.VARCHAR),
                new SqlInOutParameter("idmov", Types.INTEGER),
                new SqlOutParameter("docgp", Types.VARCHAR), // OUT varchar(30)
                new SqlParameter("numtran", Types.DECIMAL), // numeric(19,5)
                new SqlParameter("idfila", Types.INTEGER),
                new SqlParameter("fecpro", Types.TIMESTAMP), // datetime
                new SqlParameter("userid", Types.CHAR),
                new SqlParameter("modo", Types.SMALLINT),
                new SqlParameter("submodo", Types.SMALLINT),
                new SqlParameter("equipo", Types.VARCHAR),
                new SqlInOutParameter("codret", Types.INTEGER),
                new SqlInOutParameter("msj", Types.VARCHAR));
  }

  public void validateConfiguration() {
    if (jdbcTemplate == null) {
      throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> ValidateGenerationDocumentVTANC(
      String document, String tipo) {
    Map<String, Object> response = new HashMap<>();

    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_VALIDAR_GENERACION_NC_VTA")
              .declareParameters(
                  new SqlParameter("@docu", Types.VARCHAR),
                  new SqlParameter("@tipo", Types.VARCHAR))
              .returningResultSet(
                  "response",
                  (rs, rowNum) -> {
                    Map<String, Object> row = new HashMap<>();

                    row.put("codigo", rs.getString("I_vCODIGO"));
                    row.put("mensaje", rs.getString("I_vMENSAJE"));
                    row.put("tipoDocumento", rs.getString("TIPO_DOC_GENERADO"));
                    row.put("documentoGenerado", rs.getString("DOC_GENERADO"));
                    return row;
                  });

      Map<String, Object> params = new HashMap<>();
      params.put("docu", document != null ? document.trim() : "");
      params.put("tipo", tipo != null ? tipo.trim() : "");

      Map<String, Object> result = jdbcCall.execute(params);

      @SuppressWarnings("unchecked")
      List<Map<String, String>> resultSet = (List<Map<String, String>>) result.get("response");

      if (!resultSet.isEmpty()) {
        Map<String, String> firstRow = resultSet.get(0);

        response.put("success", true);
        response.put("message", "Devolución validada correctamente.");
        response.put("data", firstRow); // contiene las 4 columnas
      } else {
        response.put("success", false);
        response.put("message", "No se devolvió información desde el procedimiento.");
      }
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Error al validar la devolución.");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> eliminacionNcCabDet(String document, String tipo) {
    Map<String, Object> response = new HashMap<>();
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_ELIMINACION_NC_VTA_CAB_DET")
              .declareParameters(
                  new SqlParameter("@docu", Types.VARCHAR),
                  new SqlParameter("@tipo", Types.VARCHAR));

      Map<String, Object> params = new HashMap<>();
      params.put("docu", document != null ? document.trim() : "");
      params.put("tipo", tipo != null ? tipo.trim() : "");

      jdbcCall.execute(params); // Ejecutar sin esperar retorno
      response.put("success", true);
      response.put("message", "Devolución eliminada correctamente.");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Error al eliminar la devolucion.");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> IncidenciaNcVta(
      String document, String tipo, String codigo, String message) {
    Map<String, Object> response = new HashMap<>();
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_GUARDAR_INCIDENCIA_NC_VTA")
              .declareParameters(
                  new SqlParameter("@docu", Types.VARCHAR),
                  new SqlParameter("@tipo", Types.VARCHAR),
                  new SqlParameter("@codigo", Types.VARCHAR),
                  new SqlParameter("@descripcion", Types.VARCHAR),
                  new SqlParameter("@tipo_docu_gene", Types.VARCHAR),
                  new SqlParameter("@docu_gene", Types.VARCHAR));
      Map<String, Object> params = new HashMap<>();
      params.put("docu", document != null ? document.trim() : "");
      params.put("tipo", tipo != null ? tipo.trim() : "");
      params.put("codigo", codigo != null ? codigo.trim() : "");
      params.put("descripcion", message != null ? message.trim() : "");
      params.put("tipo_docu_gene", 0);
      params.put("docu_gene", "");

      jdbcCall.execute(params); // solo ejecutamos

      response.put("success", true);
      response.put("message", "Incidencia registrada correctamente.");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Error al registrar la incidencia.");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> procesarCabeceraNotaCredito(
      String docu,
      String idTipo,
      String puntoVenta,
      String docId,
      String cliente,
      String vendedor,
      String direccion,
      String condPago,
      String notaDocu,
      String fecha,
      String bodega,
      double subtotal,
      double descuentoComercial,
      double impuesto,
      double total) {

    Map<String, Object> response = new HashMap<>();
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_GUARDAR_NC_VTA_CAB")
              .declareParameters(
                  new SqlParameter("@docu", Types.VARCHAR),
                  new SqlParameter("@id_tipo", Types.VARCHAR),
                  new SqlParameter("@punto_venta", Types.VARCHAR),
                  new SqlParameter("@doc_id", Types.VARCHAR),
                  new SqlParameter("@cliente", Types.VARCHAR),
                  new SqlParameter("@vendedor", Types.VARCHAR),
                  new SqlParameter("@direccion", Types.VARCHAR),
                  new SqlParameter("@cond_pago", Types.VARCHAR),
                  new SqlParameter("@nota_docu", Types.VARCHAR),
                  new SqlParameter("@fecha", Types.VARCHAR),
                  new SqlParameter("@bodega", Types.VARCHAR),
                  new SqlParameter("@subtotal", Types.DOUBLE),
                  new SqlParameter("@desc_comercial", Types.DOUBLE),
                  new SqlParameter("@impuesto", Types.DOUBLE),
                  new SqlParameter("@total", Types.DOUBLE));

      Map<String, Object> params = new HashMap<>();
      params.put("docu", docu);
      params.put("id_tipo", idTipo);
      params.put("punto_venta", puntoVenta);
      params.put("doc_id", docId);
      params.put("cliente", cliente);
      params.put("vendedor", vendedor);
      params.put("direccion", direccion);
      params.put("cond_pago", condPago);
      params.put("nota_docu", notaDocu);
      params.put("fecha", fecha); // Asegúrate que sea un string ISO o tipo Date
      params.put("bodega", bodega);
      params.put("subtotal", subtotal);
      params.put("desc_comercial", descuentoComercial);
      params.put("impuesto", impuesto);
      params.put("total", total);

      jdbcCall.execute(params); // Sin esperar resultado

      response.put("success", true);
      response.put("message", "Cabecera de nota de crédito registrada correctamente.");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Error al registrar la cabecera de la nota de crédito.");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> procesarDetalleNotaCredito(
      String docu,
      String tipo,
      double linea,
      String articulo,
      String descripcionArticulo,
      String unidad,
      String bodega,
      double cantidad,
      double costo,
      double precio,
      double descuento,
      double impuesto,
      double total) {

    Map<String, Object> response = new HashMap<>();
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_GUARDAR_NC_VTA_DET")
              .declareParameters(
                  new SqlParameter("@docu", Types.VARCHAR),
                  new SqlParameter("@tipo", Types.VARCHAR),
                  new SqlParameter("@linea", Types.DOUBLE),
                  new SqlParameter("@articulo", Types.VARCHAR),
                  new SqlParameter("@descripcion_articulo", Types.VARCHAR),
                  new SqlParameter("@unidad", Types.VARCHAR),
                  new SqlParameter("@bodega", Types.VARCHAR),
                  new SqlParameter("@cantidad", Types.DOUBLE),
                  new SqlParameter("@costo", Types.DOUBLE),
                  new SqlParameter("@precio", Types.DOUBLE),
                  new SqlParameter("@descuento", Types.DOUBLE),
                  new SqlParameter("@impuesto", Types.DOUBLE),
                  new SqlParameter("@total", Types.DOUBLE));

      Map<String, Object> params = new HashMap<>();
      params.put("docu", docu);
      params.put("tipo", tipo);
      params.put("linea", linea);
      params.put("articulo", articulo);
      params.put("descripcion_articulo", descripcionArticulo);
      params.put("unidad", unidad);
      params.put("bodega", bodega);
      params.put("cantidad", cantidad);
      params.put("costo", costo);
      params.put("precio", precio);
      params.put("descuento", descuento);
      params.put("impuesto", impuesto);
      params.put("total", total);

      jdbcCall.execute(params); // Sin esperar resultado
      response.put("success", true);
      response.put("message", "Detalle de nota de crédito registrada correctamente.");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Error al registrar el detalle de la nota de crédito.");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> guardarIncidenciaFactura(
      String docu, String tipo, String codigo, String descripcion) {
    Map<String, Object> response = new HashMap<>();
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_GUARDAR_INCIDENCIA_FACTURA_VTA")
              .declareParameters(
                  new SqlParameter("@docu", Types.VARCHAR),
                  new SqlParameter("@tipo", Types.VARCHAR),
                  new SqlParameter("@codigo", Types.VARCHAR),
                  new SqlParameter("@descripcion", Types.VARCHAR));

      Map<String, Object> params = new HashMap<>();
      params.put("docu", docu != null ? docu.trim() : "");
      params.put("tipo", tipo != null ? tipo.trim() : "");
      params.put("codigo", codigo != null ? codigo.trim() : "");
      params.put("descripcion", descripcion != null ? descripcion.trim() : "");
      params.put("tipo_docu_gene", 0);
      params.put("docu_gene", "");

      jdbcCall.execute(params);

      response.put("success", true);
      response.put("message", "Incidencia de factura registrada correctamente.");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Error al registrar la incidencia de la factura.");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> obtenerTipoProducto(String articulo) {
    Map<String, Object> response = new HashMap<>();
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_OBTENER_TIPO_PRODUCTO")
              .declareParameters(new SqlParameter("@articulo", Types.VARCHAR))
              .returningResultSet("response", (rs, rowNum) -> rs.getInt("TIPO_PRODUCTO"));

      Map<String, Object> params = new HashMap<>();
      params.put("articulo", articulo != null ? articulo.trim() : "");

      Map<String, Object> result = jdbcCall.execute(params);

      @SuppressWarnings("unchecked")
      List<Integer> resultSet = (List<Integer>) result.get("response");
      int tipoProducto = resultSet.isEmpty() ? 0 : resultSet.get(0);

      response.put("success", true);
      response.put("message", "Tipo de producto obtenido correctamente.");
      response.put("tipo_producto", tipoProducto);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Error al obtener el tipo de producto.");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> generarNotaCredito(String docu, String tipo) {
    Map<String, Object> response = new HashMap<>();
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_CREACION_NC_VTA")
              .declareParameters(
                  new SqlParameter("@docu", Types.VARCHAR),
                  new SqlParameter("@tipo", Types.VARCHAR));

      Map<String, Object> params = new HashMap<>();
      params.put("DOCU", docu != null ? docu.trim() : "");
      params.put("TIPO_DOCU", tipo != null ? tipo.trim() : "");
      jdbcCall.execute(params);

      response.put("success", true);
      response.put("message", "Nota de crédito generada correctamente.");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Error al generar la nota de crédito.");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> respuestaNotaCredito(String docu, String tipo) {
    Map<String, Object> response = new HashMap<>();
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_VALIDAR_GENERACION_NC_VTA")
              .declareParameters(
                  new SqlParameter("@docu", Types.VARCHAR),
                  new SqlParameter("@tipo", Types.VARCHAR))
              .returningResultSet(
                  "response",
                  (rs, rowNum) -> {
                    Map<String, Object> row = new HashMap<>();

                    row.put("documentoOrigen", rs.getString("I_vDOCU_ORIGEN"));
                    row.put("tipo", rs.getString("I_vID_TIPO"));
                    row.put("codigo", rs.getString("I_vCODIGO"));
                    row.put("tipoDocGenerado", rs.getString("TIPO_DOC_GENERADO"));
                    row.put("documentoGenerado", rs.getString("DOC_GENERADO"));
                    row.put("mensaje", rs.getString("I_vMENSAJE"));
                    return row;
                  });

      Map<String, Object> params = new HashMap<>();
      params.put("docu", docu != null ? docu.trim() : "");
      params.put("tipo", tipo != null ? tipo.trim() : "");

      Map<String, Object> result = jdbcCall.execute(params);

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> resultSet = (List<Map<String, Object>>) result.get("response");

      if (!resultSet.isEmpty()) {
        response.put("success", true);
        response.put("message", "Respuesta de la nota de crédito obtenida correctamente.");
        response.put("respuesta", resultSet);
        return ResponseEntity.ok(response);
      } else {
        return buildError("Error al obtener la respuesta de la nota de crédito.");
      }

    } catch (Exception e) {
      return buildException(e);
    }
  }

  @Override
  public DevolutionResponse executeSpStoreHeaderDevolution(HeaderDevolutionRequest req, int modo) {
    String obs = req.getObservacion();
    if (obs != null && obs.length() > 200) {
      obs = obs.substring(0, 200);
    }
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("tipotrx", "")
            .addValue("numdoc", req.getDocumento())
            .addValue("numdocori", req.getPedidoOrigen())
            .addValue("bodorigen", req.getBodega())
            .addValue("boddestino", "")
            .addValue("binitem", "")
            .addValue("binori", "")
            .addValue("bindes", "")
            .addValue("observacion", obs)
            .addValue("item", "")
            .addValue("cantidad", java.math.BigDecimal.ZERO, Types.DECIMAL)
            .addValue("secitem", 0)
            .addValue("compartimento", null, Types.VARCHAR)
            .addValue("idmov", 0) // INOUT inicia en 0 como en .NET
            .addValue("numtran", java.math.BigDecimal.ZERO, Types.DECIMAL)
            .addValue("idfila", 0)
            .addValue("fecpro", null, Types.TIMESTAMP)
            .addValue("userid", req.getUsuario()) // SQL Server CHAR(15) rellena espacios
            .addValue("modo", modo) // 1
            .addValue("submodo", 0)
            .addValue("equipo", "wserver")
            .addValue("codret", 0) // INOUT
            .addValue("msj", "")
        // .addValue("docgp", "")
        ;
    try {
      Map<String, Object> out = spDevolucionIv.execute(in);
      int codret = CoerceHelperUtils.getInt(out.get("codret"), 0);
      int idmov = CoerceHelperUtils.getInt(out.get("idmov"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));

      if (codret != 0) {
        throw new IllegalStateException(
            "Error al guardar la cabecera pedido="
                + req.getPedidoOrigen()
                + ", idmov="
                + idmov
                + ", codret="
                + codret
                + ", msj="
                + msj);
      }
      return new DevolutionResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public DevolutionResponse executeSpStoreDetaillDevolution(
      DetailDevolutionRequest det, HeaderDevolutionRequest cab, int idmov, int secitem, int modo) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("tipotrx", "")
            .addValue("numdoc", cab.getDocumento())
            .addValue("numdocori", cab.getPedidoOrigen())
            .addValue("bodorigen", cab.getBodega())
            .addValue("boddestino", "")
            .addValue("binitem", det.getItem())
            .addValue("binori", det.getCompartimento())
            .addValue("bindes", "")
            .addValue("observacion", null, Types.VARCHAR)
            .addValue("item", det.getItem())
            .addValue("cantidad", det.getCantidad())
            .addValue("secitem", secitem) // secuencia siempre es 1 al parecer
            .addValue("compartimento", null, Types.VARCHAR)
            .addValue("idmov", idmov)
            .addValue("numtran", java.math.BigDecimal.ZERO, Types.DECIMAL)
            .addValue("idfila", 0)
            .addValue("fecpro", null, Types.TIMESTAMP)
            .addValue("userid", cab.getUsuario())
            .addValue("modo", modo) // 2
            .addValue("submodo", 0)
            .addValue("equipo", "wserver")
            .addValue("codret", 0)
            .addValue("msj", "");

    try {
      Map<String, Object> out = spDevolucionIv.execute(in);
      int codret = CoerceHelperUtils.getInt(out.get("codret"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException(
            "Error al guardar el detalle de la devolucion, documento="
                + cab.getDocumento()
                + ", idmov="
                + idmov
                + ", codret="
                + codret
                + ", msj="
                + msj);
      }
      return new DevolutionResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public DevolutionResponse executeSpGenerateMovimentDevolution(
      HeaderDevolutionRequest req, int idmov, int modo) {
         MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("tipotrx", "")
            .addValue("numdoc", req.getDocumento())
            .addValue("numdocori", req.getPedidoOrigen())
            .addValue("bodorigen", req.getBodega())
            .addValue("boddestino", "")
            .addValue("binitem", "")
            .addValue("binori", "")
            .addValue("bindes", "")
            .addValue("observacion", null, Types.VARCHAR)
            .addValue("item", "")
            .addValue("cantidad", java.math.BigDecimal.ZERO, Types.DECIMAL)
            .addValue("secitem", 0)
            .addValue("compartimento", null, Types.VARCHAR)
            .addValue("idmov", idmov)
            .addValue("numtran", java.math.BigDecimal.ZERO, Types.DECIMAL)
            .addValue("idfila", 0)
            .addValue("fecpro", null, Types.TIMESTAMP)
            .addValue("userid", req.getUsuario())
            .addValue("modo", modo) // 3
            .addValue("submodo", 0)
            .addValue("equipo", "wserver")
            .addValue("docgp", "") // INOUT
            .addValue("codret", 0) // INOUT
            .addValue("msj", "");
    try {
      Map<String, Object> out = spDevolucionIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException(
            "Error al generar movimiento de devolucion, documento="
                + req.getDocumento()
                + ", idmov="
                + idmov
                + ", codret="
                + codret
                + ", msj="
                + msj);
      }
      return new DevolutionResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
      }

  @Override
  public ResponseEntity<Map<String, Object>> consultItemBin(
      String item, String bodegaOrigen, int modo) {
    Map<String, Object> response = new HashMap<>();
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("sp_msw_movimientos_iv")
              .declareParameters(
                  new SqlParameter("numdoc", Types.VARCHAR),
                  new SqlParameter("bodorigen", Types.VARCHAR),
                  new SqlParameter("boddestino", Types.VARCHAR),
                  new SqlParameter("observacion", Types.VARCHAR),
                  new SqlInOutParameter("idmov", Types.INTEGER),
                  new SqlInOutParameter("docgp", Types.VARCHAR),
                  new SqlParameter("numdocori", Types.VARCHAR),
                  new SqlParameter("userid", Types.CHAR),
                  new SqlParameter("modo", Types.INTEGER),
                  new SqlParameter("tipotrx", Types.VARCHAR),
                  new SqlParameter("binitem", Types.VARCHAR),
                  new SqlParameter("binori", Types.VARCHAR),
                  new SqlParameter("bindes", Types.VARCHAR),
                  new SqlParameter("item", Types.VARCHAR),
                  new SqlParameter("cantidad", Types.REAL),
                  new SqlParameter("secitem", Types.INTEGER),
                  new SqlParameter("compartimento", Types.VARCHAR),
                  new SqlParameter("numtran", Types.NUMERIC),
                  new SqlParameter("idfila", Types.INTEGER),
                  new SqlParameter("fecpro", Types.DATE),
                  new SqlParameter("submodo", Types.SMALLINT),
                  new SqlParameter("equipo", Types.VARCHAR),
                  new SqlInOutParameter("codret", Types.INTEGER),
                  new SqlInOutParameter("msj", Types.VARCHAR));

      Map<String, Object> params = new HashMap<>();
      params.put("numdoc", "");
      params.put("bodorigen", bodegaOrigen);
      params.put("boddestino", "");
      params.put("observacion", "");
      params.put("idmov", 0);
      params.put("docgp", ""); // OUT
      params.put("numdocori", "");
      params.put("userid", "");
      params.put("modo", modo); // normalmente 19

      params.put("tipotrx", "");
      params.put("binitem", "");
      params.put("binori", "");
      params.put("bindes", "");
      params.put("item", item);
      params.put("cantidad", 0);
      params.put("secitem", 0);
      params.put("compartimento", "");
      params.put("numtran", 0);
      params.put("idfila", 0);
      params.put("fecpro", "");
      params.put("submodo", 0);

      params.put("equipo", "wserver");
      params.put("codret", 0);
      params.put("msj", "");

      Map<String, Object> result = jdbcCall.execute(params);

      // int codRet = (int) result.get("codret");
      // String mensaje = (String) result.get("msj");

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> registros = (List<Map<String, Object>>) result.get("#result-set-1");

      // ✅ Ordenar de forma descendente por "bodega"
      registros.sort(
          (a, b) -> {
            // 📌 Obtener los valores de "bodega", si no existe el valor, se usa un string
            // vacío
            String bodegaA = (String) a.getOrDefault("bodega", "");
            String bodegaB = (String) b.getOrDefault("bodega", "");

            // 🔄 Orden descendente (de Z a A)
            return bodegaB.compareTo(bodegaA);
          });

      if (registros != null && !registros.isEmpty()) {
        Map<String, Object> registro = registros.get(0); // 👈 Tomamos el primer elemento
        response.put("success", true);
        response.put("message", "Compartimento generado correctamente");
        response.put("data", registro);
        response.put("lista", registros);
        return ResponseEntity.ok(response);
      } else {
        return buildError("No se encontraron resultados para el compartimento.");
      }

    } catch (Exception e) {
      return buildException(e);
    }
  }

  @Override
  public ResponseEntity<Map<String, Object>> getGroupEconomicCLiente(String ruc) {
    try {
      Map<String, Object> response = new HashMap<>();
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("MSW_SP_OBTENER_CLIENTE_GRUPO_ECONOMICO")
              .declareParameters(new SqlParameter("@cliente", Types.VARCHAR))
              .returningResultSet("response", (rs, rowNum) -> rs.getString("RUC").trim());

      Map<String, Object> params = new HashMap<>();
      params.put("cliente", ruc != null ? ruc.trim() : "");
      Map<String, Object> result = jdbcCall.execute(params);
      @SuppressWarnings("unchecked")
      List<String> resultSet = (List<String>) result.get("response");
      if (!resultSet.isEmpty()) {
        response.put("success", true);
        response.put("message", "Grupo económico obtenido correctamente.");
        response.put("CGE_RUC", resultSet);
        return ResponseEntity.ok(response);
      } else {
        return buildError("No se encontró el grupo económico para el cliente proporcionado.");
      }
    } catch (Exception e) {
      return buildException(e);
    }
  }

  private ResponseEntity<Map<String, Object>> buildError(String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", message);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  private ResponseEntity<Map<String, Object>> buildException(Exception e) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
