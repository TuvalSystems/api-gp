package tuval.api.web.reportes.inventario.varianza.dao.dynamics;

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
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import tuval.api.web.reportes.inventario.varianza.dto.VarianzaDto;
import tuval.api.web.reportes.inventario.varianza.request.VarianzaRequest;

@Repository
public class VarianzaDao {
  private static final String CALL = "{? = call dbo.spInv60(@NumeroDocumento = ?)}";
  private final JdbcTemplate jdbcTemplate;

  public VarianzaDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<VarianzaDto> consultar(VarianzaRequest request) {
    return jdbcTemplate.execute((ConnectionCallback<List<VarianzaDto>>) connection -> {
      try (CallableStatement statement = connection.prepareCall(CALL)) {
        statement.registerOutParameter(1, Types.INTEGER); // SQL Server procedure return status.
        if (request.numeroDocumento() == null) {
          statement.setNull(2, Types.VARCHAR);
        } else {
          statement.setObject(2, request.numeroDocumento(), Types.VARCHAR);
        }
        statement.setQueryTimeout(30);
        statement.setFetchSize(100); // Fetch hint only; all returned rows are retained.
        statement.setMaxRows(0); // Clear any inherited template cap for this statement only.

        List<VarianzaDto> result = null;
        boolean hasResultSet = statement.execute();
        while (true) {
          if (hasResultSet) {
            if (result != null) {
              throw new DataRetrievalFailureException("Conjunto de resultados inesperado en spInv60");
            }
            try (ResultSet rows = statement.getResultSet()) {
              if (rows == null) {
                throw new DataRetrievalFailureException("Resultado ausente en spInv60");
              }
              result = readRows(rows);
            }
          } else if (statement.getUpdateCount() == -1) {
            break;
          }
          hasResultSet = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
        }
        int returnStatus = statement.getInt(1);
        if (statement.wasNull() || returnStatus != 0 || result == null) {
          throw new DataRetrievalFailureException("spInv60 no devolvio un reporte valido");
        }
        return result;
      }
    });
  }

  private static List<VarianzaDto> readRows(ResultSet rows) throws SQLException {
    List<VarianzaDto> result = new ArrayList<>();
    while (rows.next()) {
      result.add(ROW_MAPPER.mapRow(rows, result.size()));
    }
    return result;
  }

  private static final RowMapper<VarianzaDto> ROW_MAPPER = (rows, rowNum) -> {
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
