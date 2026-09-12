package tuval.api.web.ventas.sincronizacion.dao.dynamics;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import tuval.api.web.ventas.cotizaciones.request.QuoteHeaderRequest;

import java.util.List;

@Repository
public class GeneralDynamicsDao {
  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public GeneralDynamicsDao(
       @Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate,
      // Agregamos el Qualifier para asegurar que usa la conexión correcta
      @Qualifier("dynamicsNamedParameterJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }



  
  public int[] updateMassiveQuoteHeaders(List<QuoteHeaderRequest> headers) {
    String sql =
        """
        UPDATE tbm_cabcotrec
        SET tx_est = ?
        WHERE tx_cot = ?
        """;
    return jdbcTemplate.batchUpdate(
        sql,
        new BatchPreparedStatementSetter() {
          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            QuoteHeaderRequest header = headers.get(i);
            ps.setString(1, header.getEstado());
            ps.setString(2, header.getCotizacion());
          }

          @Override
          public int getBatchSize() {
            return headers.size();
          }
        });
  }
}
