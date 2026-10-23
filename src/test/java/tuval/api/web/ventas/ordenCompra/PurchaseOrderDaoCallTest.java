package tuval.api.web.ventas.ordenCompra;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.util.ReflectionTestUtils;
import tuval.api.web.ventas.ordenCompra.dao.dynamics.PurchaseOrderDao;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PurchaseOrderDaoCallTest {
  @Test
  void generatesSqlServerParameterNamesWithAtPrefix() throws Exception {
    DataSource source = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    DatabaseMetaData metadata = mock(DatabaseMetaData.class);
    when(source.getConnection()).thenReturn(connection);
    when(connection.getMetaData()).thenReturn(metadata);
    when(metadata.getDatabaseProductName()).thenReturn("Microsoft SQL Server");
    when(metadata.supportsCatalogsInProcedureCalls()).thenReturn(true);
    when(metadata.supportsSchemasInProcedureCalls()).thenReturn(true);
    when(metadata.storesMixedCaseIdentifiers()).thenReturn(true);

    PurchaseOrderDao dao = new PurchaseOrderDao(new JdbcTemplate(source));
    SimpleJdbcCall call = (SimpleJdbcCall) ReflectionTestUtils.getField(
        dao, "spInsertarOrdenCompraDirectaGP");
    call.compile();

    assertThat(call.getCallString()).isEqualTo(
        "{? = call Dynamics2021.dbo.sp_InsertarOrdenCompraDirectaGP("
            + "@proveedor = ?, @usuario = ?, @tipo_ordDes = ?, "
            + "@valorOrdenCompra = ?, @bodega = ?, @tx_numgui = ?, @ordenCompra = ?)}");
    verify(connection, never()).prepareCall(anyString());
  }
}
