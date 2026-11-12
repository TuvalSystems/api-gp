package tuval.api.web.reportes.inventario.articulos.dao.dynamics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.reportes.inventario.articulos.dto.ArticulosDto;
import tuval.api.web.reportes.inventario.articulos.dto.CatalogoArticuloDto;
import tuval.api.web.reportes.inventario.articulos.dto.ListadoItemsDto;
import tuval.api.web.reportes.inventario.articulos.request.ArticulosRequest;
import tuval.api.web.reportes.inventario.articulos.request.ListadoItemsRequest;

@Repository
public class ArticulosDao {
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

  private static final String SELECT_LISTADO_ITEMS = """
      SELECT tx_itm, codZafiro, cod3L, Descripcion, unidadMedida, tx_atrMar, tx_atrMat
      FROM dbo.vw_listadoItems
      WHERE 1 = 1
      """;
  // No key has been verified: order by every returned value, including binary
  // tie-breaks and lengths for case/accent/trailing-space variants.
  private static final String ORDER_LISTADO_ITEMS = """
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

  public static final int LIMITE_RESOLUCION = 500;
  // A closed retrieval vocabulary, including aliases understood by the matcher.
  public enum Familia {
    BUSHING(List.of("BUSHING")), NIPLE(List.of("NIPLE", "NEPLO")),
    CODO(List.of("CODO")), NUDO(List.of("NUDO", "UNION UNIVERSAL", "UNIÓN UNIVERSAL")),
    VALVULA_BOLA(List.of("VALVULA", "VÁLVULA", "LLAVE")), TEFLON(List.of("TEFLON", "TEFLÓN", "PTFE")),
    PLANCHA(List.of("PLANCHA", "PLANCHON")), ANGULO(List.of("ANGULO", "ÁNGULO")),
    TUBO(List.of("TUBO")), PERNO(List.of("PERNO")), EJE(List.of("EJE", "BARRA")),
    CHUMACERA(List.of("CHUMACERA")), BRONCE(List.of("BARRA", "BUJE", "BOCIN"));
    private final List<String> anclas;
    Familia(List<String> anclas) { this.anclas = anclas; }
  }

  public ArticulosDao(
      @Qualifier("dynamicsNamedParameterJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<ArticulosDto> consultar(ArticulosRequest request) {
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
    if (request.getSort() != ArticulosRequest.Sort.tx_itm) {
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
        List<ArticulosDto> result = new ArrayList<>();
        RowMapper<ArticulosDto> mapper = articulosMapper();
        while (rows.next()) {
          result.add(mapper.mapRow(rows, result.size()));
        }
        return result;
      }
    });
  }

  public List<ListadoItemsDto> consultarListadoItems(ListadoItemsRequest request) {
    var sql = new StringBuilder(SELECT_LISTADO_ITEMS);
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
    sql.append(ORDER_LISTADO_ITEMS);
    parameters.addValue("offset", (long) request.getPage() * request.getSize(), Types.BIGINT);
    parameters.addValue("size", request.getSize(), Types.INTEGER);

    // Timeout and row limits belong to this statement, not to the shared template.
    return jdbcTemplate.execute(sql.toString(), parameters, statement -> {
      statement.setQueryTimeout(10);
      statement.setFetchSize(request.getSize());
      statement.setMaxRows(request.getSize());
      try (ResultSet rows = statement.executeQuery()) {
        List<ListadoItemsDto> result = new ArrayList<>();
        RowMapper<ListadoItemsDto> mapper = listadoItemsMapper();
        while (rows.next()) {
          result.add(mapper.mapRow(rows, result.size()));
        }
        return result;
      }
    });
  }

  private static RowMapper<ListadoItemsDto> listadoItemsMapper() {
    return (rows, rowNum) -> new ListadoItemsDto(
        rows.getString("tx_itm"), rows.getString("codZafiro"), rows.getString("cod3L"),
        rows.getString("Descripcion"), rows.getString("unidadMedida"),
        rows.getString("tx_atrMar"), rows.getString("tx_atrMat"));
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

  private static LocalDateTime creationDate(ResultSet rows) throws SQLException {
    var timestamp = rows.getTimestamp("fechacreacion");
    return timestamp == null ? null : timestamp.toLocalDateTime();
  }

  private static RowMapper<ArticulosDto> articulosMapper() {
    return (rows, rowNum) ->
      new ArticulosDto(
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

  public List<CatalogoArticuloDto> buscarCandidatosResolucion(Familia familia, String codigo) {
    return buscarCandidatosResolucion(familia, codigo, Map.of());
  }

  public List<CatalogoArticuloDto> buscarCandidatosResolucion(Familia familia, String codigo,
      Map<String, String> filtros) {
    var consulta = consultaResolucion(familia, codigo, filtros);
    return jdbcTemplate.execute(consulta.sql(), consulta.parametros(), statement -> {
      statement.setQueryTimeout(2);
      statement.setFetchSize(LIMITE_RESOLUCION + 1);
      statement.setMaxRows(LIMITE_RESOLUCION + 1);
      try (ResultSet rows = statement.executeQuery()) {
        return leerCatalogo(rows);
      }
    });
  }

  public record BusquedaResolucion(Familia familia, String codigo, Map<String, String> filtros) {
    public BusquedaResolucion { filtros = Map.copyOf(filtros); }
  }

  /** One JDBC execution, with an independent TOP and deterministic order for each search. */
  public List<List<CatalogoArticuloDto>> buscarCandidatosResolucionLote(List<BusquedaResolucion> busquedas) {
    if (busquedas == null || busquedas.isEmpty() || busquedas.size() > 20) {
      throw new IllegalArgumentException("Lote de busquedas invalido");
    }
    var sql = new StringBuilder();
    var parametros = new MapSqlParameterSource();
    for (int i = 0; i < busquedas.size(); i++) {
      var busqueda = busquedas.get(i);
      var consulta = consultaResolucion(busqueda.familia(), busqueda.codigo(), busqueda.filtros());
      String prefijo = "b" + i + "_";
      sql.append(consulta.sql().replaceAll(":([A-Za-z][A-Za-z0-9]*)", ":" + prefijo + "$1")).append(";\n");
      for (String nombre : consulta.parametros().getParameterNames()) {
        parametros.addValue(prefijo + nombre, consulta.parametros().getValue(nombre),
            consulta.parametros().getSqlType(nombre));
      }
    }
    return jdbcTemplate.execute(sql.toString(), parametros, statement -> {
      statement.setQueryTimeout(2);
      statement.setFetchSize(LIMITE_RESOLUCION + 1);
      // TOP belongs to each SELECT. A statement-wide maxRows would obscure that scope.
      List<List<CatalogoArticuloDto>> resultado = new ArrayList<>();
      boolean disponible = statement.execute();
      for (int i = 0; i < busquedas.size(); i++) {
        if (!disponible) throw new SQLException("Resultado de busqueda incompleto");
        try (ResultSet rows = statement.getResultSet()) {
          resultado.add(leerCatalogo(rows));
        }
        if (i + 1 < busquedas.size()) disponible = statement.getMoreResults();
      }
      return resultado;
    });
  }

  private record ConsultaResolucion(String sql, MapSqlParameterSource parametros) {}

  private ConsultaResolucion consultaResolucion(Familia familia, String codigo, Map<String, String> filtros) {
    if ((familia == null) == (codigo == null) || (codigo != null &&
        (codigo.isBlank() || codigo.length() > 31))) {
      throw new IllegalArgumentException("Busqueda invalida");
    }
    var parameters = new MapSqlParameterSource().addValue("limite", LIMITE_RESOLUCION + 1, Types.INTEGER);
    var sql = new StringBuilder("""
        SELECT TOP (:limite) tx_itm, codZafiro, cod3L, Descripcion, unidadMedida,
               tx_atrMar, tx_atrMat
        FROM dbo.vw_listadoItems WHERE (
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
    sql.append(")");
    // Filter supported aliases before TOP, with parameters local to this statement.
    String material = filtros.get("material");
    if ("INOXIDABLE".equals(material)) sql.append(" AND UPPER(Descripcion) LIKE '%INOX%'");
    else if ("BRONCE".equals(material)) sql.append(" AND UPPER(Descripcion) LIKE '%BRONCE%'");
    else if ("GALVANIZADO".equals(material)) sql.append(" AND UPPER(Descripcion) LIKE '%GALV%'");
    String cedula = filtros.get("cedula");
    if (cedula != null && cedula.matches("[0-9]{1,3}")) {
      sql.append(" AND (UPPER(Descripcion + ' ') LIKE :ced OR UPPER(Descripcion + ' ') LIKE :cedula"
          + " OR UPPER(Descripcion + ' ') LIKE :sch OR UPPER(Descripcion + ' ') LIKE :schEspacio"
          + " OR UPPER(Descripcion + ' ') LIKE :c)");
      parameters.addValue("ced", "%CED " + cedula + "[^0-9]%");
      parameters.addValue("cedula", "%CEDULA " + cedula + "[^0-9]%");
      parameters.addValue("sch", "%SCH" + cedula + "[^0-9]%");
      parameters.addValue("schEspacio", "%SCH " + cedula + "[^0-9]%");
      parameters.addValue("c", "% C" + cedula + "[^0-9]%");
    }
    // No offsets, COUNT, hints, shared-template changes, or database objects.
    sql.append("""
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
        """);
    return new ConsultaResolucion(sql.toString(), parameters);
  }

  private static List<CatalogoArticuloDto> leerCatalogo(ResultSet rows) throws SQLException {
    List<CatalogoArticuloDto> result = new ArrayList<>();
    while (rows.next()) {
      result.add(new CatalogoArticuloDto(rows.getString("tx_itm"), rows.getString("codZafiro"),
          rows.getString("cod3L"), rows.getString("Descripcion"), rows.getString("unidadMedida"),
          rows.getString("tx_atrMar"), rows.getString("tx_atrMat")));
    }
    return result;
  }
}
