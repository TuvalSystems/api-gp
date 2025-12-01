package tuval.api.web.models.dao.tuval.bodegas;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.dto.bodegas.StockWarehouseParamsRequest;
import tuval.api.web.models.entity.tuval.bodegas.ListPriceItemResponseEntity;
import tuval.api.web.models.entity.tuval.bodegas.PriceItemEntity;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseEntity;

@Repository
public class StockWarehouseDao {

  @Autowired
  @Qualifier("tuvalJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  private SimpleJdbcCall spObtenerListaPrecio;

  @PostConstruct
  public void init() {
    validateConfiguration();

    /** sp de obtener lista de precios */
    this.spObtenerListaPrecio =
        new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("MSW_SP_OBTENER_LISTA_PRECIO")
            .withSchemaName("dbo")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(new SqlParameter("articulo", Types.VARCHAR));
  }

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

  public ListPriceItemResponseEntity executeSpConsultListPrices(String item) {

    MapSqlParameterSource in = new MapSqlParameterSource().addValue("articulo", item);
    try {
      Map<String, Object> out = spObtenerListaPrecio.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");

      List<PriceItemEntity> lista = new ArrayList<>();
      for(Map<String, Object> row :rs){
        PriceItemEntity e = new PriceItemEntity();
        e.setItem(CoerceHelperUtils.stringOrEmpty(row.get("ITEMNMBR")));
        e.setCurncyId(CoerceHelperUtils.stringOrEmpty(row.get("CURNCYID")));
        e.setUofm(CoerceHelperUtils.stringOrEmpty(row.get("UOFM")));
        e.setIdLista(CoerceHelperUtils.stringOrEmpty(row.get("ID_LISTA")));
        e.setValor(CoerceHelperUtils.toBigDecimal(row.get("VALOR")));
        lista.add(e);
      }
      ListPriceItemResponseEntity response = new ListPriceItemResponseEntity();
      response.setData(lista);
      return response;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
