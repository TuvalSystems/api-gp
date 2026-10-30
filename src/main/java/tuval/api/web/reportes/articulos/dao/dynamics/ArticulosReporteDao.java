package tuval.api.web.reportes.articulos.dao.dynamics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.reportes.articulos.dto.ArticulosReporteDto;
import tuval.api.web.reportes.articulos.request.ArticulosReporteRequest;

@Repository
public class ArticulosReporteDao {
  private final NamedParameterJdbcTemplate jdbcTemplate;

  private static final String SELECT = """
      SELECT tx_itm, codZafiro, cod3L, Descripcion, unidadMedida, claseItem,
             precio, peso, costo, transito, cabGrupoVta, detGrupoVta, cabGrupoCom,
             detGrupoComp, promVtaMenAct, promVtaMenTeo, promVntaMenAnt, moq, fre,
             stokbase, exc, itemCabecera, categoriaCompra, lineaCompra, claseCompra,
             Marca, Material, compras, SubCompras, Control, SubControl,
             fechacreacion, catven, catpes, catfre, cattuv, marUti
      FROM dbo.vw_MaestroArticulo
      WHERE 1 = 1
      """;

  public ArticulosReporteDao(
      @Qualifier("dynamicsNamedParameterJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<ArticulosReporteDto> consultar(ArticulosReporteRequest request) {
    var sql = new StringBuilder(SELECT);
    var parameters = new MapSqlParameterSource();
    exact(sql, parameters, "tx_itm", request.getTx_itm());
    exact(sql, parameters, "codZafiro", request.getCodZafiro());
    exact(sql, parameters, "cod3L", request.getCod3L());
    if (request.getDescripcion() != null) {
      sql.append(" AND [Descripcion] LIKE :descripcion ESCAPE '~'");
      parameters.addValue("descripcion", "%" + escapeLike(request.getDescripcion()) + "%", Types.VARCHAR);
    }
    exact(sql, parameters, "claseItem", request.getClaseItem());
    exact(sql, parameters, "unidadMedida", request.getUnidadMedida());
    exact(sql, parameters, "categoriaCompra", request.getCategoriaCompra());
    exact(sql, parameters, "lineaCompra", request.getLineaCompra());
    exact(sql, parameters, "claseCompra", request.getClaseCompra());
    exact(sql, parameters, "Marca", request.getMarca());
    exact(sql, parameters, "Material", request.getMaterial());
    exact(sql, parameters, "itemCabecera", request.getItemCabecera());
    exact(sql, parameters, "catven", request.getCatven());
    exact(sql, parameters, "catpes", request.getCatpes());
    exact(sql, parameters, "catfre", request.getCatfre());
    exact(sql, parameters, "cattuv", request.getCattuv());

    // Both identifiers and directions come exclusively from closed enums.
    sql.append(" ORDER BY [").append(request.getSort().name()).append("] ")
        .append(request.getDirection().name());
    if (request.getSort() != ArticulosReporteRequest.Sort.tx_itm) {
      sql.append(", [tx_itm] ASC");
    }
    sql.append(" OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
    parameters.addValue("offset", (long) request.getPage() * request.getSize(), Types.BIGINT);
    parameters.addValue("size", request.getSize(), Types.INTEGER);

    // Statement-local settings: never mutate the shared template or its DataSource.
    return jdbcTemplate.execute(sql.toString(), parameters, statement -> {
      statement.setQueryTimeout(30);
      statement.setFetchSize(request.getSize());
      statement.setMaxRows(request.getSize());
      try (ResultSet rows = statement.executeQuery()) {
        List<ArticulosReporteDto> result = new ArrayList<>();
        while (rows.next()) {
          result.add(ROW_MAPPER.mapRow(rows, result.size()));
        }
        return result;
      }
    });
  }

  private static void exact(StringBuilder sql, MapSqlParameterSource parameters,
      String column, String value) {
    if (value != null) {
      sql.append(" AND [").append(column).append("] = :").append(column);
      parameters.addValue(column, value, Types.VARCHAR);
    }
  }

  private static String escapeLike(String value) {
    return value.replace("~", "~~").replace("%", "~%")
        .replace("_", "~_").replace("[", "~[");
  }

  private static java.time.LocalDateTime creationDate(ResultSet rows) throws SQLException {
    var timestamp = rows.getTimestamp("fechacreacion");
    return timestamp == null ? null : timestamp.toLocalDateTime();
  }

  private static final RowMapper<ArticulosReporteDto> ROW_MAPPER = (rows, rowNum) ->
      new ArticulosReporteDto(
          rows.getString("tx_itm"), rows.getString("codZafiro"), rows.getString("cod3L"),
          rows.getString("Descripcion"), rows.getString("unidadMedida"), rows.getString("claseItem"),
          rows.getBigDecimal("precio"), rows.getBigDecimal("peso"), rows.getBigDecimal("costo"),
          rows.getBigDecimal("transito"), rows.getObject("cabGrupoVta", Short.class),
          rows.getObject("detGrupoVta", Short.class), rows.getObject("cabGrupoCom", Short.class),
          rows.getObject("detGrupoComp", Short.class), rows.getBigDecimal("promVtaMenAct"),
          rows.getBigDecimal("promVtaMenTeo"), rows.getBigDecimal("promVntaMenAnt"),
          rows.getBigDecimal("moq"), rows.getBigDecimal("fre"), rows.getBigDecimal("stokbase"),
          rows.getBigDecimal("exc"), rows.getString("itemCabecera"), rows.getString("categoriaCompra"),
          rows.getString("lineaCompra"), rows.getString("claseCompra"), rows.getString("Marca"),
          rows.getString("Material"), rows.getString("compras"), rows.getString("SubCompras"),
          rows.getString("Control"), rows.getString("SubControl"), creationDate(rows),
          rows.getString("catven"), rows.getString("catpes"), rows.getString("catfre"),
          rows.getString("cattuv"), rows.getBigDecimal("marUti"));
}
