package tuval.api.web.models.dao.dynamics.articulos;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dto.articulos.TravapItemOriginDto;
import tuval.api.web.models.dto.articulos.TravapItemSelectionDto;
import tuval.api.web.models.dto.articulos.TravapItemSelectionResponse;
import tuval.api.web.models.request.bodegas.articulos.TravapItemSelectionRequest;

@Repository
public class ArticleDynamicsDao {
  private final SimpleJdbcCall spSelTravapItem;

  public ArticleDynamicsDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.spSelTravapItem =
        new SimpleJdbcCall(jdbcTemplate)
            .withSchemaName("dbo")
            .withProcedureName("sp_sel_travap_item")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("nd_pretrab_psig", Types.DECIMAL),
                new SqlParameter("nd_preret_psig", Types.DECIMAL),
                new SqlParameter("nd_carga_kg", Types.DECIMAL),
                new SqlParameter("nd_facseg", Types.DECIMAL))
            .returningResultSet(
                "selectedItems", BeanPropertyRowMapper.newInstance(TravapItemSelectionDto.class))
            .returningResultSet(
                "originItems", BeanPropertyRowMapper.newInstance(TravapItemOriginDto.class));
  }

  @SuppressWarnings("unchecked")
  public TravapItemSelectionResponse getTravapItemSelection(TravapItemSelectionRequest request) {
    try {
      MapSqlParameterSource in =
          new MapSqlParameterSource()
              .addValue("nd_pretrab_psig", request.getWorkingPressurePsig())
              .addValue("nd_preret_psig", request.getReturnPressurePsig())
              .addValue("nd_carga_kg", request.getEstimatedLoadKg())
              .addValue("nd_facseg", request.getSafetyFactor());

      Map<String, Object> out = spSelTravapItem.execute(in);


      List<TravapItemSelectionDto> selectedItems =
          (List<TravapItemSelectionDto>) out.get("selectedItems");

      List<TravapItemOriginDto> originItems = (List<TravapItemOriginDto>) out.get("originItems");

      TravapItemSelectionDto selectedItem =
          selectedItems != null && !selectedItems.isEmpty() ? selectedItems.get(0) : null;

      return new TravapItemSelectionResponse(selectedItem, originItems);

    } catch (Exception e) {
      throw new RuntimeException(
          "Fallo técnico en la base de datos al seleccionar el artículo TRAVAP: " + e.getMessage(),
          e);
    }
  }
}
