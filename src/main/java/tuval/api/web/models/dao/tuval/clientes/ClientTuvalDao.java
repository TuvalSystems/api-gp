package tuval.api.web.models.dao.tuval.clientes;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.request.cliente.ClientRequest;

@Repository
public class ClientTuvalDao {

  @Autowired
  @Qualifier("tuvalJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  private SimpleJdbcCall spClienteTuval;


  public void validateConfiguration() {
    if (jdbcTemplate == null) {
      throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
    }
  }

  @PostConstruct
  public void init() {
    validateConfiguration();

    this.spClienteTuval =
        new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("MSW_SP_CREACION_MODIFICACION_CLIENTE")
            .withSchemaName("dbo")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("ruc", Types.VARCHAR),
                new SqlParameter("razon_social", Types.VARCHAR),
                new SqlParameter("nombre_corto", Types.VARCHAR),
                new SqlParameter("clase_cliente", Types.VARCHAR),
                new SqlParameter("prioridad", Types.INTEGER),
                new SqlParameter("contacto", Types.VARCHAR),
                new SqlParameter("direccion1", Types.VARCHAR),
                new SqlParameter("direccion2", Types.VARCHAR),
                new SqlParameter("ciudad", Types.VARCHAR),
                new SqlParameter("provincia", Types.VARCHAR),
                new SqlParameter("pais", Types.VARCHAR),
                new SqlParameter("territorio", Types.VARCHAR),
                new SqlParameter("telefono1", Types.VARCHAR),
                new SqlParameter("telefono2", Types.VARCHAR),
                new SqlParameter("telefono3", Types.VARCHAR),
                new SqlParameter("telefono4", Types.VARCHAR),
                new SqlParameter("correo_electronico_sri", Types.VARCHAR),
                new SqlParameter("correo_electronico_notificacion_interna", Types.VARCHAR),
                new SqlParameter("vendedor", Types.VARCHAR),
                new SqlParameter("grupo", Types.VARCHAR),
                new SqlParameter("subgrupo", Types.VARCHAR),
                new SqlParameter("descuento_comercial", Types.DECIMAL), // numeric(19,5)
                new SqlParameter("condicion_pago", Types.VARCHAR),
                new SqlParameter("lista_precio", Types.VARCHAR),
                new SqlParameter("punto_venta", Types.VARCHAR),
                new SqlParameter("tipo_id", Types.VARCHAR))
                .returningResultSet("resultados", new ColumnMapRowMapper());
  }

  public void executeSpCreateOrModifyClient(ClientRequest cli) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("ruc", cli.getRuc())
            .addValue("razon_social", cli.getRazonSocial())
            .addValue("nombre_corto", cli.getNombreCorto())
            .addValue("clase_cliente", cli.getClase())
            .addValue("prioridad", cli.getPrioridad())
            .addValue("contacto", cli.getContacto())
            .addValue("direccion1", cli.getDireccion1())
            .addValue("direccion2", cli.getDireccion2())
            .addValue("ciudad", cli.getCiudad())
            .addValue("provincia", cli.getProvincia())
            .addValue("pais", cli.getPais())
            .addValue("territorio", cli.getTerritorio())
            .addValue("telefono1", cli.getTelefono1())
            .addValue("telefono2", cli.getTelefono2())
            .addValue("telefono3", cli.getTelefono3())
            .addValue("telefono4", cli.getTelefono4())
            .addValue("correo_electronico_sri", cli.getCorreoSri())
            .addValue("correo_electronico_notificacion_interna", cli.getCorreoNotificacion())
            .addValue("vendedor", cli.getVendedor())
            .addValue("grupo", cli.getGrupo())
            .addValue("subgrupo", cli.getSubGrupo())
            .addValue("descuento_comercial", cli.getDescComercial())
            .addValue("condicion_pago", cli.getCondPago())
            .addValue("lista_precio", cli.getNivelPrecio())
            .addValue("punto_venta", cli.getPuntoVenta())
            .addValue("tipo_id", cli.getTipoId());

    try {
      Map<String, Object> out = spClienteTuval.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> resultList = (List<Map<String, Object>>) out.get("resultados");
      if (resultList == null || resultList.isEmpty()) {
        throw new RuntimeException("El SP no devolvió ningún mensaje de confirmación.");
      }
      Map<String, Object> primerFila = resultList.get(0);
      String mensajeRespuesta = (String) primerFila.get("ESTADO_TRANSACCION");

      if (mensajeRespuesta == null || !mensajeRespuesta.startsWith("EXITOSO")) {
        throw new RuntimeException("Error en base de datos: " + mensajeRespuesta);
      }
    } catch (Exception e) {
      throw new RuntimeException("SP executeSpStoreHeaderDeliveryNote falló: " + e.getMessage(), e);
    }
  }
}
