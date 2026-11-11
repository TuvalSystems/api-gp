package tuval.api.web.reportes.inventario.varianza.dao.dynamics;

import java.sql.Date;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.reportes.inventario.varianza.dto.VarianzaDto;
import tuval.api.web.reportes.inventario.varianza.request.VarianzaRequest;

@Repository
public class VarianzaDao {
  private final SimpleJdbcCall spInv60;

  public VarianzaDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    JdbcTemplate dynamicsJdbcTemplate =
        Objects.requireNonNull(jdbcTemplate, "El bean dynamicsJdbcTemplate no puede ser null");

    // Configuración local del reporte, sin alterar el template compartido.
    JdbcTemplate reporteJdbcTemplate =
        new JdbcTemplate(Objects.requireNonNull(dynamicsJdbcTemplate.getDataSource()));
    reporteJdbcTemplate.setQueryTimeout(30);
    reporteJdbcTemplate.setFetchSize(100);
    reporteJdbcTemplate.setMaxRows(0);

    this.spInv60 =
        new SimpleJdbcCall(reporteJdbcTemplate)
            .withSchemaName("dbo")
            .withProcedureName("spInv60")
            .withReturnValue()
            .withNamedBinding()
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlOutParameter("RETURN_VALUE", Types.INTEGER),
                new SqlParameter("NumeroDocumento", Types.VARCHAR))
            .returningResultSet("data", varianzaMapper());
  }

  @SuppressWarnings("unchecked")
  public List<VarianzaDto> consultar(VarianzaRequest request) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("NumeroDocumento", request.numeroDocumento(), Types.VARCHAR);
    Map<String, Object> result = spInv60.execute(params);

    Number returnStatus = (Number) result.get("RETURN_VALUE");
    if (returnStatus == null || returnStatus.intValue() != 0 || result.get("data") == null) {
      throw new DataRetrievalFailureException("spInv60 no devolvio un reporte valido");
    }
    if (result.keySet().stream().anyMatch(key -> key.startsWith("#result-set-"))) {
      throw new DataRetrievalFailureException("Conjunto de resultados inesperado en spInv60");
    }

    return (List<VarianzaDto>) result.get("data");
  }

  private static RowMapper<VarianzaDto> varianzaMapper() {
    return (rows, rowNum) -> {
    Date date = rows.getDate("DOCDATE");
    return new VarianzaDto(
        rows.getObject("IVDOCTYP", Integer.class),
        rows.getString("IVDOCNBR"),
        date == null ? null : date.toLocalDate(),
        rows.getString("BACHNUMB"),
        rows.getString("ITEMNMBR"),
        rows.getString("ITMGEDSC"),
        rows.getString("ITMSHNAM"),
        rows.getString("ITEMDESC"),
        rows.getBigDecimal("LNSEQNBR"),
        rows.getBigDecimal("QTYBSUOM"),
        rows.getString("TRXLOCTN"),
        rows.getBigDecimal("TRXQTY"),
        rows.getBigDecimal("UNITCOST"),
        rows.getBigDecimal("EXTDCOST"),
        rows.getString("Reason_Code"),
        rows.getBigDecimal("CURRCOST"),
        rows.getBigDecimal("EXISTENCIA"));
    };
  }
}
