package tuval.api.web.models.dao.dynamics.bodegas;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dto.cliente.SegmentsAbcDto;

@Repository
public class WarehouseDynamicsDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public WarehouseDynamicsDao(
      @Qualifier("dynamicsNamedParameterJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<SegmentsAbcDto> findAllSegmentsAbcDao(List<String> items) {
    String SQL =
        """
            SELECT
            TRIM(tx_itm) as item,
            TRIM("Valor Venta") as valorVenta,
            TRIM(Peso) as peso,
            TRIM(Frecuencia) as frecuencia,
            TRIM(Tuval) as tuval,
            TRIM(Clase) as clase
        FROM dbo.vw_itemsABC_Columnas WHERE tx_itm IN (:listaItems)
        """;

    try {
      SqlParameterSource parameters = new MapSqlParameterSource("listaItems", items);
      var mapper = new BeanPropertyRowMapper<>(SegmentsAbcDto.class);
      return jdbcTemplate.query(SQL, parameters, mapper);
    } catch (Exception e) {
      throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
    }
  }
}
