package tuval.api.web.models.dao.tuval.guiaremision.Imp;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dao.tuval.guiaremision.ShippingGuideDao;
import tuval.api.web.models.dto.bodegas.guiaRemision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.HeaderGuideTransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;

@Repository
public class ShippingGuideDaoImp implements ShippingGuideDao {

  @Autowired
  @Qualifier("tuvalJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  private SimpleJdbcCall spMovimientosIv;
  private SimpleJdbcCall spCheckCodeAccessDeliveryNote;

  @PostConstruct
  public void init() {
    validateConfiguration();
    this.spMovimientosIv =
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

    this.spCheckCodeAccessDeliveryNote =
        new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("MSW_SP_VALIDAR_GENERACION_GUIA_REMISION")
            .useInParameterNames("docu") // opcional, ayuda a claridad
            .declareParameters(new SqlParameter("docu", Types.VARCHAR));
  }

  public void validateConfiguration() {
    if (jdbcTemplate == null) {
      throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
    }
  }

  @Override
  public CheckCodeAccessDeliveryNoteResponse checkGuiaRemision(String numDoc) {
    try {
      MapSqlParameterSource in =
          new MapSqlParameterSource()
              .addValue(
                  "docu",
                  (numDoc == null || numDoc.isBlank()) ? null : numDoc.trim(),
                  java.sql.Types.VARCHAR);

      Map<String, Object> out = spCheckCodeAccessDeliveryNote.execute(in);
      @SuppressWarnings("unchecked")
      var rows = (java.util.List<java.util.Map<String, Object>>) out.get("#result-set-1");
      if (rows == null || rows.isEmpty()) {
        throw new RuntimeException("No se encontraron filas en MSW_SP_VALIDAR_GENERACION_GUIA_REMISION");
      }

      var row = rows.get(0);
      // Sugerencia: CLAVE_ACCESO suele ser de 49 dígitos (SRI) -> usa String en tu POJO
      String claveAcceso = asStr(row.get("CLAVE_ACCESO"));
      String documentoOrigen = asStr(row.get("I_vDOCU_ORIGEN"));
      int codigo = getInt(row.get("I_vCODIGO"), 0); // ojo: viene vacío en tu log -> quedará 0
      String mensaje = asStr(row.get("I_vMENSAJE"));
      String docGenerado = asStr(row.get("DOC_GENERADO"));
     
      return new CheckCodeAccessDeliveryNoteResponse(
          claveAcceso, documentoOrigen, codigo, mensaje, docGenerado);

    } catch (Exception e) {
      throw new RuntimeException("Error ejecutando MSW_SP_VALIDAR_GENERACION_GUIA_REMISION", e);
    }
  }

  /** proceso de guardado de la guia */
  public HeaderGuideTransferVtaResponse executeSpStoreHeaderTransferVta(
      HeaderGuideTransferVtaRequest req) {

    // saneo mínimo (evita overflow al SP)
    String obs = req.getObservacion();
    if (obs != null && obs.length() > 200) {
      obs = obs.substring(0, 200);
    }
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("tipotrx", "")
            .addValue("numdoc", req.getPedido())
            .addValue("numdocori", "")
            .addValue("bodorigen", req.getBodegaOrigen())
            .addValue("boddestino", req.getBodegaDestino())
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
            .addValue("modo", 1)
            .addValue("submodo", 0)
            .addValue("equipo", "wserver")
            .addValue("codret", 0) // INOUT
            .addValue("msj", "")
        // .addValue("docgp", "")
        ;
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = getInt(out.get("codret"), 0);
      int idmov = getInt(out.get("idmov"), 0);
      String docgp = out.get("docgp") != null ? out.get("docgp").toString() : "";
      String msj = out.get("msj") != null ? out.get("msj").toString() : "";

      if (codret != 0) {
        throw new IllegalStateException(
            "Error al guardar la cabecera (modo=1). pedido="
                + req.getPedido()
                + ", idmov="
                + idmov
                + ", codret="
                + codret
                + ", msj="
                + msj);
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(
          "Fallo en modo=1 (guardar cabecera). pedido=" + req.getPedido() + ": " + e.getMessage(),
          e);
    }
  }

  public DetailGuideTransferVtaResponse executeSpStoreDetailGuideTransferVta(
      DetailGuideTransferVtaRequest det,
      HeaderGuideTransferVtaRequest cab,
      int idmov,
      int secitem) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("tipotrx", "")
            .addValue("numdoc", cab.getPedido())
            .addValue("numdocori", "")
            .addValue("bodorigen", cab.getBodegaOrigen())
            .addValue("boddestino", cab.getBodegaDestino())
            .addValue("binitem", det.getItem())
            .addValue("binori", det.getCompartimento())
            .addValue("bindes", "")
            .addValue("observacion", null, Types.VARCHAR)
            .addValue("item", det.getItem())
            .addValue("cantidad", det.getCantidad())
            .addValue("secitem", 1) // secuencia siempre es 1 al parecer
            .addValue("compartimento", null, Types.VARCHAR)
            .addValue("idmov", idmov)
            .addValue("numtran", java.math.BigDecimal.ZERO, Types.DECIMAL)
            .addValue("idfila", 0)
            .addValue("fecpro", null, Types.TIMESTAMP)
            .addValue("userid", cab.getUsuario())
            .addValue("modo", 2)
            .addValue("submodo", 0)
            .addValue("equipo", "wserver")
            .addValue("codret", 0)
            .addValue("msj", "");

    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = toInt(out.get("codret"), 0);
      String msj = out.get("msj") != null ? String.valueOf(out.get("msj")) : "";
      if (codret != 0) {
        throw new IllegalStateException(
            "Error al guardar el detalle (modo=2). pedido="
                + cab.getPedido()
                + ", idmov="
                + idmov
                + ", codret="
                + codret
                + ", msj="
                + msj);
      }
      return new DetailGuideTransferVtaResponse(codret, msj, idmov, det.getItem(), 1);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(
          "Fallo en modo=2 (guardar detalle). pedido="
              + cab.getPedido()
              + ", idmov="
              + idmov
              + ": "
              + e.getMessage(),
          e);
    }
  }

  public HeaderGuideTransferVtaResponse executeSpGenerateMovimentTransferVta(
      HeaderGuideTransferVtaRequest req, int idmov) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("tipotrx", "")
            .addValue("numdoc", req.getPedido())
            .addValue("numdocori", "")
            .addValue("bodorigen", req.getBodegaOrigen())
            .addValue("boddestino", req.getBodegaDestino())
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
            .addValue("modo", 3)
            .addValue("submodo", 0)
            .addValue("equipo", "wserver")
            .addValue("docgp", "") // INOUT
            .addValue("codret", 0) // INOUT
            .addValue("msj", "");
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = getInt(out.get("codret"), 0);
      String docgp = out.get("docgp") != null ? out.get("docgp").toString().trim() : "";
      String msj = out.get("msj") != null ? out.get("msj").toString().trim() : "";
      if (codret != 0) {
        throw new IllegalStateException(
            "Error al generar movimiento (modo=3). pedido="
                + req.getPedido()
                + ", idmov="
                + idmov
                + ", codret="
                + codret
                + ", msj="
                + msj);
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(
          "Fallo en modo=3 (generar movimiento). pedido="
              + req.getPedido()
              + ", idmov="
              + idmov
              + ": "
              + e.getMessage(),
          e);
    }
  }

  private static int getInt(Object o, int dflt) {
    if (o == null) return dflt;
    if (o instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(o.toString().trim());
    } catch (Exception e) {
      return dflt;
    }
  }

  private static int toInt(Object o, int dflt) {
    if (o == null) return dflt;
    if (o instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(o.toString().trim());
    } catch (Exception ignored) {
      return dflt;
    }
  }

  private static String asStr(Object v) {
    return v == null ? "" : v.toString().trim();
  }
}
