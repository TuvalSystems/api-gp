package tuval.api.web.reportes.resolucionarticulos;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.sql.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import tuval.api.web.reportes.resolucionarticulos.dao.dynamics.ResolucionArticulosReporteDao;
import tuval.api.web.reportes.resolucionarticulos.dao.dynamics.ResolucionArticulosReporteDao.Familia;

class ResolucionArticulosReporteDaoTest {
  private final DataSource source = mock(DataSource.class);
  private final Connection connection = mock(Connection.class);
  private final PreparedStatement statement = mock(PreparedStatement.class);
  private final ResultSet rows = mock(ResultSet.class);
  private final ResolucionArticulosReporteDao dao = new ResolucionArticulosReporteDao(new NamedParameterJdbcTemplate(source));

  @BeforeEach
  void jdbc() throws Exception {
    when(source.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(rows);
  }

  @Test
  void soloSeleccionaSieteCamposSegurosConLimiteTimeoutLocalYSinonimos() throws Exception {
    assertThat(dao.buscar(Familia.NIPLE, null)).isEmpty();
    var sql = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sql.capture());
    assertThat(sql.getValue()).startsWith("SELECT TOP (?) tx_itm, codZafiro, cod3L, Descripcion, unidadMedida,")
        .contains("tx_atrMar, tx_atrMat", "FROM dbo.vw_listadoItems",
            "UPPER(Descripcion) LIKE ? OR UPPER(Descripcion) LIKE ?", "ORDER BY tx_itm COLLATE Latin1_General_100_BIN2")
        .doesNotContain("*", "precio", "costo", "marUti", "NOLOCK", "INSERT", "UPDATE", "DELETE",
            "categoriaCompra", "lineaCompra", "claseCompra");
    verify(statement).setObject(1, 501, Types.INTEGER);
    verify(statement).setString(2, "%NIPLE%");
    verify(statement).setString(3, "%NEPLO%");
    verify(statement).setQueryTimeout(2);
    verify(statement).setMaxRows(501);
    verify(statement).setFetchSize(501);
    verify(rows).close();
    verify(connection).close();
  }

  @Test
  void codigoSeEnlazaLiteralmenteSinInterpolacionNiLike() throws Exception {
    String malicious = "O'BRIEN%_[];--";
    dao.buscar(null, malicious);
    var sql = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sql.capture());
    assertThat(sql.getValue()).contains("tx_itm = ? OR codZafiro = ? OR cod3L = ?")
        .doesNotContain(malicious, "LIKE");
    for (int i = 2; i <= 4; i++) verify(statement).setString(i, malicious);
  }

  @Test
  void anclasIncluyenOrtografiaConAcentoSinDependerDeCollationInsensible() throws Exception {
    dao.buscar(Familia.VALVULA_BOLA, null);
    verify(statement).setString(2, "%VALVULA%");
    verify(statement).setString(3, "%VÁLVULA%");
    verify(statement).setString(4, "%LLAVE%");
  }

  @Test
  void conservaNullYMapeaSoloCamposSeguros() throws Exception {
    when(rows.next()).thenReturn(true, false);
    when(rows.getString("tx_itm")).thenReturn("GP");
    when(rows.getString("Descripcion")).thenReturn("DESCRIPCION");
    var row = dao.buscar(Familia.CODO, null).getFirst();
    assertThat(row.codigoGP()).isEqualTo("GP");
    assertThat(row.descripcion()).isEqualTo("DESCRIPCION");
    assertThat(row.codigoZafiro()).isNull();
    assertThat(row.esquemaUnidad()).isNull();
    verify(rows, never()).getBigDecimal(anyString());
  }

  @Test
  void usaFuenteLigeraYOrdenBinarioDeTodosLosCamposSinMaestroNiPaginacion() throws Exception {
    dao.buscar(Familia.BUSHING, null);
    var sql = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sql.capture());
    assertThat(sql.getValue()).contains("FROM dbo.vw_listadoItems")
        .doesNotContain("vw_MaestroArticulo", "OFFSET", "DISTINCT", "COUNT(");
    for (String columna : java.util.List.of("tx_itm", "codZafiro", "cod3L", "Descripcion",
        "unidadMedida", "tx_atrMar", "tx_atrMat")) {
      assertThat(sql.getValue()).contains(columna + " COLLATE Latin1_General_100_BIN2",
          "DATALENGTH(" + columna + ")");
    }
  }

  @Test
  void mapeaEsquemaYAtributosSinRenombrarlosComoCamposDelMaestro() throws Exception {
    when(rows.next()).thenReturn(true, false);
    when(rows.getString("unidadMedida")).thenReturn("ESQUEMA");
    when(rows.getString("tx_atrMar")).thenReturn("ATR-MAR");
    when(rows.getString("tx_atrMat")).thenReturn("ATR-MAT");
    var row = dao.buscar(Familia.BUSHING, null).getFirst();
    assertThat(row.esquemaUnidad()).isEqualTo("ESQUEMA");
    assertThat(row.atributoMarca()).isEqualTo("ATR-MAR");
    assertThat(row.atributoMaterial()).isEqualTo("ATR-MAT");
    verify(rows, never()).getString("Marca");
    verify(rows, never()).getString("Material");
  }

  @Test
  void nuncaConsultaSinAncla() {
    assertThatThrownBy(() -> dao.buscar(null, null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> dao.buscar(Familia.CODO, "GP")).isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(source);
  }

  @Test
  void seleccionaDynamicsAunConTuvalPrimario() {
    var dynamics = new NamedParameterJdbcTemplate(mock(DataSource.class));
    var tuval = new NamedParameterJdbcTemplate(mock(DataSource.class));
    try (var context = new AnnotationConfigApplicationContext()) {
      context.registerBean("dynamicsNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class, () -> dynamics);
      context.registerBean("tuvalNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class,
          () -> tuval, definition -> definition.setPrimary(true));
      context.register(ResolucionArticulosReporteDao.class);
      context.refresh();
      assertThat(ReflectionTestUtils.getField(context.getBean(ResolucionArticulosReporteDao.class), "jdbcTemplate"))
          .isSameAs(dynamics);
    }
  }
}
