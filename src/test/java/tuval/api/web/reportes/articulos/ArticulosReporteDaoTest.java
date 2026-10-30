package tuval.api.web.reportes.articulos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import tuval.api.web.reportes.articulos.dao.dynamics.ArticulosReporteDao;
import tuval.api.web.reportes.articulos.request.ArticulosReporteRequest;

class ArticulosReporteDaoTest {
  // Independent expected schema from configVista.xlsx (not inferred from production code).
  static final List<String> COLUMNS = List.of(
      "tx_itm", "codZafiro", "cod3L", "Descripcion", "unidadMedida", "claseItem",
      "precio", "peso", "costo", "transito", "cabGrupoVta", "detGrupoVta", "cabGrupoCom",
      "detGrupoComp", "promVtaMenAct", "promVtaMenTeo", "promVntaMenAnt", "moq", "fre",
      "stokbase", "exc", "itemCabecera", "categoriaCompra", "lineaCompra", "claseCompra",
      "Marca", "Material", "compras", "SubCompras", "Control", "SubControl",
      "fechacreacion", "catven", "catpes", "catfre", "cattuv", "marUti");
  private final DataSource source = mock(DataSource.class);
  private final Connection connection = mock(Connection.class);
  private final PreparedStatement statement = mock(PreparedStatement.class);
  private final ResultSet rows = mock(ResultSet.class);
  private final ArticulosReporteDao dao =
      new ArticulosReporteDao(new NamedParameterJdbcTemplate(source));

