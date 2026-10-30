package tuval.api.web.reportes.ven11.dao.dynamics;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import tuval.api.web.reportes.ven11.dto.Ven11ReporteDto;
import tuval.api.web.reportes.ven11.request.Ven11ReporteRequest;

@Repository
public class Ven11ReporteDao {
  public static final int MAX_FILAS = 500;
  private static final String CALL =
      "{? = call dbo.spVen11(@FechaIni = ?, @FechaFin = ?, @Sucursales = ?)}";
  private final JdbcTemplate jdbcTemplate;

  public Ven11ReporteDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Ven11ReporteDto> consultar(Ven11ReporteRequest request) {
    return jdbcTemplate.execute((ConnectionCallback<List<Ven11ReporteDto>>) connection -> {
      // Omit @isRelacionada entirely so the deployed procedure supplies its default.
      try (CallableStatement statement = connection.prepareCall(CALL)) {
        statement.registerOutParameter(1, Types.INTEGER); // SQL Server procedure return status.
        statement.setObject(2, Date.valueOf(request.fechaIni()), Types.DATE);
        statement.setObject(3, Date.valueOf(request.fechaFin()), Types.DATE);
        statement.setObject(4, request.sucursales(), Types.VARCHAR);
        statement.setQueryTimeout(30);
        statement.setFetchSize(100);

        List<Ven11ReporteDto> result = null;
        boolean hasResultSet = statement.execute();
        while (true) {
          if (hasResultSet) {
            if (result != null) {
              throw new DataRetrievalFailureException("Conjunto de resultados inesperado en spVen11");
            }
            try (ResultSet rows = statement.getResultSet()) {
              if (rows == null) {
                throw new DataRetrievalFailureException("Resultado ausente en spVen11");
              }
              result = readRows(rows);
            }
          } else if (statement.getUpdateCount() == -1) {
            break;
          }
          // SQL Server can emit update counts even for a SELECT-only procedure without NOCOUNT.
          hasResultSet = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
        }
        int returnStatus = statement.getInt(1);
        if (statement.wasNull() || returnStatus != 0 || result == null) {
          throw new DataRetrievalFailureException("spVen11 no devolvió un reporte válido");
        }
        return result;
      }
    });
  }

  private static List<Ven11ReporteDto> readRows(ResultSet rows) throws SQLException {
    List<Ven11ReporteDto> result = new ArrayList<>();
    while (rows.next()) {
      if (result.size() == MAX_FILAS) {
        // Detect the 501st row; never return a truncated successful response.
        throw new IncorrectResultSizeDataAccessException(
            "El reporte supera el máximo de 500 filas", MAX_FILAS, MAX_FILAS + 1);
      }
      result.add(ROW_MAPPER.mapRow(rows, result.size()));
    }
    return result;
  }

  private static final RowMapper<Ven11ReporteDto> ROW_MAPPER = (rows, rowNum) ->
      new Ven11ReporteDto(
          rows.getString("tx_nom"),
          rows.getBigDecimal("subTotalDiaHoy"), rows.getBigDecimal("extCostDiaHoy"),
          rows.getBigDecimal("subTotalAyer"), rows.getBigDecimal("extCostAyer"),
          rows.getBigDecimal("subTotalMes"), rows.getBigDecimal("extCostMes"),
          rows.getBigDecimal("subtotalMesAnterior"), rows.getBigDecimal("extCostMesAnterior"),
          rows.getBigDecimal("subtotalMesAnterior2"), rows.getBigDecimal("extCostMesAnterior2"),
          rows.getBigDecimal("subtotalAnioActual"), rows.getBigDecimal("extCostAnioActual"),
          rows.getBigDecimal("subtotalAnioAtras"), rows.getBigDecimal("extCostAnioAtras"));
}
