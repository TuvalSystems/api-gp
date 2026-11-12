package tuval.api.web.reportes.ventas.sucursales.dao.dynamics;

import java.sql.Date;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.reportes.ventas.sucursales.dto.VentasComparativasDto;
import tuval.api.web.reportes.ventas.sucursales.request.VentasComparativasRequest;

@Repository
public class SucursalesDao {
  public static final int MAX_FILAS = 500;
  private final SimpleJdbcCall spVen11;

  public SucursalesDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    JdbcTemplate dynamicsJdbcTemplate =
        Objects.requireNonNull(jdbcTemplate, "El bean dynamicsJdbcTemplate no puede ser null");
    JdbcTemplate reporteJdbcTemplate =
        new JdbcTemplate(Objects.requireNonNull(dynamicsJdbcTemplate.getDataSource()));
    reporteJdbcTemplate.setQueryTimeout(30);
    reporteJdbcTemplate.setFetchSize(100);
    // Sin recorte JDBC: el mapper debe detectar y rechazar la fila 501.
    reporteJdbcTemplate.setMaxRows(0);

    this.spVen11 =
        new SimpleJdbcCall(reporteJdbcTemplate)
            .withSchemaName("dbo")
            .withProcedureName("spVen11")
            .withReturnValue()
            .withNamedBinding()
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlOutParameter("RETURN_VALUE", Types.INTEGER),
                new SqlParameter("@FechaIni", Types.DATE),
                new SqlParameter("@FechaFin", Types.DATE),
                new SqlParameter("@Sucursales", Types.VARCHAR))
            .returningResultSet("data", ventasComparativasMapper());
  }

  @SuppressWarnings("unchecked")
  public List<VentasComparativasDto> consultarComparativo(VentasComparativasRequest request) {
    // @isRelacionada se omite: se conserva el predeterminado del SP instalado.
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("@FechaIni", Date.valueOf(request.fechaIni()), Types.DATE)
        .addValue("@FechaFin", Date.valueOf(request.fechaFin()), Types.DATE)
        .addValue("@Sucursales", request.sucursales(), Types.VARCHAR);
    Map<String, Object> result = spVen11.execute(params);
    Number returnStatus = (Number) result.get("RETURN_VALUE");
    if (returnStatus == null || returnStatus.intValue() != 0 || result.get("data") == null) {
      throw new DataRetrievalFailureException("spVen11 no devolvió un reporte válido");
    }
    if (result.keySet().stream().anyMatch(key -> key.startsWith("#result-set-"))) {
      throw new DataRetrievalFailureException("Conjunto de resultados inesperado en spVen11");
    }
    return (List<VentasComparativasDto>) result.get("data");
  }

  private static RowMapper<VentasComparativasDto> ventasComparativasMapper() {
    return (rows, rowNum) -> {
      if (rowNum >= MAX_FILAS) {
        throw new IncorrectResultSizeDataAccessException(
            "El reporte supera el máximo de 500 filas", MAX_FILAS, MAX_FILAS + 1);
      }
      return new VentasComparativasDto(
          rows.getString("tx_nom"),
          rows.getBigDecimal("subTotalDiaHoy"), rows.getBigDecimal("extCostDiaHoy"),
          rows.getBigDecimal("subTotalAyer"), rows.getBigDecimal("extCostAyer"),
          rows.getBigDecimal("subTotalMes"), rows.getBigDecimal("extCostMes"),
          rows.getBigDecimal("subtotalMesAnterior"), rows.getBigDecimal("extCostMesAnterior"),
          rows.getBigDecimal("subtotalMesAnterior2"), rows.getBigDecimal("extCostMesAnterior2"),
          rows.getBigDecimal("subtotalAnioActual"), rows.getBigDecimal("extCostAnioActual"),
          rows.getBigDecimal("subtotalAnioAtras"), rows.getBigDecimal("extCostAnioAtras"));
    };
  }
}
