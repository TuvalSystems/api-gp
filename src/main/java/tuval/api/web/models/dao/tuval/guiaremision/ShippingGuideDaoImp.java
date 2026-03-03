package tuval.api.web.models.dao.tuval.guiaremision;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailCheckGuideAnyResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.ValidateExistsDeliveryNoteResponse;
import tuval.api.web.models.request.bodegas.guiaremision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.request.bodegas.guiaremision.DispatchDeliveryNoteMovementRequest;
import tuval.api.web.models.request.bodegas.guiaremision.HeaderGuideDispatchShipmentRequest;
import tuval.api.web.models.request.bodegas.guiaremision.HeaderGuideTransferVtaRequest;
import tuval.api.web.models.request.bodegas.guiaremision.ValidateGuideDispatchRequest;
import tuval.api.web.models.request.bodegas.guiaremision.ValidateGuideSalesTransferedRequest;
import tuval.api.web.models.request.bodegas.guiaremision.crearGuia.DestinationDeliveryNoteRequest;
import tuval.api.web.models.request.bodegas.guiaremision.crearGuia.DetailDeliveryNoteRequest;
import tuval.api.web.models.request.bodegas.guiaremision.crearGuia.HeaderDeliveryNoteRequest;

@Repository
public class ShippingGuideDaoImp implements ShippingGuideDao {

  private final JdbcTemplate jdbcTemplate;

  private final SimpleJdbcCall spCabeceraGuiaRemision;
  private final SimpleJdbcCall spDestinatarioGuiaRemision;
  private final SimpleJdbcCall spDetalleGuiaRemision;
  private final SimpleJdbcCall spCreateAccessCodeDeliveryNote;
  private final SimpleJdbcCall spUpdateAccessCodeDeliveryNote;
  private final SimpleJdbcCall spCheckExistsDeliverNote;
  private final SimpleJdbcCall spMovimientosIv;
  private final SimpleJdbcCall spCheckCodeAccessDeliveryNote;

