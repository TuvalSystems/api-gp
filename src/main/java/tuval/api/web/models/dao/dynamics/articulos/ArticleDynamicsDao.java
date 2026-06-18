package tuval.api.web.models.dao.dynamics.articulos;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.dto.articulos.ArticlePurchaseClassificationDto;
import tuval.api.web.models.dto.articulos.ArticleSegmentConfigurationDto;
import tuval.api.web.models.dto.articulos.TravapItemOriginDto;
import tuval.api.web.models.dto.articulos.TravapItemSelectionDto;
import tuval.api.web.models.dto.articulos.TravapItemSelectionResponse;
import tuval.api.web.models.request.bodegas.articulos.ArticleConfigSegmentRequest;
import tuval.api.web.models.request.bodegas.articulos.TravapItemSelectionRequest;

@Repository
public class ArticleDynamicsDao {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spSelTravapItem;
  private final SimpleJdbcCall spInv51;

  public ArticleDynamicsDao(@Qualifier("dynamicsJdbcTemplate") @NonNull JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate =
        Objects.requireNonNull(jdbcTemplate, "dynamicsJdbcTemplate no puede ser null");
    this.spSelTravapItem =
        new SimpleJdbcCall(this.jdbcTemplate)
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

    this.spInv51 =
        new SimpleJdbcCall(this.jdbcTemplate)
            .withSchemaName("dbo")
            .withProcedureName("spInv51")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(new SqlParameter("item", Types.VARCHAR))
            .returningResultSet("data", articlePurchaseClassificationMapper());
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

  public List<ArticleSegmentConfigurationDto> getArticlesSegmentConfiguration(
      ArticleConfigSegmentRequest req) {
    StringBuilder sqlText =
        new StringBuilder(
            """
                    SELECT
                    co_subseg AS codigoSubsegmento,
                    RTRIM(segmento_cliente) AS segmentoCliente,
                    RTRIM(co_claatritm) AS codigoClaseAtributoItem,
                    RTRIM(codCategoriaCompra) AS codigoCategoria,
                    RTRIM(codLineaCompra) AS codigoLinea,
                    RTRIM(codClaseCompra) AS codigoClase,
                    RTRIM(codMarca) AS codigoMarca,
                    co_nivpri AS codigoNivel,
                    nivel AS nivel,
                    RTRIM(descripcion_nivel) AS descripcionNivel
                FROM dbo.vw_segmencliatritm_config
                WHERE co_subseg = ?
            """);
    List<Object> args = new ArrayList<>();
    args.add(req.getCodigoSegmento());

    if (req.getCodigoCategoria() != null && !req.getCodigoCategoria().isBlank()) {
      sqlText.append(" AND codCategoriaCompra = ?");
      args.add(req.getCodigoCategoria());
    }

    if (req.getCodigoLinea() != null && !req.getCodigoLinea().isBlank()) {
      sqlText.append(" AND codLineaCompra = ?");
      args.add(req.getCodigoLinea());
    }

    sqlText.append(
        """
            ORDER BY
                codCategoriaCompra,
                codLineaCompra,
                codClaseCompra,
                codMarca
        """);

    try {
      var mapper = new BeanPropertyRowMapper<>(ArticleSegmentConfigurationDto.class);

      return jdbcTemplate.query(sqlText.toString(), mapper, args.toArray());
    } catch (DataAccessException e) {
      throw new IllegalStateException(
          "Error al consultar la configuración de artículos por segmento", e);
    }
  }

  @SuppressWarnings("unchecked")
  public ArticlePurchaseClassificationDto getArticlePurchaseClassification(String item) {
    if (item == null || item.isBlank()) {
      return null;
    }

    try {
      MapSqlParameterSource params = new MapSqlParameterSource().addValue("item", item.trim());

      Map<String, Object> result = spInv51.execute(params);

      List<ArticlePurchaseClassificationDto> rows =
          (List<ArticlePurchaseClassificationDto>) result.get("data");

      if (rows == null || rows.isEmpty()) {
        return null;
      }

      return rows.get(0);

    } catch (DataAccessException e) {
      throw new IllegalStateException(
          "Error al consultar la clasificación de compra del artículo: " + item, e);
    }
  }

  private RowMapper<ArticlePurchaseClassificationDto> articlePurchaseClassificationMapper() {
    return (rs, rowNum) ->
        new ArticlePurchaseClassificationDto(
            CoerceHelperUtils.stringOrEmpty(rs.getString("tx_itm")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("Marca")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("CategoriaCompra")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("LineaCompra")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("ClaseCompra")));
  }
}
