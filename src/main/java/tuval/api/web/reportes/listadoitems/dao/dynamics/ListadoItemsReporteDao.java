package tuval.api.web.reportes.listadoitems.dao.dynamics;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.reportes.listadoitems.dto.ListadoItemsReporteDto;
import tuval.api.web.reportes.listadoitems.request.ListadoItemsReporteRequest;

@Repository
public class ListadoItemsReporteDao {
  private final NamedParameterJdbcTemplate jdbcTemplate;

  private static final String SELECT = """
      SELECT tx_itm, codZafiro, cod3L, Descripcion, unidadMedida, tx_atrMar, tx_atrMat
      FROM dbo.vw_listadoItems
      WHERE 1 = 1
      """;
  // No key has been verified: order by every returned value, including binary
  // tie-breaks and lengths for case/accent/trailing-space variants.
  private static final String ORDER = """
       ORDER BY tx_itm COLLATE Latin1_General_100_BIN2,
       codZafiro COLLATE Latin1_General_100_BIN2,
       cod3L COLLATE Latin1_General_100_BIN2,
       Descripcion COLLATE Latin1_General_100_BIN2,
       unidadMedida COLLATE Latin1_General_100_BIN2,
       tx_atrMar COLLATE Latin1_General_100_BIN2,
       tx_atrMat COLLATE Latin1_General_100_BIN2,
       DATALENGTH(tx_itm), DATALENGTH(codZafiro), DATALENGTH(cod3L),
       DATALENGTH(Descripcion), DATALENGTH(unidadMedida),
       DATALENGTH(tx_atrMar), DATALENGTH(tx_atrMat)
       OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
      """;

  public ListadoItemsReporteDao(
      @Qualifier("dynamicsNamedParameterJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<ListadoItemsReporteDto> consultar(ListadoItemsReporteRequest request) {
    var sql = new StringBuilder(SELECT);
    var parameters = new MapSqlParameterSource();
    if (request.getCodigo() != null) {
      sql.append(" AND (tx_itm = :codigo OR codZafiro = :codigo OR cod3L = :codigo)");
      parameters.addValue("codigo", request.getCodigo(), Types.VARCHAR);
    }
    if (request.getDescripcion() != null) {
      sql.append(" AND Descripcion LIKE :descripcion ESCAPE '~'");
      parameters.addValue("descripcion", "%" + escapeLike(request.getDescripcion()) + "%", Types.VARCHAR);
    }
    if (request.getMarca() != null) {
      sql.append(" AND tx_atrMar = :marca");
      parameters.addValue("marca", request.getMarca(), Types.VARCHAR);
    }
    if (request.getMaterial() != null) {
      sql.append(" AND tx_atrMat = :material");
      parameters.addValue("material", request.getMaterial(), Types.VARCHAR);
    }
    sql.append(ORDER);
    parameters.addValue("offset", (long) request.getPage() * request.getSize(), Types.BIGINT);
    parameters.addValue("size", request.getSize(), Types.INTEGER);

    // Timeout and row limits belong to this statement, not to the shared template.
    return jdbcTemplate.execute(sql.toString(), parameters, statement -> {
      statement.setQueryTimeout(10);
      statement.setFetchSize(request.getSize());
      statement.setMaxRows(request.getSize());
      try (ResultSet rows = statement.executeQuery()) {
        List<ListadoItemsReporteDto> result = new ArrayList<>();
        while (rows.next()) {
          result.add(new ListadoItemsReporteDto(
              rows.getString("tx_itm"), rows.getString("codZafiro"), rows.getString("cod3L"),
              rows.getString("Descripcion"), rows.getString("unidadMedida"),
              rows.getString("tx_atrMar"), rows.getString("tx_atrMat")));
        }
        return result;
      }
    });
  }

  private static String escapeLike(String value) {
    return value.replace("~", "~~").replace("%", "~%")
        .replace("_", "~_").replace("[", "~[");
  }
}