  public ShippingGuideDaoImp(@Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.spCabeceraGuiaRemision = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
        .withProcedureName("MSW_SP_GUARDAR_GUIA_REMISION_CAB_jgms")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(
            new SqlParameter("docu", Types.VARCHAR),
            new SqlParameter("establecimiento", Types.VARCHAR),
            new SqlParameter("punto_emision", Types.VARCHAR),
            new SqlParameter("secuencial", Types.VARCHAR),
            new SqlParameter("direccion_partida", Types.VARCHAR),
            new SqlParameter("razon_social_transportista", Types.VARCHAR),
            new SqlParameter("identificador_transportista", Types.VARCHAR),
            new SqlParameter("tipo_identificador_transportista", Types.VARCHAR),
            new SqlParameter("rise", Types.VARCHAR),
            new SqlParameter("fecha_inicio", Types.TIMESTAMP),
            new SqlParameter("fecha_fin", Types.TIMESTAMP),
            new SqlParameter("placa", Types.VARCHAR),
            new SqlParameter("correo_electronico", Types.VARCHAR),
            new SqlParameter("observacion", Types.VARCHAR),
            new SqlOutParameter("codret", Types.INTEGER));
    // guardar destinatario guia remision

    this.spDestinatarioGuiaRemision = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
        .withProcedureName("MSW_SP_GUARDAR_GUIA_REMISION_DESTINATARIO_jgms")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(
            new SqlParameter("docu", Types.VARCHAR),
            new SqlParameter("secuencial", Types.INTEGER),
            new SqlParameter("identificador_destinatario", Types.VARCHAR),
            new SqlParameter("razon_social_destinatario", Types.VARCHAR),
            new SqlParameter("direccion_destinatario", Types.VARCHAR),
            new SqlParameter("motivo_traslado", Types.VARCHAR),
            new SqlParameter("documento_aduanero", Types.VARCHAR),
            new SqlParameter("establecimiento_destino", Types.VARCHAR),
            new SqlParameter("desc_establecimiento_destino", Types.VARCHAR),
            new SqlParameter("ruta", Types.VARCHAR),
            new SqlParameter("codigo_sustento", Types.VARCHAR),
            new SqlParameter("docu_sustento", Types.VARCHAR),
            new SqlParameter("nume_autorizacion", Types.VARCHAR),
            new SqlParameter("fecha", Types.DATE),
            new SqlOutParameter("codret", Types.INTEGER));

    // guardar detalle guia remision
    this.spDetalleGuiaRemision = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
        .withProcedureName("MSW_SP_GUARDAR_GUIA_REMISION_DET_jgms")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(
            new SqlParameter("docu", Types.VARCHAR),
            new SqlParameter("secuencial_destinatario", Types.INTEGER),
            new SqlParameter("secuencial_detalle", Types.INTEGER),
            new SqlParameter("identificacion_destinatario", Types.VARCHAR),
            new SqlParameter("numero_docu_sustento", Types.VARCHAR),
            new SqlParameter("codigo_producto", Types.VARCHAR),
            new SqlParameter("codigo_adicional", Types.VARCHAR),
            new SqlParameter("descripcion_producto", Types.NVARCHAR),
            new SqlParameter("cantidad", Types.DECIMAL), // numeric(19,5)
            new SqlParameter("unidad_medida", Types.VARCHAR),
            new SqlParameter("bodega", Types.VARCHAR),
            new SqlOutParameter("codret", Types.INTEGER));

    // sp para generar las claves de acceso de la guia de remision
    this.spCreateAccessCodeDeliveryNote = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
        .withProcedureName("MSW_SP_GENERAR_CLAVE_ACCESO_GUIA_REMISION")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(new SqlParameter("docu", Types.VARCHAR));

    // sp para actualizar la guia de remision con la clave de acceso
    this.spUpdateAccessCodeDeliveryNote = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
        .withProcedureName("MSW_SP_ACTUALIZAR_DOCU_GENERADO_GUIA_REMISION")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(new SqlParameter("docu", Types.VARCHAR));

    // sp para validar la existencia de la guia de remision
    this.spCheckExistsDeliverNote = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
        .withProcedureName("sp_validar_existencia_guia_remision_tuv_jgms")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(
            new SqlParameter("docu", Types.VARCHAR),
            new SqlOutParameter("existe_cabecera", Types.BIT),
            new SqlInOutParameter("codret", Types.INTEGER),
            new SqlInOutParameter("msj", Types.VARCHAR));
    // para movimientos
    this.spMovimientosIv = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
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
            new SqlParameter("guia", Types.VARCHAR),
            new SqlParameter("hash_intento", Types.VARCHAR));

