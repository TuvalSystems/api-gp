package tuval.api.web.models.dao.tuval.clientes;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import tuval.api.web.models.dto.cliente.NewAddressStatusDto;
import tuval.api.web.models.request.cliente.ClientRequest;
import tuval.api.web.models.request.cliente.NewAddressRequest;

@Repository
public class ClientTuvalDao {

  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spClienteTuval;
  private final SimpleJdbcCall spModifyClientTuval;

  public ClientTuvalDao(@Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.spClienteTuval = new SimpleJdbcCall(jdbcTemplate)
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

    this.spModifyClientTuval = new SimpleJdbcCall(jdbcTemplate)
        .withProcedureName("MSW_SP_CREACION_MODIFICACION_DIRECCION_CLIENTE")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(
            new SqlParameter("@RUC", Types.VARCHAR),
            new SqlParameter("@VENDEDOR", Types.VARCHAR),
            new SqlParameter("@CONTACTO", Types.VARCHAR),
            new SqlParameter("@ID_DIRECCION", Types.VARCHAR),
            new SqlParameter("@DIRECCION_1", Types.VARCHAR),
            new SqlParameter("@DIRECCION_2", Types.VARCHAR),
            new SqlParameter("@PAIS", Types.VARCHAR),
            new SqlParameter("@CIUDAD", Types.VARCHAR),
            new SqlParameter("@PROVINCIA", Types.VARCHAR),
            new SqlParameter("@TELEFONO1", Types.VARCHAR),
            new SqlParameter("@TELEFONO2", Types.VARCHAR),
            new SqlParameter("@TELEFONO3", Types.VARCHAR),
            new SqlParameter("@TELEFONO4", Types.VARCHAR),
            new SqlParameter("@TERRITORIO", Types.VARCHAR),
            new SqlParameter("@CORREO_ELECTRONICO_SRI", Types.VARCHAR),
            new SqlParameter("@CORREO_ELECTRONICO_NOTIFICACION_INTERNA", Types.VARCHAR),
            new SqlParameter("@punto_venta", Types.VARCHAR));

  
  }

  public void executeSpCreateOrModifyClient(ClientRequest cli) {
    MapSqlParameterSource in = new MapSqlParameterSource()
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

  public NewAddressStatusDto executeSpCreateOrModifyAddress(NewAddressRequest request) {
    try {
      MapSqlParameterSource in = new MapSqlParameterSource()
          .addValue("@RUC", request.getRuc())
          .addValue("@VENDEDOR", request.getVendedor())
          .addValue("@CONTACTO", request.getContacto())
          .addValue("@ID_DIRECCION", request.getDireccionId())
          .addValue("@DIRECCION_1", request.getDireccion1())
          .addValue("@DIRECCION_2", request.getDireccion2())
          .addValue("@PAIS", request.getPais())
          .addValue("@CIUDAD", request.getCiudad())
          .addValue("@PROVINCIA", request.getProvincia())
          .addValue("@TELEFONO1", request.getTelefono1())
          .addValue("@TELEFONO2", request.getTelefono2())
          .addValue("@TELEFONO3", request.getTelefono3())
          .addValue("@TELEFONO4", request.getTelefono4())
          .addValue("@TERRITORIO", request.getTerritorio())
          .addValue("@CORREO_ELECTRONICO_SRI", request.getCorreoSri())
          .addValue("@CORREO_ELECTRONICO_NOTIFICACION_INTERNA", request.getCorreoNotificacion())
          .addValue("@punto_venta", request.getPuntoVenta());

      Map<String, Object> out = spModifyClientTuval.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> resultList = (List<Map<String, Object>>) out.get("#result-set-1");
      if (resultList != null && !resultList.isEmpty()) {
        String rawMessage = (String) resultList.get(0).get("ESTADO_TRANSACCION");

        if (rawMessage != null) {
          // VALIDACIÓN: Si NO empieza con EXITOSO, es un error de negocio del SP
          if (!rawMessage.startsWith("EXITOSO")) {

            // Limpiamos el mensaje (quitamos el código *1234 si existe)
            String cleanError = rawMessage;
            if (rawMessage.contains("*")) {
              cleanError = rawMessage.split("\\*")[0].trim();
            }

            // LANZAMOS EXCEPCIÓN DE JAVA (El DAO aborta aquí)
            throw new RuntimeException(cleanError);
          }

          // ÉXITO: Parseamos y retornamos
          String[] parts = rawMessage.split("-");
          String mensaje = parts[0].trim();
          String identificador = (parts.length > 1) ? parts[1].trim() : null;

          return new NewAddressStatusDto(mensaje, identificador);
        }
      }

      throw new RuntimeException("Error: Respuesta vacía o nula del SP");

    } catch (ResponseStatusException e) {
      // Si ya es una excepción controlada (la que lanzamos arriba), la dejamos pasar
      throw e;
    } catch (Exception e) {
      // Errores de conexión, base de datos caída, etc.
      throw new RuntimeException("Fallo técnico en SP executeSpCreateOrModifyAddress: " + e.getMessage(), e);
    }
  }

 

}
