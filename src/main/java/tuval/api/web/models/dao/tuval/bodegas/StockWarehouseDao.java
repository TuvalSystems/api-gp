package tuval.api.web.models.dao.tuval.bodegas;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dto.bodegas.StockWarehouseParamsRequest;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseEntity;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseResponseEntity;

@Repository
public class StockWarehouseDao {

  @PostConstruct
  public void init() {
    validateConfiguration();
  }

  @Autowired
  @Qualifier("tuvalJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  public void validateConfiguration() {
    if (jdbcTemplate == null) {
      throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
    }
  }

  public StockWarehouseEntity executeSpConsultStockWarehouse(
      StockWarehouseParamsRequest request, String type) {
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("SP_TUV_OBTENER_STOCK_BODEGA")
              .declareParameters(
                  new SqlParameter("ARTICULO", Types.VARCHAR),
                  new SqlParameter("BODEGA", Types.VARCHAR))
              .returningResultSet(
                  "resultados", new BeanPropertyRowMapper<>(StockWarehouseEntity.class));
      Map<String, Object> params = new HashMap<>();
      params.put("ARTICULO", request.getArticulo() != null ? request.getArticulo().trim() : "");
      params.put("BODEGA", request.getBodega() != null ? request.getBodega().trim() : "");
      Map<String, Object> result = jdbcCall.execute(params);
      @SuppressWarnings("unchecked")
      List<StockWarehouseEntity> stockList = (List<StockWarehouseEntity>) result.get("resultados");
      if (stockList == null || stockList.isEmpty()) {
        throw new RuntimeException(
            "No se encontraron datos para el artículo y bodega especificados.");
      }
      return stockList.get(0);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error al ejecutar SP_TUV_OBTENER_STOCK_BODEGA -%s- (articulo=%s, bodega=%s): %s"
              .formatted(
                  type,
                  String.valueOf(request.getArticulo()),
                  String.valueOf(request.getBodega()),
                  e.getMessage()),
          e);
    }
  }

  public StockWarehouseEntity executeSpConsultStockByWarehouseAndCompartment(
      String articulo, String bodega, String compartimiento) {

    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(jdbcTemplate)
              .withProcedureName("SP_TUV_OBTENER_BODEGA_COMPARTIMENTO")
              .declareParameters(
                  // Parámetros de entrada
                  new SqlParameter("@ARTICULO", Types.VARCHAR),
                  new SqlParameter("@BODEGA", Types.VARCHAR),
                  new SqlParameter("@COMPARTIMIENTO", Types.VARCHAR))
              .returningResultSet(
                  "resultados", new BeanPropertyRowMapper<>(StockWarehouseEntity.class));
      Map<String, Object> params = new HashMap<>();
      params.put("ARTICULO", articulo != null ? articulo.trim() : "");
      params.put("BODEGA", bodega != null ? bodega.trim() : "");
      params.put("COMPARTIMIENTO", compartimiento != null ? compartimiento.trim() : "");
      Map<String, Object> result = jdbcCall.execute(params);

      @SuppressWarnings("unchecked")
      List<StockWarehouseEntity> stockList = (List<StockWarehouseEntity>) result.get("resultados");
    if (stockList == null || stockList.isEmpty()) {
        throw new RuntimeException(
            "No se encontraron datos para el artículo y bodega especificados.");
      }
      return stockList.get(0);
    } catch (Exception e) {
      throw new RuntimeException("Error al consultar stock compartimento: " + e.getMessage(), e);
    }
  }
}
