package tuval.api.web.reportes.listadoitems;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tuval.api.web.reportes.listadoitems.dao.dynamics.ListadoItemsReporteDao;
import tuval.api.web.reportes.listadoitems.request.ListadoItemsReporteRequest;

class ListadoItemsReporteDaoTest {
  static final List<String> COLUMNS = List.of("tx_itm", "codZafiro", "cod3L", "Descripcion",
      "unidadMedida", "tx_atrMar", "tx_atrMat");
  private final DataSource source = mock(DataSource.class);
  private final Connection connection = mock(Connection.class);
  private final PreparedStatement statement = mock(PreparedStatement.class);
  private final ResultSet rows = mock(ResultSet.class);
  private final NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(source);
  private final ListadoItemsReporteDao dao = new ListadoItemsReporteDao(template);

  @BeforeEach
  void jdbc() throws Exception {
    when(source.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(rows);
  }

  @Test
  void selectsSevenColumnsWithBoundedSqlPagingAndBinaryOrdering() throws Exception {
    assertThat(dao.consultar(new ListadoItemsReporteRequest())).isEmpty();
    String sql = sql();
    assertThat(sql).startsWith("SELECT ").contains("FROM dbo.vw_listadoItems")
        .contains("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")
        .doesNotContain("*", "COUNT(", "NOLOCK", "INSERT ", "UPDATE ", "DELETE ", "MERGE ");
    assertThat(java.util.Arrays.stream(sql.substring(7, sql.indexOf("FROM")).split(","))
        .map(String::trim).map(s -> s.replace("[", "").replace("]", "")).toList())
        .containsExactlyElementsOf(COLUMNS);
    String order = sql.substring(sql.indexOf("ORDER BY"), sql.indexOf("OFFSET"));
    for (String column : COLUMNS) {
      assertThat(order).containsPattern("\\[?" + column + "\\]?\\s+COLLATE\\s+\\w*BIN2(?:\\s+ASC)?");
      assertThat(order).containsPattern("DATALENGTH\\(\\[?" + column + "\\]?\\)");
    }
    verify(statement).setObject(1, 0L, Types.BIGINT);
    verify(statement).setObject(2, 100, Types.INTEGER);
    verify(statement).setQueryTimeout(10);
    verify(statement).setFetchSize(100);
    verify(statement).setMaxRows(100);
    verify(statement, never()).executeUpdate();
    verify(statement, never()).execute();
    verify(rows).close();
    verify(statement).close();
    verify(connection).close();
  }

  @Test
  void bindsCombinedFiltersAsVarcharAndEscapesLiteralLikeCharacters() throws Exception {
    var request = new ListadoItemsReporteRequest();
    request.setCodigo("O'Brien");
    request.setDescripcion("50%_[x]~' OR 1=1--");
    request.setMarca("MARCA");
    request.setMaterial("MATERIAL");
    request.setPage(2);
    request.setSize(50);
    dao.consultar(request);
    assertThat(sql().replace("[", "").replace("]", "")).contains("tx_itm = ?", "OR codZafiro = ?", "OR cod3L = ?",
        "Descripcion LIKE ? ESCAPE '~'", "tx_atrMar = ?", "tx_atrMat = ?")
        .doesNotContain("O'Brien", "OR 1=1", "50%");
    for (int i = 1; i <= 3; i++) verify(statement).setString(i, "O'Brien");
    verify(statement).setString(4, "%50~%~_~[x]~~' OR 1=1--%");
    verify(statement).setString(5, "MARCA");
    verify(statement).setString(6, "MATERIAL");
    verify(statement).setObject(7, 100L, Types.BIGINT);
    verify(statement).setObject(8, 50, Types.INTEGER);
  }

  @Test
  void maximumPageUsesLongOffsetWithoutChangingSharedTemplateSettings() throws Exception {
    var jdbc = template.getJdbcTemplate();
    jdbc.setQueryTimeout(73);
    jdbc.setFetchSize(71);
    jdbc.setMaxRows(79);
    var request = new ListadoItemsReporteRequest();
    request.setPage(10000);
    request.setSize(500);
    dao.consultar(request);
    verify(statement).setObject(1, 5000000L, Types.BIGINT);
    verify(statement).setObject(2, 500, Types.INTEGER);
    verify(statement).setQueryTimeout(10);
    verify(statement).setFetchSize(500);
    verify(statement).setMaxRows(500);
    assertThat(jdbc.getQueryTimeout()).isEqualTo(73);
    assertThat(jdbc.getFetchSize()).isEqualTo(71);
    assertThat(jdbc.getMaxRows()).isEqualTo(79);
  }

  @Test
  void mapsEveryStringToItsExactJsonName() throws Exception {
    when(rows.next()).thenReturn(true, false);
    for (String column : COLUMNS) when(rows.getString(column)).thenReturn("value-" + column);
    var json = new ObjectMapper().valueToTree(dao.consultar(new ListadoItemsReporteRequest()).getFirst());
    assertThat(json.size()).isEqualTo(7);
    for (String column : COLUMNS) assertThat(json.path(column).asText()).isEqualTo("value-" + column);
  }

  @Test
  void preservesAllSqlNullsAndJsonProperties() throws Exception {
    when(rows.next()).thenReturn(true, false);
    var json = new ObjectMapper().valueToTree(dao.consultar(new ListadoItemsReporteRequest()).getFirst());
    assertThat(json.size()).isEqualTo(7);
    for (String column : COLUMNS) {
      assertThat(json.has(column)).as(column).isTrue();
      assertThat(json.get(column).isNull()).as(column).isTrue();
    }
  }

  @Test
  void closesResourcesWhenMappingFails() throws Exception {
    when(rows.next()).thenReturn(true);
    when(rows.getString(anyString())).thenThrow(new SQLException("mapping failed"));
    assertThatThrownBy(() -> dao.consultar(new ListadoItemsReporteRequest()))
        .isInstanceOf(org.springframework.dao.DataAccessException.class);
    verify(rows).close();
    verify(statement).close();
    verify(connection, atLeastOnce()).close();
  }

  @Test
  void qualifierSelectsDynamicsEvenWhenTuvalIsPrimary() throws Exception {
    DataSource tuvalSource = mock(DataSource.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      context.registerBean("dynamicsNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class, () -> template);
      context.registerBean("tuvalNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class,
          () -> new NamedParameterJdbcTemplate(tuvalSource), definition -> definition.setPrimary(true));
      context.register(ListadoItemsReporteDao.class);
      context.refresh();
      assertThat(context.getBean(ListadoItemsReporteDao.class).consultar(new ListadoItemsReporteRequest())).isEmpty();
      verify(source).getConnection();
      verifyNoInteractions(tuvalSource);
    }
  }

  private String sql() throws Exception {
    var capture = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(capture.capture());
    return capture.getValue();
  }
}
