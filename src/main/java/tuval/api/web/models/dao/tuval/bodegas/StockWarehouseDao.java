package tuval.api.web.models.dao.tuval.bodegas;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseEntity;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseResponseEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderEntity;

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

    public StockWarehouseResponseEntity consultStockWarehouseList(String articulo, String bodega) {
        try {

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("SP_TUV_OBTENER_STOCK_BODEGA")
                    .declareParameters(
                            // Parámetros de entrada
                            new SqlParameter("@ARTICULO", Types.VARCHAR),
                            new SqlParameter("@BODEGA", Types.VARCHAR)

                    ).returningResultSet("resultados",
                            new BeanPropertyRowMapper<>(StockWarehouseEntity.class));

            Map<String, Object> params = new HashMap<>();
            params.put("ARTICULO", articulo != null ? articulo.trim() : "");
            params.put("BODEGA", bodega != null ? bodega.trim() : "");
            Map<String, Object> result = jdbcCall.execute(params);

            StockWarehouseResponseEntity response = new StockWarehouseResponseEntity();
            @SuppressWarnings("unchecked")
            List<StockWarehouseEntity> stockList = (List<StockWarehouseEntity>) result.get("resultados");
            if (stockList == null) {
                stockList = List.of(); // Inicializa como lista vacía si es nulo
            }
            response.setStockWarehouseList(stockList);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar transferencias: " + e.getMessage(), e);
        }
    }

}
