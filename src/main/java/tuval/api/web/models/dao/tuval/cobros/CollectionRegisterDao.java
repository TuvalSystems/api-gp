package tuval.api.web.models.dao.tuval.cobros;

import static tuval.api.web.models.Utils.CoerceHelperUtils.defaultIfBlank;
import static tuval.api.web.models.Utils.CoerceHelperUtils.emptyIfNull;
import static tuval.api.web.models.Utils.CoerceHelperUtils.toBigDecimalOrZero;
import static tuval.api.web.models.Utils.CoerceHelperUtils.toTimestamp;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionCheckHeaderRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionCheckInvoiceRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionCheckRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionDefaultInvoiceRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionDetailInvoicesRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionHeaderRequest;

@Repository
public class CollectionRegisterDao {
  private final SimpleJdbcCall spRegisterCollectionMs;
  private final SimpleJdbcCall spRegisterCollection;

  public CollectionRegisterDao(@Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate) {
    jdbcTemplate.setResultsMapCaseInsensitive(true);

    this.spRegisterCollectionMs =
        new SimpleJdbcCall(jdbcTemplate)
            .withSchemaName("dbo")
            .withProcedureName("sp_mc_registra_cobro_ms")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(

                // Cabecera del cobro
                new SqlParameter("numrecibo", Types.VARCHAR),
                new SqlParameter("idcliente", Types.VARCHAR),
                new SqlParameter("fechacobro", Types.TIMESTAMP),
                new SqlParameter("tipo", Types.VARCHAR),
                new SqlParameter("observacion", Types.VARCHAR),
                new SqlParameter("soldscto", Types.VARCHAR),
                new SqlParameter("idvendedor", Types.VARCHAR),
                new SqlParameter("monto", Types.NUMERIC),
                new SqlParameter("saldopend", Types.NUMERIC),
                new SqlParameter("bancomax", Types.VARCHAR),
                new SqlParameter("fecdeptrf", Types.TIMESTAMP),
                new SqlParameter("papeleta", Types.VARCHAR),
                new SqlParameter("idretencion", Types.VARCHAR),
                new SqlParameter("porcenret", Types.NUMERIC),

                // Factura individual
                new SqlParameter("numfac", Types.VARCHAR),
                new SqlParameter("montoapl", Types.NUMERIC),

                // Cheque individual
                new SqlParameter("bcocliente", Types.VARCHAR),
                new SqlParameter("numcta", Types.VARCHAR),
                new SqlParameter("fecvcto", Types.TIMESTAMP),
                new SqlParameter("numchq", Types.VARCHAR),
                new SqlParameter("montochq", Types.NUMERIC),
                new SqlParameter("origencta", Types.VARCHAR),
                new SqlParameter("feccad", Types.TIMESTAMP),
                new SqlParameter("numautor", Types.VARCHAR),

                // Table-Valued Parameters
                new SqlParameter("chequescobro", SqlTypeValue.TYPE_UNKNOWN),
                new SqlParameter("facturascobro", SqlTypeValue.TYPE_UNKNOWN),

                // Control
                new SqlParameter("userid", Types.CHAR),
                new SqlParameter("modo", Types.SMALLINT),
                new SqlParameter("submodo", Types.SMALLINT),
                new SqlParameter("equipo", Types.VARCHAR),

                // Salidas
                new SqlOutParameter("codret", Types.INTEGER),
                new SqlOutParameter("msj", Types.VARCHAR));

    this.spRegisterCollection =
        new SimpleJdbcCall(jdbcTemplate)
            .withSchemaName("dbo")
            .withProcedureName("sp_mc_registra_cobro")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                // Cabecera del cobro
                new SqlParameter("numrecibo", Types.VARCHAR),
                new SqlParameter("idcliente", Types.VARCHAR),
                new SqlParameter("fechacobro", Types.TIMESTAMP),
                new SqlParameter("tipo", Types.VARCHAR),
                new SqlParameter("observacion", Types.VARCHAR),
                new SqlParameter("soldscto", Types.VARCHAR),
                new SqlParameter("idvendedor", Types.VARCHAR),
                new SqlParameter("monto", Types.NUMERIC),
                new SqlParameter("saldopend", Types.NUMERIC),
                new SqlParameter("bancomax", Types.VARCHAR),
                new SqlParameter("fecdeptrf", Types.TIMESTAMP),
                new SqlParameter("papeleta", Types.VARCHAR),
                new SqlParameter("idretencion", Types.VARCHAR),
                new SqlParameter("porcenret", Types.NUMERIC),
                // Factura individual
                new SqlParameter("numfac", Types.VARCHAR),
                new SqlParameter("montoapl", Types.NUMERIC),

                // Cheque individual
                new SqlParameter("bcocliente", Types.VARCHAR),
                new SqlParameter("numcta", Types.VARCHAR),
                new SqlParameter("fecvcto", Types.TIMESTAMP),
                new SqlParameter("numchq", Types.VARCHAR),
                new SqlParameter("montochq", Types.NUMERIC),
                new SqlParameter("origencta", Types.VARCHAR),
                new SqlParameter("feccad", Types.TIMESTAMP),
                new SqlParameter("numautor", Types.VARCHAR),

                // Control
                new SqlParameter("userid", Types.CHAR),
                new SqlParameter("modo", Types.SMALLINT),
                new SqlParameter("submodo", Types.SMALLINT),
                new SqlParameter("equipo", Types.VARCHAR),

                // Salidas
                new SqlOutParameter("codret", Types.INTEGER),
                new SqlOutParameter("msj", Types.VARCHAR));
  }

  public String registerCashCollection(CollectionDefaultInvoiceRequest request, int modo) {
    try {
      CollectionHeaderRequest header = request.getCabecera();

      SQLServerDataTable checksTable = buildEmptyChecksTable();

      SQLServerDataTable invoicesTable = buildInvoicesTable(header, request.getDetalle());
      MapSqlParameterSource in =
          new MapSqlParameterSource()
              // Cabecera del cobro
              .addValue("numrecibo", emptyIfNull(header.getNumeroRecibo()))
              .addValue("idcliente", emptyIfNull(header.getRuc()))
              .addValue("fechacobro", toTimestamp(header.getFechaCobro()), Types.TIMESTAMP)
              .addValue("tipo", emptyIfNull(header.getTipoCobro()))
              .addValue("observacion", emptyIfNull(header.getObservacion()))
              .addValue("soldscto", emptyIfNull(header.getSolicitudDescuento()))
              .addValue("idvendedor", emptyIfNull(header.getIdVendedor()))
              .addValue("monto", toBigDecimalOrZero(header.getMontoCobro()), Types.NUMERIC)
              .addValue("saldopend", toBigDecimalOrZero(header.getSaldoPendiente()), Types.NUMERIC)
              .addValue("bancomax", emptyIfNull(header.getBancoTuval()))
              .addValue("fecdeptrf", toTimestamp(header.getFechaDeposito()), Types.TIMESTAMP)
              .addValue("papeleta", emptyIfNull(header.getNumeroPapeleta()))

              // Retenciones
              .addValue("idretencion", emptyIfNull(header.getNumeroRetencion()))
              .addValue(
                  "porcenret", toBigDecimalOrZero(header.getPorcentajeRetencion()), Types.NUMERIC)

              /*
               * Parámetros individuales de factura.
               * En modo 2 no se utilizan, porque las facturas
               * llegan mediante @facturascobro.
               */
              .addValue("numfac", "")
              .addValue("montoapl", BigDecimal.ZERO, Types.NUMERIC)

              /*
               * Parámetros individuales de cheque.
               * En efectivo no se utilizan.
               */
              .addValue("bcocliente", "")
              .addValue("numcta", "")
              .addValue("fecvcto", null, Types.TIMESTAMP)
              .addValue("numchq", "")
              .addValue("montochq", BigDecimal.ZERO, Types.NUMERIC)
              .addValue("origencta", "")

              // Datos adicionales de retención
              .addValue("feccad", toTimestamp(header.getFechaCaducidad()), Types.TIMESTAMP)
              .addValue("numautor", emptyIfNull(header.getNumeroAutorizacion()))

              // Table-Valued Parameters
              .addValue("chequescobro", checksTable, SqlTypeValue.TYPE_UNKNOWN)
              .addValue("facturascobro", invoicesTable, SqlTypeValue.TYPE_UNKNOWN)

              // Control
              .addValue("userid", "apigp")
              .addValue("modo", modo, Types.SMALLINT)
              .addValue("submodo", 0, Types.SMALLINT)
              .addValue("equipo", "apigp");

      Map<String, Object> result;
      try {
        result = spRegisterCollectionMs.execute(in);
      } catch (DataAccessException exception) {
        throw new RuntimeException(
            "No se pudo ejecutar el registro del cobro en SQL Server. "
                + "SQL Server no respondio o la conexion fue cerrada.",
            exception);
      }

      int returnCode =
          result.get("codret") != null ? ((Number) result.get("codret")).intValue() : -1;
      String message =
          result.get("msj") != null
              ? result.get("msj").toString()
              : "El procedimiento no devolvió ningún mensaje.";

      if (returnCode != 0) {
        throw new RuntimeException("Código " + returnCode + ": " + message);
      }

      return "Registro de cobro guardado correctamente.";

    } catch (SQLServerException exception) {
      throw new RuntimeException(
          "Error al construir los parametros tipo tabla: " + exception.getMessage(), exception);
    }
  }

  public String registerApplicationCollection(CollectionDetailInvoicesRequest req, int modo) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            // Cabecera del cobro
            .addValue("numrecibo", emptyIfNull(req.getRecibo()))
            .addValue("idcliente", emptyIfNull(req.getIdcliente()))
            .addValue("fechacobro", null, Types.TIMESTAMP)
            .addValue("tipo", null)
            .addValue("observacion", emptyIfNull(req.getObservacion()))
            .addValue("soldscto", "")
            .addValue("idvendedor", "")
            .addValue("monto", BigDecimal.ZERO, Types.NUMERIC)
            .addValue("saldopend", null, Types.NUMERIC)
            .addValue("bancomax", "")
            .addValue("fecdeptrf", null, Types.TIMESTAMP)
            .addValue("papeleta", null)

            // Retenciones
            .addValue("idretencion", "")
            .addValue("porcenret", BigDecimal.ZERO, Types.NUMERIC)

            /*
             * Parametros individuales de factura.
             * En modo 6 se usan para aplicar un cobro existente a una factura.
             */
            .addValue("numfac", emptyIfNull(req.getFactura()))
            .addValue("montoapl", toBigDecimalOrZero(req.getMonto()), Types.NUMERIC)

            /*
             * Parametros individuales de cheque.
             * En aplicacion directa no se utilizan.
             */
            .addValue("bcocliente", null)
            .addValue("numcta", null)
            .addValue("fecvcto", null, Types.TIMESTAMP)
            .addValue("numchq", null)
            .addValue("montochq", BigDecimal.ZERO, Types.NUMERIC)
            .addValue("origencta", null)

            // Datos adicionales de retencion
            .addValue("feccad", null, Types.TIMESTAMP)
            .addValue("numautor", null)

            // Table-Valued Parameters

            // Control
            .addValue("userid", "apigp")
            .addValue("modo", modo, Types.SMALLINT)
            .addValue("submodo", 0, Types.SMALLINT)
            .addValue("equipo", "apigp");
    Map<String, Object> result;
    try {
      result = spRegisterCollection.execute(in);
    } catch (DataAccessException exception) {
      throw new RuntimeException(
          "No se pudo ejecutar la aplicacion del cobro en SQL Server. "
              + "SQL Server no respondio o la conexion fue cerrada.",
          exception);
    }

    int returnCode = result.get("codret") != null ? ((Number) result.get("codret")).intValue() : -1;
    String message =
        result.get("msj") != null
            ? result.get("msj").toString()
            : "El procedimiento no devolvió ningún mensaje.";
    if (returnCode != 0) {
      throw new RuntimeException("Código " + returnCode + ": " + message);
    }
    return "Registro de cobro guardado correctamente.";
  }

  public String registerRetentionHeaderCollection(CollectionHeaderRequest header, int modo) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            // Cabecera del cobro
            .addValue("numrecibo", emptyIfNull(header.getNumeroRecibo()))
            .addValue("idcliente", emptyIfNull(header.getRuc()))
            .addValue("fechacobro", toTimestamp(header.getFechaCobro()), Types.TIMESTAMP)
            .addValue("tipo", emptyIfNull(header.getTipoCobro()))
            .addValue("observacion", emptyIfNull(header.getObservacion()))
            .addValue("soldscto", "")
            .addValue("idvendedor", emptyIfNull(header.getIdVendedor()))
            .addValue("monto", toBigDecimalOrZero(header.getMontoCobro()), Types.NUMERIC)
            .addValue("saldopend", BigDecimal.ZERO, Types.NUMERIC)
            .addValue("bancomax", "")
            .addValue("fecdeptrf", null, Types.TIMESTAMP)
            .addValue("papeleta", emptyIfNull(header.getNumeroRetencion()))

            // Retenciones
            .addValue("idretencion", emptyIfNull(header.getNumeroRetencion()))
            .addValue(
                "porcenret", toBigDecimalOrZero(header.getPorcentajeRetencion()), Types.NUMERIC)

            /*
             * Parametros individuales de factura.
             * En modo 6 se usan para aplicar un cobro existente a una factura.
             */
            .addValue("numfac", "")
            .addValue("montoapl", BigDecimal.ZERO, Types.NUMERIC)

            /*
             * Parametros individuales de cheque.
             * En aplicacion directa no se utilizan.
             */
            .addValue("bcocliente", null)
            .addValue("numcta", null)
            .addValue("fecvcto", null, Types.TIMESTAMP)
            .addValue("numchq", null)
            .addValue("montochq", BigDecimal.ZERO, Types.NUMERIC)
            .addValue("origencta", null)

            // Datos adicionales de retencion
            .addValue("feccad", toTimestamp(header.getFechaCaducidad()), Types.TIMESTAMP)
            .addValue("numautor", emptyIfNull(header.getNumeroAutorizacion()))

            // Table-Valued Parameters

            // Control
            .addValue("userid", "apigp")
            .addValue("modo", modo, Types.SMALLINT)
            .addValue("submodo", 0, Types.SMALLINT)
            .addValue("equipo", "apigp");
    Map<String, Object> result;
    try {
      result = spRegisterCollection.execute(in);
    } catch (DataAccessException exception) {
      throw new RuntimeException(
          "No se pudo ejecutar la retencion del cobro en SQL Server. "
              + "SQL Server no respondio o la conexion fue cerrada.",
          exception);
    }

    int returnCode = result.get("codret") != null ? ((Number) result.get("codret")).intValue() : -1;
    String message =
        result.get("msj") != null
            ? result.get("msj").toString()
            : "El procedimiento no devolvió ningún mensaje.";
    if (returnCode != 0) {
      throw new RuntimeException("Código " + returnCode + ": " + message);
    }
    return "Registro de cobro guardado correctamente.";
  }

  public String registerCheckCollection(CollectionCheckInvoiceRequest request, int modo) {
    try {
      CollectionCheckHeaderRequest header = request.getCabecera();

      SQLServerDataTable checksTable = buildChecksTable(header, request.getCheques());

      SQLServerDataTable invoicesTable = buildInvoicesTable(header, request.getDetalle());
      MapSqlParameterSource in =
          new MapSqlParameterSource()
              // Cabecera del cobro
              .addValue("numrecibo", emptyIfNull(header.getNumeroRecibo()))
              .addValue("idcliente", emptyIfNull(header.getRuc()))
              .addValue("fechacobro", toTimestamp(header.getFechaCobro()), Types.TIMESTAMP)
              .addValue("tipo", emptyIfNull(header.getTipoCobro()))
              .addValue("observacion", emptyIfNull(header.getObservacion()))
              .addValue("soldscto", emptyIfNull(header.getSolicitudDescuento()))
              .addValue("idvendedor", emptyIfNull(header.getIdVendedor()))
              .addValue("monto", toBigDecimalOrZero(header.getMontoCobro()), Types.NUMERIC)
              .addValue("saldopend", toBigDecimalOrZero(header.getSaldoPendiente()), Types.NUMERIC)
              .addValue("bancomax", "")
              .addValue("fecdeptrf", null, Types.TIMESTAMP)
              .addValue("papeleta", null)

              // Retenciones
              .addValue("idretencion", "")
              .addValue("porcenret", BigDecimal.ZERO, Types.NUMERIC)

              /*
               * Parámetros individuales de factura.
               * En modo 2 no se utilizan, porque las facturas
               * llegan mediante @facturascobro.
               */
              .addValue("numfac", "")
              .addValue("montoapl", BigDecimal.ZERO, Types.NUMERIC)

              /*
               * Parámetros individuales de cheque.
               * En efectivo no se utilizan.
               */
              .addValue("bcocliente", "")
              .addValue("numcta", "")
              .addValue("fecvcto", null, Types.TIMESTAMP)
              .addValue("numchq", "")
              .addValue("montochq", BigDecimal.ZERO, Types.NUMERIC)
              .addValue("origencta", "")

              // Datos adicionales de retencion
              .addValue("feccad", null, Types.TIMESTAMP)
              .addValue("numautor", "")

              // Table-Valued Parameters
              .addValue("chequescobro", checksTable, SqlTypeValue.TYPE_UNKNOWN)
              .addValue("facturascobro", invoicesTable, SqlTypeValue.TYPE_UNKNOWN)

              // Control
              .addValue("userid", "apigp")
              .addValue("modo", modo, Types.SMALLINT)
              .addValue("submodo", 0, Types.SMALLINT)
              .addValue("equipo", "apigp");

      Map<String, Object> result;
      try {
        result = spRegisterCollectionMs.execute(in);
      } catch (DataAccessException exception) {
        throw new RuntimeException(
            "No se pudo ejecutar el registro del cobro en SQL Server. "
                + getDataAccessDetail(exception),
            exception);
      }

      int returnCode =
          result.get("codret") != null ? ((Number) result.get("codret")).intValue() : -1;
      String message =
          result.get("msj") != null
              ? result.get("msj").toString()
              : "El procedimiento no devolvió ningún mensaje.";

      if (returnCode != 0) {
        throw new RuntimeException("Código " + returnCode + ": " + message);
      }

      return "Registro de cobro guardado correctamente.";

    } catch (SQLServerException exception) {
      throw new RuntimeException(
          "Error al construir los parametros tipo tabla: " + exception.getMessage(), exception);
    }
  }

  private SQLServerDataTable buildInvoicesTable(
      CollectionHeaderRequest header, List<CollectionDetailInvoicesRequest> invoices)
      throws SQLServerException {
    return buildInvoicesTable(header.getNumeroRecibo(), header.getRuc(), invoices);
  }

  private SQLServerDataTable buildInvoicesTable(
      CollectionCheckHeaderRequest header, List<CollectionDetailInvoicesRequest> invoices)
      throws SQLServerException {
    return buildInvoicesTable(header.getNumeroRecibo(), header.getRuc(), invoices);
  }

  private SQLServerDataTable buildInvoicesTable(
      String numeroRecibo, String ruc, List<CollectionDetailInvoicesRequest> invoices)
      throws SQLServerException {

    SQLServerDataTable table = new SQLServerDataTable();

    table.setTvpName("dbo.FacturasCobro");

    /*
     * El orden debe coincidir exactamente con el tipo
     * dbo.FacturasCobro de SQL Server.
     */
    table.addColumnMetadata("recibo", Types.VARCHAR);
    table.addColumnMetadata("idcliente", Types.VARCHAR);
    table.addColumnMetadata("factura", Types.VARCHAR);
    table.addColumnMetadata("monto", Types.NUMERIC);
    table.addColumnMetadata("observacion", Types.VARCHAR);

    if (invoices == null) {
      return table;
    }

    for (CollectionDetailInvoicesRequest invoice : invoices) {
      table.addRow(
          defaultIfBlank(invoice.getRecibo(), numeroRecibo),
          defaultIfBlank(invoice.getIdcliente(), ruc),
          emptyIfNull(invoice.getFactura()),
          toBigDecimalOrZero(invoice.getMonto()),
          emptyIfNull(invoice.getObservacion()));
    }

    return table;
  }

  private SQLServerDataTable buildChecksTable(
      CollectionCheckHeaderRequest header, List<CollectionCheckRequest> checks)
      throws SQLServerException {

    SQLServerDataTable table = buildEmptyChecksTable();

    if (checks == null) {
      return table;
    }

    for (CollectionCheckRequest check : checks) {
      table.addRow(
          defaultIfBlank(check.getRecibo(), header.getNumeroRecibo()),
          defaultIfBlank(check.getIdcliente(), header.getRuc()),
          emptyIfNull(check.getBancocliente()),
          emptyIfNull(check.getCuenta()),
          toTimestamp(check.getVencimiento()),
          emptyIfNull(check.getCheque()),
          toBigDecimalOrZero(check.getMonto()),
          emptyIfNull(check.getOrigen()),
          emptyIfNull(check.getObservacion()));
    }

    return table;
  }

  private SQLServerDataTable buildEmptyChecksTable() throws SQLServerException {

    SQLServerDataTable table = new SQLServerDataTable();

    table.setTvpName("dbo.ChequesCobro");

    /*
     * El orden debe coincidir exactamente con el tipo
     * dbo.ChequesCobro de SQL Server.
     */
    table.addColumnMetadata("recibo", Types.VARCHAR);
    table.addColumnMetadata("idcliente", Types.VARCHAR);
    table.addColumnMetadata("bancocliente", Types.VARCHAR);
    table.addColumnMetadata("cuenta", Types.VARCHAR);
    table.addColumnMetadata("vencimiento", Types.TIMESTAMP);
    table.addColumnMetadata("cheque", Types.VARCHAR);
    table.addColumnMetadata("monto", Types.NUMERIC);
    table.addColumnMetadata("origen", Types.VARCHAR);
    table.addColumnMetadata("observacion", Types.VARCHAR);

    // Para efectivo se envía la estructura sin filas.
    return table;
  }

  private String getDataAccessDetail(DataAccessException exception) {
    Throwable cause = exception.getMostSpecificCause();
    if (cause == null || cause.getMessage() == null || cause.getMessage().isBlank()) {
      return exception.getMessage();
    }
    return cause.getMessage();
  }
}