  @BeforeEach
  void jdbc() throws Exception {
    when(source.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(rows);
  }

  @Test
  void defaultsSelectOnlyThe37ColumnsFromTheViewAndLimitInSql() throws Exception {
    assertThat(dao.consultar(new ArticulosReporteRequest())).isEmpty();
    String sql = sql();
    assertThat(sql).startsWith("SELECT ").contains("FROM dbo.vw_MaestroArticulo")
        .contains("ORDER BY [tx_itm] ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")
        .doesNotContain("*", "COUNT(", "NOLOCK", "INSERT ", "UPDATE ", "DELETE ", "MERGE ", "TRUNCATE ");
    assertThat(java.util.Arrays.stream(sql.substring("SELECT ".length(), sql.indexOf("FROM"))
        .split(",")).map(String::trim).toList()).containsExactlyElementsOf(COLUMNS);
    verify(statement).setObject(1, 0L, Types.BIGINT);
    verify(statement).setObject(2, 100, Types.INTEGER);
    verify(statement).setQueryTimeout(30);
    verify(statement).setFetchSize(100);
    verify(statement).setMaxRows(100);
    verify(statement).executeQuery();
    verify(statement, never()).executeUpdate();
    verify(statement, never()).execute();
    verify(connection).close();
    verify(rows).close();
  }

  @Test
  void bindsAllCombinedFiltersWithTheirSqlTypesAndEscapesLiteralLikeCharacters() throws Exception {
    var request = new ArticulosReporteRequest();
    request.setTx_itm("O'Brien");
    request.setCodZafiro("ZAF");
    request.setCod3L("BEV");
    request.setDescripcion("50%_[x]~' OR 1=1--");
    request.setClaseItem("CLASE");
    request.setUnidadMedida("PZAS");
    request.setCategoriaCompra("CAT");
    request.setLineaCompra("LINEA");
    request.setClaseCompra("COMPRA");
    request.setMarca("MARCA");
    request.setMaterial("MATERIAL");
    request.setItemCabecera("CAB");
    request.setCatven("V");
    request.setCatpes("P");
    request.setCatfre("F");
    request.setCattuv("T");
    request.setPage(2);
    request.setSize(50);
    dao.consultar(request);
    String sql = sql();
    for (String column : List.of("tx_itm", "codZafiro", "cod3L", "claseItem", "unidadMedida",
        "categoriaCompra", "lineaCompra", "claseCompra", "Marca", "Material", "itemCabecera",
        "catven", "catpes", "catfre", "cattuv")) {
      assertThat(sql).contains("AND [" + column + "] = ?");
    }
    assertThat(sql).contains("[Descripcion] LIKE ? ESCAPE '~'")
        .doesNotContain("O'Brien", "OR 1=1", "50%");
    String[] values = {"O'Brien", "ZAF", "BEV", "%50~%~_~[x]~~' OR 1=1--%", "CLASE",
        "PZAS", "CAT", "LINEA", "COMPRA", "MARCA", "MATERIAL", "CAB", "V", "P", "F", "T"};
    for (int index = 0; index < values.length; index++) {
      verify(statement).setString(index + 1, values[index]);
    }
    verify(statement).setObject(17, 100L, Types.BIGINT);
    verify(statement).setObject(18, 50, Types.INTEGER);
  }

  @Test
  void maxSizeAndLargePageKeepLongOffsetAndSecondaryOrder() throws Exception {
    var request = new ArticulosReporteRequest();
    request.setPage(Integer.MAX_VALUE);
    request.setSize(500);
    request.setSort(ArticulosReporteRequest.Sort.precio);
    request.setDirection(ArticulosReporteRequest.Direction.DESC);
    dao.consultar(request);
    assertThat(sql()).contains("ORDER BY [precio] DESC, [tx_itm] ASC");
    verify(statement).setObject(1, 1073741823500L, Types.BIGINT);
    verify(statement).setObject(2, 500, Types.INTEGER);
    verify(statement).setMaxRows(500);
  }

  @Test
  void mapsEveryColumnWithoutLosingDecimalPrecisionOrTimestamp() throws Exception {
    var expected = new LinkedHashMap<String, Object>();
    List<String> decimal5 = List.of("precio", "costo", "promVntaMenAnt");
    List<String> decimal6 = List.of("peso", "transito", "promVtaMenAct", "promVtaMenTeo",
        "moq", "fre", "stokbase", "exc");
    List<String> smallints = List.of("cabGrupoVta", "detGrupoVta", "cabGrupoCom", "detGrupoComp");
    for (int i = 0; i < COLUMNS.size(); i++) {
      String column = COLUMNS.get(i);
      if (decimal5.contains(column) || decimal6.contains(column) || column.equals("marUti")) {
        String suffix = decimal5.contains(column) ? ".12345" : column.equals("marUti") ? ".1234" : ".123456";
        long integerPart = (decimal6.contains(column) ? 999_999_999_900L : 99_999_999_999_900L) + i;
        var value = new BigDecimal(integerPart + suffix);
        when(rows.getBigDecimal(column)).thenReturn(value);
        expected.put(column, value);
      } else if (smallints.contains(column)) {
        Short value = (short) (i + 1);
        when(rows.getObject(column, Short.class)).thenReturn(value);
        expected.put(column, value);
      } else if (column.equals("fechacreacion")) {
        var value = Timestamp.valueOf("2025-01-23 12:34:56.997");
        when(rows.getTimestamp(column)).thenReturn(value);
        expected.put(column, "2025-01-23T12:34:56.997");
      } else {
        when(rows.getString(column)).thenReturn("value-" + column);
        expected.put(column, "value-" + column);
      }
    }
    when(rows.next()).thenReturn(true, false);
    var actual = dao.consultar(new ArticulosReporteRequest()).getFirst();
    var mapper = new ObjectMapper().findAndRegisterModules();
    JsonNode actualJson = mapper.valueToTree(actual);
    JsonNode expectedJson = mapper.valueToTree(expected);
    assertThat(actualJson).isEqualTo(expectedJson);
    assertThat(actual.precio()).isEqualTo(new BigDecimal("99999999999906.12345"));
    assertThat(actual.peso()).isEqualTo(new BigDecimal("999999999907.123456"));
  }

  @Test
  void retainsNullForEveryColumnAndIncludesAll37JsonProperties() throws Exception {
    when(rows.next()).thenReturn(true, false);
    var dto = dao.consultar(new ArticulosReporteRequest()).getFirst();
    var json = new ObjectMapper().findAndRegisterModules().valueToTree(dto);
    assertThat(json.size()).isEqualTo(37);
    for (String column : COLUMNS) {
      assertThat(json.has(column)).as(column).isTrue();
      assertThat(json.get(column).isNull()).as(column).isTrue();
    }
  }

  @Test
  void qualifierSelectsDynamicsEvenWhenTuvalIsPrimary() {
    var dynamics = new NamedParameterJdbcTemplate(mock(DataSource.class));
    var tuval = new NamedParameterJdbcTemplate(mock(DataSource.class));
    try (var context = new AnnotationConfigApplicationContext()) {
      context.registerBean("dynamicsNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class,
          () -> dynamics);
      context.registerBean("tuvalNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class,
          () -> tuval, definition -> definition.setPrimary(true));
      context.register(ArticulosReporteDao.class);
      context.refresh();
      assertThat(ReflectionTestUtils.getField(context.getBean(ArticulosReporteDao.class), "jdbcTemplate"))
          .isSameAs(dynamics);
    }
  }

  private String sql() throws Exception {
    var sql = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sql.capture());
    return sql.getValue();
  }
}
