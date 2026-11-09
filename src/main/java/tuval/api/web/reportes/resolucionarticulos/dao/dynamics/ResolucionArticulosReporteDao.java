package tuval.api.web.reportes.resolucionarticulos.dao.dynamics;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.reportes.resolucionarticulos.dto.CatalogoArticuloDto;

@Repository
public class ResolucionArticulosReporteDao {
  public static final int LIMITE = 500;
  private final NamedParameterJdbcTemplate jdbcTemplate;

  // A closed retrieval vocabulary, including aliases understood by the matcher.
  public enum Familia {
    BUSHING(List.of("BUSHING")), NIPLE(List.of("NIPLE", "NEPLO")),
    CODO(List.of("CODO")), NUDO(List.of("NUDO", "UNION UNIVERSAL", "UNIÓN UNIVERSAL")),
    VALVULA_BOLA(List.of("VALVULA", "VÁLVULA", "LLAVE")), TEFLON(List.of("TEFLON", "TEFLÓN", "PTFE"));
    private final List<String> anclas;
    Familia(List<String> anclas) { this.anclas = anclas; }
  }

  public ResolucionArticulosReporteDao(
      @Qualifier("dynamicsNamedParameterJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<CatalogoArticuloDto> buscar(Familia familia, String codigo) {
    if ((familia == null) == (codigo == null) || (codigo != null &&
        (codigo.isBlank() || codigo.length() > 31))) {
      throw new IllegalArgumentException("Busqueda invalida");
    }
    var parameters = new MapSqlParameterSource().addValue("limite", LIMITE + 1, Types.INTEGER);
    var sql = new StringBuilder("""
        SELECT TOP (:limite) tx_itm, codZafiro, cod3L, Descripcion, unidadMedida,
               Marca, Material, categoriaCompra, lineaCompra, claseCompra
        FROM dbo.vw_MaestroArticulo WHERE (
        """);
    if (codigo != null) {
      sql.append("tx_itm = :codigo OR codZafiro = :codigo OR cod3L = :codigo");
      parameters.addValue("codigo", codigo, Types.VARCHAR);
    } else {
      for (int i = 0; i < familia.anclas.size(); i++) {
        if (i > 0) sql.append(" OR ");
        sql.append("UPPER(Descripcion) LIKE :ancla").append(i);
        parameters.addValue("ancla" + i, "%" + familia.anclas.get(i) + "%", Types.VARCHAR);
      }
    }
    // No offsets, COUNT, hints, shared-template changes, or database objects.
    sql.append(") ORDER BY tx_itm, codZafiro, cod3L, Descripcion, unidadMedida, "
        + "Marca, Material, categoriaCompra, lineaCompra, claseCompra");
    return jdbcTemplate.execute(sql.toString(), parameters, statement -> {
      statement.setQueryTimeout(2);
      statement.setFetchSize(LIMITE + 1);
      statement.setMaxRows(LIMITE + 1);
      try (ResultSet rows = statement.executeQuery()) {
        List<CatalogoArticuloDto> result = new ArrayList<>();
        while (rows.next()) {
          result.add(new CatalogoArticuloDto(rows.getString("tx_itm"), rows.getString("codZafiro"),
              rows.getString("cod3L"), rows.getString("Descripcion"), rows.getString("unidadMedida"),
              rows.getString("Marca"), rows.getString("Material"), rows.getString("categoriaCompra"),
              rows.getString("lineaCompra"), rows.getString("claseCompra")));
        }
        return result;
      }
    });
  }
}