    this.spCheckCodeAccessDeliveryNote = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
        .withProcedureName("MSW_SP_VALIDAR_GENERACION_GUIA_REMISION")
        .withoutProcedureColumnMetaDataAccess()
        .useInParameterNames("docu") // opcional, ayuda a claridad
        .declareParameters(new SqlParameter("docu", Types.VARCHAR));

  }

  @Override
  public CheckCodeAccessDeliveryNoteResponse checkGuiaRemision(String numDoc) {
    try {
      MapSqlParameterSource in = new MapSqlParameterSource()
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
      // Sugerencia: CLAVE_ACCESO suele ser de 49 dígitos (SRI) -> usa String en tu
      // POJO
      String claveAcceso = CoerceHelperUtils.stringOrEmpty(row.get("CLAVE_ACCESO"));
      String documentoOrigen = CoerceHelperUtils.stringOrEmpty(row.get("I_vDOCU_ORIGEN"));
      int codigo = CoerceHelperUtils.getInt(
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
    MapSqlParameterSource in = new MapSqlParameterSource()
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
        .addValue("guia", req.getGuiaRemision())
        .addValue("hash_intento", req.getHashInterno());
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret    = CoerceHelperUtils.getInt(out.get("codret"), 0);
      int idmov     = CoerceHelperUtils.getInt(out.get("idmov"), 0);
      String docgp  = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj    = CoerceHelperUtils.stringOrEmpty(out.get("msj"));

      if (codret != 0) {
        throw new IllegalStateException("""
            Error al guardar la cabecera de egreso de transferencia.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Mensaje del sistema: %s""".formatted(req.getPedido(), idmov, codret, msj));
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
    MapSqlParameterSource in = new MapSqlParameterSource()
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
        .addValue("guia", cab.getGuiaRemision())
        .addValue("hash_intento", "");

    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException("""
            Error al guardar el detalle de egreso de transferencia de venta.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Item: %s
              Mensaje del sistema: %s""".formatted(cab.getPedido(), idmov, codret, det.getItem(), msj));

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
    MapSqlParameterSource in = new MapSqlParameterSource()
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
        .addValue("guia", "")
        .addValue("hash_intento", "");
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException("""
            Error al generar el movimiento de salida de transferencia de venta.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Mensaje del sistema: %s""".formatted(req.getPedido(), idmov, codret, msj));
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
    MapSqlParameterSource in = new MapSqlParameterSource()
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
        .addValue("guia", req.getGuiaRemision())
        .addValue("hash_intento", req.getHashInterno());
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.getInt(out.get("codret"), 0);
      int idmov = CoerceHelperUtils.getInt(out.get("idmov"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));

      if (codret != 0) {
        throw new IllegalStateException("""
            Error al guardar la cabecera de despacho.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Mensaje del sistema: %s""".formatted(req.getPedido(), idmov, codret, msj));
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
    MapSqlParameterSource in = new MapSqlParameterSource()
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
        .addValue("guia", cab.getGuiaRemision())
        .addValue("hash_intento", "");

    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException("""
            Error al guardar el detalle de despacho.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Item: %s
              Mensaje del sistema: %s""".formatted(cab.getPedido(), idmov, codret, det.getItem(), msj));

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
    MapSqlParameterSource in = new MapSqlParameterSource()
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
        .addValue("guia", "")
        .addValue("hash_intento", "");
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException("""
            Error al generar el movimiento de despacho.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Mensaje del sistema: %s""".formatted(req.getPedido(), idmov, codret, msj));
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public HeaderGuideTransferVtaResponse executeFixSpGenerateMovementDispatchDeliveryNote(
      DispatchDeliveryNoteMovementRequest req, int modo) {
    MapSqlParameterSource in = new MapSqlParameterSource()
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
        .addValue("idmov", req.getIdMovimiento())
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
        .addValue("guia", "")
        .addValue("hash_intento", "");
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException("""
            Error al generar el movimiento de despacho.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Mensaje del sistema: %s""".formatted(req.getPedido(), req.getIdMovimiento(), codret, msj));
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, req.getIdMovimiento(), docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public HeaderCheckGuideDispatchResponse getAjusteGuideDispatchVtaDao(
      ValidateGuideDispatchRequest req) {
    // 1. Quitamos el ORDER BY del bloque principal
    StringBuilder sql = new StringBuilder("""
        SELECT
            mc_id                 AS idMovimiento,
            mc_numdoc             AS numDoc,
            mc_numdocori          AS numDocOri,
            mc_origen             AS origen,
            RTRIM(mc_usring)      AS userIngreso,
            mc_fecgp              AS fechaGp,
            RTRIM(mc_docgp)       AS docGp,
            mc_guia_remision_id   AS idGuiaRemision,
            mc_guia_remision      AS guiaRemision
        FROM dbo.tbmsw_movimiento_cab
        WHERE mc_numdoc = ?
          AND mc_docgp IS NOT NULL
          AND mc_tipotrx = ?
          AND mc_guia_remision_id = ?
          AND mc_guia_remision = ?
          AND mc_tipomov = ?
          AND mc_hash_intento = ?
        """);

    List<Object> args = new ArrayList<>();
    args.add(req.getOrderNumber());
    args.add(req.getTipoTrx());
    args.add(req.getIdGuiaRemision());
    args.add(req.getGuiaRemision());
    args.add(req.getTipo());
    args.add(req.getHashInterno()); // Se mapea al mc_hash_intento de la BD

    // 2. Agregamos condiciones dinámicas
    if (req.getNewOrderNumber() != null && !req.getNewOrderNumber().isBlank()) {
      sql.append(" AND mc_numdocori = ?");
      args.add(req.getNewOrderNumber());
    }


    try {
      var mapper = new BeanPropertyRowMapper<>(HeaderCheckGuideDispatchResponse.class);
      return jdbcTemplate.queryForObject(sql.toString(), mapper, args.toArray());
    } catch (EmptyResultDataAccessException e) {
      return null;
    } catch (DataAccessException e) {
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
    String SQL = """
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
    String SQL = """
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
    String SQL = """
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

  @Override
  public void executeSpStoreHeaderDeliveryNote(HeaderDeliveryNoteRequest h) {
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("docu", h.getGuiaRemision())
        .addValue("establecimiento", h.getEstablecimiento())
        .addValue("punto_emision", h.getPtoEmision())
        .addValue("secuencial", h.getSecuencial())
        .addValue("direccion_partida", h.getDireccionPartida())
        .addValue("razon_social_transportista", h.getDireccionPartida())
        .addValue("identificador_transportista", h.getIdentificacionTransportista())
        .addValue("tipo_identificador_transportista", h.getTipoIdentificacionTransportista())
        .addValue("rise", CoerceHelperUtils.emptyIfNull(h.getRise()))
        .addValue("fecha_inicio", h.getFechaInicio())
        .addValue("fecha_fin", h.getFechaFin())
        .addValue("placa", h.getPlaca())
        .addValue("correo_electronico", h.getCorreoElectronico())
        .addValue("observacion", h.getObservacion());

    try {
      Map<String, Object> out = spCabeceraGuiaRemision.execute(in);
      Integer codret = (Integer) out.get("codret");
      if (codret == 1) {
        throw new RuntimeException("Cabecera ya existente (codret=" + h.getGuiaRemision() + ")");
      }
      if (codret == null || (codret != 0 && codret != 1)) {
        throw new RuntimeException("Error al guardar la cabecera (codret=" + codret + ")");
      }
    } catch (Exception e) {
      throw new RuntimeException("SP executeSpStoreHeaderDeliveryNote falló: " + e.getMessage(), e);
    }
  }

  @Override
  public void executeSpStoreDestinationDeliveryNote(DestinationDeliveryNoteRequest h) {
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("docu", h.getGuiaRemision())
        .addValue("secuencial", h.getSecuencialDestinatario())
        .addValue("identificador_destinatario", h.getIdentificacionDestinatario())
        .addValue("razon_social_destinatario", h.getRazonSocialDestinatario())
        .addValue("direccion_destinatario", h.getDireccionDestinatario())
        .addValue("motivo_traslado", h.getMotivoTraslado())
        .addValue("documento_aduanero", h.getDocAduanero())
        .addValue("establecimiento_destino", h.getCodEstablecimientoDestino())
        .addValue("desc_establecimiento_destino", h.getDescEstablecimientoDestino())
        .addValue("ruta", h.getRuta())
        .addValue("codigo_sustento", CoerceHelperUtils.emptyIfNull(h.getDocSustento()))
        .addValue("docu_sustento", CoerceHelperUtils.emptyIfNull(h.getNumDocSustento()))
        .addValue("nume_autorizacion", CoerceHelperUtils.emptyIfNull(h.getNumAutoDocSustento()))
        .addValue("fecha", h.getFechaEmisionDocSustento());
    try {
      Map<String, Object> out = spDestinatarioGuiaRemision.execute(in);
      Integer codret = (Integer) out.get("codret");
      if (codret == null || (codret != 0 && codret != 1)) {
        throw new RuntimeException("Error al guardar el destinatario (codret=" + codret + ")");
      }
    } catch (Exception e) {
      throw new RuntimeException("SP destinatario falló: " + e.getMessage(), e);
    }
  }

  @Override
  public void executeSpStoreDetailDeliveryNote(DetailDeliveryNoteRequest d) {
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("docu", d.getGuiaRemision())
        .addValue("secuencial_destinatario", d.getSecuencialDestinatario())
        .addValue("secuencial_detalle", d.getSecuenciaDetalle())
        .addValue("identificacion_destinatario", d.getIdentificacionDestinatario())
        .addValue("numero_docu_sustento", CoerceHelperUtils.emptyIfNull(d.getDocSustento()))
        .addValue("codigo_producto", d.getCodInterno())
        .addValue("codigo_adicional", CoerceHelperUtils.emptyIfNull(d.getCodAdicional()))
        .addValue("descripcion_producto", d.getDescripcion())
        .addValue("cantidad", d.getCantidad())
        .addValue("unidad_medida", d.getUnidadMedida())
        .addValue("bodega", d.getGuiaBodega());

    try {
      Map<String, Object> out = spDetalleGuiaRemision.execute(in);
      System.out.println("Guardado detalle de guia de remisión" + out);
      Integer codret = (Integer) out.get("codret");
      if (codret == null || (codret != 0 && codret != 1)) {
        throw new RuntimeException(
            "Error al guardar el DETALLE de guía: codret="
                + codret
                + " [docu="
                + d.getGuiaRemision()
                + ", secDest="
                + d.getSecuencialDestinatario()
                + ", secDet="
                + d.getSecuenciaDetalle()
                + "]");
      }
    } catch (Exception e) {
      throw new RuntimeException("SP executeSpStoreDetailDeliveryNote falló: " + e.getMessage(), e);
    }
  }

  @Override
  public void executeSpCreateAccesoCodeDeliveryNote(HeaderDeliveryNoteRequest h) {
    MapSqlParameterSource in = new MapSqlParameterSource().addValue("docu", h.getGuiaRemision());
    try {
      Map<String, Object> out = spCreateAccessCodeDeliveryNote.execute(in);
    } catch (Exception e) {
      throw new RuntimeException(
          "SP executeSpCreateAccesoCodeDeliveryNote falló: " + e.getMessage(), e);
    }
  }

  @Override
  public void executeSpUpdateAccesoCodeDeliveryNote(HeaderDeliveryNoteRequest h) {
    MapSqlParameterSource in = new MapSqlParameterSource().addValue("docu", h.getGuiaRemision());
    try {
      Map<String, Object> out = spUpdateAccessCodeDeliveryNote.execute(in);
    } catch (Exception e) {
      throw new RuntimeException(
          "SP executeSpStoreAccesoCodeDeliveryNote falló: " + e.getMessage(), e);
    }
  }

  @Override
  public ValidateExistsDeliveryNoteResponse executeSpValidateExistsDeliveryNoteResponse(
      String docu) {
    MapSqlParameterSource in = new MapSqlParameterSource().addValue("docu", docu);
    try {
      Map<String, Object> out = spCheckExistsDeliverNote.execute(in);
      Integer codret = (Integer) out.get("codret");
      String mensaje = (String) out.get("msj");
      if (codret == null || codret != 0) {
        throw new RuntimeException(
            "Error al obtener la información de la guía de remisión (codret=" + codret + ")");
      }
      return new ValidateExistsDeliveryNoteResponse(
          codret != null ? codret : -1, mensaje != null ? mensaje : "");
    } catch (Exception e) {
      throw new RuntimeException(
          "SP executeSpValidateExistsDeliveryNoteResponse falló: " + e.getMessage(), e);
    }
  }

 
  public HeaderCheckGuideDispatchResponse getAjusttmentReceivedReservedDao(
      String orderNumber,
      String newOrderNumber,
      String origen,
      Integer idGuiaRemision,
      String guiaRemision) {
    String SQL = """
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
          AND mc_tipotrx  = 'RTC'
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

  /** sp para la recepcion de items de la guia */
  @Override
  public HeaderGuideTransferVtaResponse executeSpStoreHeaderReceivedGuideTransfered(
      HeaderGuideTransferVtaRequest req, int modo) {

    // saneo mínimo (evita overflow al SP)
    String obs = req.getObservacion();
    if (obs != null && obs.length() > 200) {
      obs = obs.substring(0, 200);
    }
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("tipotrx", "")
        .addValue("numdoc", req.getPedido())
        .addValue("numdocori", req.getNumTranferencia())
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
        .addValue("guia", req.getGuiaRemision())
        .addValue("hash_intento", req.getHashInterno());
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.getInt(out.get("codret"), 0);
      int idmov = CoerceHelperUtils.getInt(out.get("idmov"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));

      if (codret != 0) {
        throw new IllegalStateException("""
            Error al guardar la cabecera de recepcion de transferencia de reserva.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Mensaje del sistema: %s""".formatted(req.getPedido(), idmov, codret, msj));
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public DetailGuideTransferVtaResponse executeSpStoreDetailReceivedGuideTransfered(
      DetailGuideTransferVtaRequest det,
      HeaderGuideTransferVtaRequest cab,
      int idmov,
      int secitem,
      int modo) {
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("tipotrx", "")
        .addValue("numdoc", cab.getPedido())
        .addValue("numdocori", cab.getNumTranferencia())
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
        .addValue("guia", cab.getGuiaRemision())
        .addValue("hash_intento", "");

    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException("""
            Error al guardar el detalle de recepcion de transferencia de reservas.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Item: %s
              Mensaje del sistema: %s""".formatted(cab.getPedido(), idmov, codret, det.getItem(), msj));
      }
      return new DetailGuideTransferVtaResponse(codret, msj, idmov, det.getItem(), secitem);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public HeaderGuideTransferVtaResponse executeSpGenerateMovimentReceivedGuideTransfered(
      HeaderGuideTransferVtaRequest req, int idmov, int modo) {
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("tipotrx", "")
        .addValue("numdoc", req.getPedido())
        .addValue("numdocori", req.getNumTranferencia())
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
        .addValue("guia", "")
        .addValue("hash_intento", "");

    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException("""
            Error al generar el movimiento de transferencia de reserva.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Mensaje del sistema: %s""".formatted(req.getPedido(), idmov, codret, msj));
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /** proceso de recepcion guias de trransferencia de ventas */
  @Override
  public HeaderGuideTransferVtaResponse executeSpStoreHeaderReceivedSalesTransferedDao(
      HeaderGuideTransferVtaRequest req, int modo) {
    // saneo mínimo (evita overflow al SP)
    String obs = req.getObservacion();
    if (obs != null && obs.length() > 200) {
      obs = obs.substring(0, 200);
    }
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("tipotrx", "")
        .addValue("numdoc", req.getPedido())
        .addValue("numdocori", req.getNumTranferencia())
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
        .addValue("guia", req.getGuiaRemision())
        .addValue("hash_intento", req.getHashInterno());
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.getInt(out.get("codret"), 0);
      int idmov = CoerceHelperUtils.getInt(out.get("idmov"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));

      if (codret != 0) {
        throw new IllegalStateException("""
            Error al guardar la cabecera de recepcion de transferencia.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Mensaje del sistema: %s""".formatted(req.getPedido(), idmov, codret, msj));
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public DetailGuideTransferVtaResponse executeSpStoreDetailReceivedGuideSalesTransferedDao(
      DetailGuideTransferVtaRequest det,
      HeaderGuideTransferVtaRequest cab,
      int idmov,
      int secitem,
      int modo) {
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("tipotrx", "")
        .addValue("numdoc", cab.getPedido())
        .addValue("numdocori", cab.getNumTranferencia())
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
        .addValue("guia", cab.getGuiaRemision())
        .addValue("hash_intento", "");

    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {

        throw new IllegalStateException("""
            Error al guardar el detalle de recepcion de transferencia.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Item: %s
              Mensaje del sistema: %s""".formatted(cab.getPedido(), idmov, codret, det.getItem(), msj));
      }
      return new DetailGuideTransferVtaResponse(codret, msj, idmov, det.getItem(), secitem);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public HeaderGuideTransferVtaResponse executeSpGenerateMovimentReceivedGuideTransferedDao(
      HeaderGuideTransferVtaRequest req, int idmov, int modo) {
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("tipotrx", "")
        .addValue("numdoc", req.getPedido())
        .addValue("numdocori", req.getNumTranferencia())
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
        .addValue("guia", "")
        .addValue("hash_intento", "");
    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String docgp = CoerceHelperUtils.stringOrEmpty(out.get("docgp"));
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {
        throw new IllegalStateException("""
            Error al generar el movimiento de transferencia.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Mensaje del sistema: %s""".formatted(req.getPedido(), idmov, codret, msj));
      }
      return new HeaderGuideTransferVtaResponse(codret, msj, idmov, docgp);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public HeaderCheckGuideDispatchResponse getAdjustmentGuideSalesTransferedDao(
      ValidateGuideSalesTransferedRequest req) {
    String sqlText = """
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
          AND mc_docgp IS NOT NULL
          AND mc_guia_remision_id = ?
          AND mc_guia_remision = ?
          AND mc_origen = ?
          AND mc_destino = ?
          AND mc_tipotrx = ?
          AND mc_tipomov = ?
          AND mc_hash_intento = ?
          ORDER BY mc_fecgp DESC
        """;
    List<Object> args = new ArrayList<>();
    args.add(req.getOrderNumber());
    args.add(req.getIdGuiaRemision());
    args.add(req.getGuiaRemision());
    args.add(req.getOrigen());
    args.add(req.getDestino());
    args.add(req.getTipoTrx());
    args.add(req.getTipo());
    args.add(req.getHashInterno());

    try {
      var mapper = new BeanPropertyRowMapper<>(HeaderCheckGuideDispatchResponse.class);
      return jdbcTemplate.queryForObject(sqlText, mapper, args.toArray());
    } catch (EmptyResultDataAccessException e) {
      return null;
    } catch (DataAccessException e) {
      throw new IllegalStateException("Error al consultar la base de datos" + e);
    }
  }

  /** funcion para el almacenamiento del detalle de guias de reposiciones */

  @Override
  public DetailGuideTransferVtaResponse executeSpStoreDetailReceivedGuideReplenishmentTransferDao(
      DetailGuideTransferVtaRequest det,
      HeaderGuideTransferVtaRequest cab,
      int idmov,
      int secitem,
      int modo) {
    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("tipotrx", "")
        .addValue("numdoc", cab.getPedido())
        .addValue("numdocori", cab.getNumTranferencia())
        .addValue("bodorigen", cab.getBodegaOrigen())
        .addValue("boddestino", cab.getBodegaDestino())
        .addValue("binitem", det.getItem())
        .addValue("binori", "")
        .addValue("bindes", det.getCompartimento())
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
        .addValue("guia", cab.getGuiaRemision())
        .addValue("hash_intento", "");

    try {
      Map<String, Object> out = spMovimientosIv.execute(in);
      int codret = CoerceHelperUtils.toInt(out.get("codret"), 0);
      String msj = CoerceHelperUtils.stringOrEmpty(out.get("msj"));
      if (codret != 0) {

        throw new IllegalStateException("""
            Error al guardar el detalle de recepcion de transferencia.
              Pedido: %s
              ID Movimiento: %d
              Código de retorno: %d
              Item: %s
              Mensaje del sistema: %s""".formatted(cab.getPedido(), idmov, codret, det.getItem(), msj));
      }
      return new DetailGuideTransferVtaResponse(codret, msj, idmov, det.getItem(), secitem);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
