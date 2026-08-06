package tuval.api.web.models.dao.tuval.cobros;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionCashInvoiceRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionDetailInvoicesRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionHeaderRequest;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerException;


@Repository
public class CollectionRegisterDao {
  private static final ZoneId ECUADOR_ZONE = ZoneId.of("America/Guayaquil");

  private final SimpleJdbcCall spRegisterCollection;

  public CollectionRegisterDao(@Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate) {
    jdbcTemplate.setResultsMapCaseInsensitive(true);

    this.spRegisterCollection =
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
  }

  public String registerCashCollection(CollectionCashInvoiceRequest request, Integer modo) {
    try {
      CollectionHeaderRequest header = request.getCabecera();

      SQLServerDataTable checksTable = buildEmptyChecksTable();

      SQLServerDataTable invoicesTable = buildInvoicesTable(header, request.getDetalle());
      MapSqlParameterSource in =
          new MapSqlParameterSource()
              // Cabecera del cobro
              .addValue("numrecibo", empty(header.getNumeroRecibo()))
              .addValue("idcliente", empty(header.getRuc()))
              .addValue("fechacobro", toTimestamp(header.getFechaCobro()), Types.TIMESTAMP)
              .addValue("tipo", empty(header.getTipoCobro()))
              .addValue("observacion", empty(header.getObservacion()))
              .addValue("soldscto", empty(header.getSolicitudDescuento()))
              .addValue("idvendedor", empty(header.getIdVendedor()))
              .addValue("monto", zeroIfNull(header.getMontoCobro()), Types.NUMERIC)
              .addValue("saldopend", zeroIfNull(header.getSaldoPendiente()), Types.NUMERIC)
              .addValue("bancomax", empty(header.getBancoTuval()))
              .addValue("fecdeptrf", toTimestamp(header.getFechaDeposito()), Types.TIMESTAMP)
              .addValue("papeleta", empty(header.getNumeroPapeleta()))

              // Retenciones
              .addValue("idretencion", empty(header.getNumeroRetencion()))
              .addValue("porcenret", zeroIfNull(header.getPorcentajeRetencion()), Types.NUMERIC)

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
              .addValue("numautor", empty(header.getNumeroAutorizacion()))

              // Table-Valued Parameters
              .addValue("chequescobro", checksTable, SqlTypeValue.TYPE_UNKNOWN)
              .addValue("facturascobro", invoicesTable, SqlTypeValue.TYPE_UNKNOWN)

              // Control
              .addValue("userid", "wsrvmc")
              .addValue("modo", modo, Types.SMALLINT)
              .addValue("submodo", 0, Types.SMALLINT)
              .addValue("equipo", "wserver");

      Map<String, Object> result = spRegisterCollection.execute(in);
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
            "Error al construir los parámetros tipo tabla: "
                + exception.getMessage(),
            exception
        );
    }
  }

 private SQLServerDataTable buildInvoicesTable(
    CollectionHeaderRequest header,
    List<CollectionDetailInvoicesRequest> invoices
) throws SQLServerException {

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
            defaultIfBlank(invoice.getRecibo(), header.getNumeroRecibo()),
            defaultIfBlank(invoice.getIdcliente(), header.getRuc()),
            empty(invoice.getFactura()),
            zeroIfNull(invoice.getMonto()),
            empty(invoice.getObservacion())
        );
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

  private String empty(String value) {
    return value == null ? "" : value.trim();
  }

  private String defaultIfBlank(String value, String defaultValue) {
    return value == null || value.trim().isEmpty() ? empty(defaultValue) : value.trim();
  }

  private BigDecimal zeroIfNull(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private Timestamp toTimestamp(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }

    String text = value.trim();

    try {
      LocalDateTime localDateTime =
          OffsetDateTime.parse(text).atZoneSameInstant(ECUADOR_ZONE).toLocalDateTime();
      return Timestamp.valueOf(localDateTime);
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
      return Timestamp.valueOf(
          LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    } catch (DateTimeParseException ignored) {
    }

    throw new IllegalArgumentException("Formato de fecha invalido para registrar cobro: " + value);
  }
}
