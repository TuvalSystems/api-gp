package tuval.api.web.bodega.stock.dao.dynamics;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import tuval.api.web.bodega.articulos.dto.ItemExpoDto;
import tuval.api.web.clientes.dto.SegmentsAbcDto;
import tuval.api.web.bodega.articulos.request.ItemsExpoRequest;

@Repository
public class WarehouseDynamicsDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public WarehouseDynamicsDao(
      @Qualifier("dynamicsNamedParameterJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<SegmentsAbcDto> findAllSegmentsAbcDao(List<String> items) {
    String SQL = """
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

  public ItemExpoDto findItemExpoByPtoVta(ItemsExpoRequest req) {

    String SQL = """
        SELECT TOP (1)
          TRIM(ITEMNMBR) AS item,
          TOTAL as cantidad,
          trim(tx_codSer) as puntoVenta,
          trim(BIN) as bodega
          FROM dbo.vw_ConsultaItemsExpo
          WHERE ITEMNMBR = :item
          AND tx_codSer = :puntoVenta
          ORDER BY ITEMNMBR
        """;
    try {
      // Preparamos los parámetros mapeándolos por nombre
      MapSqlParameterSource params = new MapSqlParameterSource()
          .addValue("item", req.getItem())
          .addValue("puntoVenta", req.getPuntoVenta());

      var mapper = new BeanPropertyRowMapper<>(ItemExpoDto.class);

      List<ItemExpoDto> results = jdbcTemplate.query(SQL, params, mapper);

      return results.isEmpty() ? null : results.get(0);

    } catch (org.springframework.dao.DataAccessException e) {
      throw new IllegalStateException("Ocurrió un error al consultar la base de datos para el item: " + req.getItem(), e);
    }
  }

}
