package tuval.api.web.ventas.ordenCompra.dao.dynamics;

import java.sql.Types;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.ventas.ordenCompra.dto.CreatePurchaseOrderResponse;
import tuval.api.web.ventas.ordenCompra.request.CreatePurchaseOrderRequest;

@Repository
public class PurchaseOrderDao {
  private final SimpleJdbcCall spInsertarOrdenCompraDirectaGP;

  public PurchaseOrderDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    JdbcTemplate dynamicsJdbcTemplate =
        Objects.requireNonNull(jdbcTemplate, "El bean dynamicsJdbcTemplate no puede ser null");

    this.spInsertarOrdenCompraDirectaGP =
        new SimpleJdbcCall(dynamicsJdbcTemplate)
            .withCatalogName("Dynamics2021")
            .withSchemaName("dbo")
            .withProcedureName("sp_InsertarOrdenCompraDirectaGP")
            .withReturnValue()
            .withNamedBinding()
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlOutParameter("RETURN_VALUE", Types.INTEGER),
                new SqlParameter("@proveedor", Types.VARCHAR),
                new SqlParameter("@usuario", Types.VARCHAR),
                new SqlParameter("@tipo_ordDes", Types.INTEGER),
                new SqlParameter("@valorOrdenCompra", Types.DECIMAL),
                new SqlParameter("@bodega", Types.VARCHAR),
                new SqlParameter("@tx_numgui", Types.NVARCHAR),
                new SqlOutParameter("@ordenCompra", Types.VARCHAR));
  }

  public CreatePurchaseOrderResponse create(CreatePurchaseOrderRequest request) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("@proveedor", request.proveedor())
            .addValue("@usuario", request.usuario())
            .addValue("@tipo_ordDes", request.tipoDespachoId())
            .addValue("@valorOrdenCompra", request.valorOrdenCompra())
            .addValue("@bodega", request.bodega())
            .addValue("@tx_numgui", request.numGuia());
    Map<String, Object> result = spInsertarOrdenCompraDirectaGP.execute(params);

    Number returnCode = (Number) result.get("RETURN_VALUE");
    return new CreatePurchaseOrderResponse(
        returnCode == null ? null : returnCode.intValue(), (String) result.get("@ordenCompra"));
  }
}
