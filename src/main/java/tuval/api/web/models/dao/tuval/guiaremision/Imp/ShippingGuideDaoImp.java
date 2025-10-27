package tuval.api.web.models.dao.tuval.guiaremision.Imp;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.dao.tuval.guiaremision.ShippingGuideDao;
import tuval.api.web.models.dto.bodegas.guiaRemision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.HeaderGuideDispatchShipmentRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.HeaderGuideTransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailCheckGuideAnyResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
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
                new SqlInOutParameter("msj", Types.VARCHAR),
                new SqlParameter("idguia", Types.INTEGER),
                new SqlParameter("guia", Types.VARCHAR));

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
        return new CheckCodeAccessDeliveryNoteResponse("", "", 0, "", "");
      }

      var row = rows.get(0);
      // Sugerencia: CLAVE_ACCESO suele ser de 49 dígitos (SRI) -> usa String en tu POJO
      String claveAcceso = CoerceHelperUtils.stringOrEmpty(row.get("CLAVE_ACCESO"));
      String documentoOrigen = CoerceHelperUtils.stringOrEmpty(row.get("I_vDOCU_ORIGEN"));
      int codigo =
          CoerceHelperUtils.getInt(
              row.get("I_vCODIGO"), 0); // ojo: viene vacío en tu log -> quedará 0
      String mensaje = CoerceHelperUtils.stringOrEmpty(row.get("I_vMENSAJE"));
      String docGenerado = CoerceHelperUtils.stringOrEmpty(row.get("DOC_GENERADO"));

      return new CheckCodeAccessDeliveryNoteResponse(
          claveAcceso, documentoOrigen, codigo, mensaje, docGenerado);

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /** Transferencia de venta Reposición */
  @Override
  public HeaderGuideTransferVtaResponse executeSpStoreHeaderTransferVta(
      HeaderGuideTransferVtaRequest req, int modo) {

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
            .addValue("modo", modo) // 1
            .addValue("submodo", 0)
            .addValue("equipo", "wserver")
            .addValue("codret", 0) // INOUT
            .addValue("msj", "")
            .addValue("idguia", req.getIdGuiaRemision())
            .addValue("guia", req.getGuiaRemision());
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.getInt(out.get("codret"), 0);
      int idmov = CoerceHelperUtils.getInt(out.get("idmov"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));

      if (codret != 0) {
        throw new IllegalStateException(
            String.format(
                "Error al guardar la cabecera de transferencia.\n"
                    + "  Pedido: %s\n"
                    + "  ID Movimiento: %d\n"
                    + "  Código de retorno: %d\n"
                    + "  Mensaje del sistema: %s",
                req.getPedido(), idmov, codret, msj));
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public DetailGuideTransferVtaResponse executeSpStoreDetailGuideTransferVta(
      DetailGuideTransferVtaRequest det,
      HeaderGuideTransferVtaRequest cab,
      int idmov,
      int secitem,
      int modo) {
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
            .addValue("msj", "")
            .addValue("idguia", cab.getIdGuiaRemision())
            .addValue("guia", cab.getGuiaRemision());

    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException(
            String.format(
                "Error al guardar el detalle de transferencia.\n"
                    + "  Pedido: %s\n"
                    + "  ID Movimiento: %d\n"
                    + "  Código de retorno: %d\n"
                    + "  Item: %s\n"
                    + "  Mensaje del sistema: %s",
                cab.getPedido(), idmov, codret, det.getItem(), msj));
      }
      return new DetailGuideTransferVtaResponse(codret, msj, idmov, det.getItem(), secitem);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public HeaderGuideTransferVtaResponse executeSpGenerateMovimentTransferVta(
      HeaderGuideTransferVtaRequest req, int idmov, int modo) {
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
            .addValue("modo", modo) // 3
            .addValue("submodo", 0)
            .addValue("equipo", "wserver")
            .addValue("docgp", "") // INOUT
            .addValue("codret", 0) // INOUT
            .addValue("msj", "")
            .addValue("idguia", "")
            .addValue("guia", "");
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException(
            String.format(
                "Error al guardar el movimiento de transferencia.\n"
                    + "  Pedido: %s\n"
                    + "  ID Movimiento: %d\n"
                    + "  Código de retorno: %d\n"
                    + "  Mensaje del sistema: %s",
                req.getPedido(), idmov, codret, msj));
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /** despacho de venta */
  @Override
  public HeaderGuideTransferVtaResponse executeSpStoreHeaderGuideDispatchVta(
      HeaderGuideDispatchShipmentRequest req, int modo) {

    // saneo mínimo (evita overflow al SP)
    String obs = req.getObservacion();
    if (obs != null && obs.length() > 200) {
      obs = obs.substring(0, 200);
    }
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("tipotrx", "")
            .addValue("numdoc", req.getPedido())
            .addValue("numdocori", req.getPedidoNuevo())
            .addValue("bodorigen", req.getBodegaOrigen())
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
            .addValue("idguia", req.getIdGuiaRemision())
            .addValue("guia", req.getGuiaRemision());
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.getInt(out.get("codret"), 0);
      int idmov = CoerceHelperUtils.getInt(out.get("idmov"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));

      if (codret != 0) {
        throw new IllegalStateException(
            String.format(
                "Error al guardar la cabecera de despacho.\n"
                    + "  Pedido: %s\n"
                    + "  ID Movimiento: %d\n"
                    + "  Código de retorno: %d\n"
                    + "  Mensaje del sistema: %s",
                req.getPedido(), idmov, codret, msj));
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public DetailGuideTransferVtaResponse executeSpStoreDetailGuideDispatchVta(
      DetailGuideTransferVtaRequest det,
      HeaderGuideDispatchShipmentRequest cab,
      int idmov,
      int secitem,
      int modo) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("tipotrx", "")
            .addValue("numdoc", cab.getPedido())
            .addValue("numdocori", "")
            .addValue("bodorigen", cab.getBodegaOrigen())
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
            .addValue("msj", "")
            .addValue("idguia", cab.getIdGuiaRemision())
            .addValue("guia", cab.getGuiaRemision());

    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException(
            String.format(
                "Error al guardar el detalle de despacho.\n"
                    + "  Pedido: %s\n"
                    + "  ID Movimiento: %d\n"
                    + "  Código de retorno: %d\n"
                    + "  Item: %s\n"
                    + "  Mensaje del sistema: %s",
                cab.getPedido(), idmov, codret, det.getItem(), msj));
      }
      return new DetailGuideTransferVtaResponse(codret, msj, idmov, det.getItem(), secitem);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(
          "Fallo en guardar detalle de despacho, pedido="
              + cab.getPedido()
              + ", idmov="
              + idmov
              + ": "
              + e.getMessage()
              + ", item="
              + det.getItem(),
          e);
    }
  }

  @Override
  public HeaderGuideTransferVtaResponse executeSpGenerateMovimentDispatchVta(
      HeaderGuideDispatchShipmentRequest req, int idmov, int modo) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("tipotrx", "")
            .addValue("numdoc", req.getPedido())
            .addValue("numdocori", "")
            .addValue("bodorigen", req.getBodegaOrigen())
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
            .addValue("msj", "")
            .addValue("idguia", "")
            .addValue("guia", "");
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        if (codret != 0) {
          throw new IllegalStateException(
              String.format(
                  "Error al generar el movimiento de despacho.\n"
                      + "  Pedido: %s\n"
                      + "  ID Movimiento: %d\n"
                      + "  Código de retorno: %d\n"
                      + "  Mensaje del sistema: %s",
                  req.getPedido(), idmov, codret, msj));
        }
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public HeaderCheckGuideDispatchResponse getAjusteGuideDispatchVtaDao(
      String orderNumber, String newOrderNumber) {
    String SQL =
        """
          SELECT TOP (1)
            mc_id      AS idMovimiento,
            mc_numdoc  AS numDoc,
            mc_numdocori  AS numDocOri,
            mc_origen  AS origen,
            RTRIM(mc_usring) AS userIngreso,
            mc_fecgp   AS fechaGp,
            RTRIM(mc_docgp)  AS docGp,
            mc_guia_remision_id as idGuiaRemision,
            mc_guia_remision as guiaRemision
        FROM dbo.tbmsw_movimiento_cab
        WHERE mc_numdoc = ?
          AND mc_numdocori = ?
          AND mc_tipotrx = 'DSP'
          AND mc_docgp IS NOT NULL
        ORDER BY mc_fecgp DESC
        """;
    try {
      var mapper =
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(
              HeaderCheckGuideDispatchResponse.class);
      return jdbcTemplate.queryForObject(SQL, mapper, orderNumber, newOrderNumber);
    } catch (EmptyResultDataAccessException e) {
      return null;
    } catch (DataAccessException e) {
      System.out.println("getAjusteGuideDispatchVtaDao" + e.getMessage());
      throw new IllegalStateException("Error al consultar la base de datos", e);
    }
  }

  @Override
  public HeaderCheckGuideDispatchResponse getAjusteGuideTransferVtaDao(
      String orderNumber,
      String newOrderNumber,
      String origen,
      Integer idGuiaRemision,
      String guiaRemision) {
    String SQL =
        """
          SELECT
            mc_id             AS idMovimiento,
            mc_numdoc         AS numDoc,
            mc_numdocori      AS numDocOri,
            mc_origen         AS origen,
            mc_destino        AS destino,
            RTRIM(mc_usring)  AS userIngreso,
            mc_fecgp          AS fechaGp,
            RTRIM(mc_docgp)   AS docGp,
            mc_guia_remision_id AS idGuiaRemision,
            mc_guia_remision AS guiaRemision
        FROM dbo.tbmsw_movimiento_cab
        WHERE mc_numdoc   = ?
          AND mc_tipotrx  = 'TRD'
          AND mc_docgp IS NOT NULL
          AND mc_guia_remision_id = ?
          AND mc_guia_remision = ?
        """;
    List<Object> args = new ArrayList<>();
    args.add(orderNumber);
    args.add(idGuiaRemision);
    args.add(guiaRemision);
    if (newOrderNumber != null && !newOrderNumber.isBlank()) {
      SQL += " AND mc_numdocori = ?";
      args.add(newOrderNumber);
    }

    if (origen != null && !origen.isBlank()) {
      SQL += " AND mc_origen = ?";
      args.add(origen);
    }
    SQL += " ORDER BY mc_fecgp DESC";

    try {
      var mapper = new BeanPropertyRowMapper<>(HeaderCheckGuideDispatchResponse.class);
      return jdbcTemplate.queryForObject(SQL, mapper, args.toArray());
    } catch (EmptyResultDataAccessException e) {
      return null;
    } catch (DataAccessException e) {
      throw new IllegalStateException("Error al consultar la base de datos", e);
    }
  }

  @Override
  public HeaderCheckGuideDispatchResponse getAjusteGuideRepositionVtaDao(
      String orderNumber,
      String origen,
      Integer idMovimiento,
      Integer idGuiaRemision,
      String guiaRemision) {
    String SQL =
        """
            SELECT TOP (1)
          mc_id        AS idMovimiento,
          mc_numdoc    AS numDoc,
          mc_numdocori AS numDocOri,
          mc_origen    AS origen,
          RTRIM(mc_usring) AS userIngreso,
          mc_fecgp     AS fechaGp,
          RTRIM(mc_docgp)  AS docGp,
          mc_guia_remision_id AS idGuiaRemision,
          mc_guia_remision AS guiaRemision
        FROM dbo.tbmsw_movimiento_cab
        WHERE mc_numdoc = ?
          AND mc_tipotrx = 'TRR'
          AND mc_docgp IS NOT NULL
          AND mc_guia_remision_id = ?
          AND mc_guia_remision = ?
        """;
    List<Object> args = new ArrayList<>();
    args.add(orderNumber);
    args.add(idGuiaRemision);
    args.add(guiaRemision);

    if (idMovimiento != null) {
      SQL += " AND mc_id = ?";
      args.add(idMovimiento);
    }
    if (origen != null && !origen.isBlank()) {
      SQL += " AND mc_origen = ?";
      args.add(origen);
    }
    SQL += " ORDER BY mc_fecgp DESC";

    try {
      var mapper = new BeanPropertyRowMapper<>(HeaderCheckGuideDispatchResponse.class);
      return jdbcTemplate.queryForObject(SQL, mapper, args.toArray());
    } catch (EmptyResultDataAccessException e) {
      return null;
    } catch (DataAccessException e) {
      throw new IllegalStateException("Error al consultar la base de datos", e);
    }
  }

  @Override
  public DetailCheckGuideAnyResponse getAjustmentDetailGuide(
      String orderNumber,
      String newOrderNumber,
      String item,
      Integer idMovimiento,
      Integer cantidad) {
    String SQL =
        """
          SELECT TOP (1)
          md_id        AS idMovimiento,
          md_tipotrx   AS tipoTrx,
          md_numdoc    AS numDoc,
          md_numdocori AS numDocOri,
          md_item      AS item,
          md_cantidad  AS cantidad,
          md_origen    AS origen,
          md_fecmod     AS fechaGp,
          RTRIM(md_docgp)  AS docGp
        FROM dbo.tbmsw_movimiento_det
        WHERE md_numdoc = ?
          AND md_item = ?
          AND md_docgp IS NOT NULL,
        """;
    List<Object> args = new ArrayList<>();
    args.add(orderNumber);
    args.add(item);

    if (idMovimiento != null) {
      SQL += " AND md_id = ?";
      args.add(idMovimiento);
    }
    if (newOrderNumber != null) {
      SQL += " AND md_numdocori = ?";
      args.add(newOrderNumber);
    }
    if (cantidad != null && cantidad > 0) {
      SQL += " AND md_cantidad = ?";
      args.add(cantidad);
    }
    SQL += " ORDER BY md_fecmod DESC";

    try {
      var mapper = new BeanPropertyRowMapper<>(DetailCheckGuideAnyResponse.class);
      return jdbcTemplate.queryForObject(SQL, mapper, args.toArray());
    } catch (EmptyResultDataAccessException e) {
      return null;
    } catch (DataAccessException e) {
      throw new IllegalStateException("Error al consultar la base de datos", e);
    }
  }
}
