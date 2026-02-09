package tuval.api.web.models.dao.dynamics.usuario;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.dto.usuarios.UserRequest;
import tuval.api.web.models.entity.dynamics.usuario.UserValidatorEntity;

@Repository
public class UserValidatorDao {

  @Autowired
  @Qualifier("dynamicsJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  private SimpleJdbcCall spConsultUsers;

  @PostConstruct
  public void init() {
    validateConfiguration();
    this.spConsultUsers =
        new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("sp_tuv_consulta_usuarios")
            .withSchemaName("dbo")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("compania", Types.VARCHAR),
                new SqlParameter("userid", Types.VARCHAR),
                new SqlParameter("modo", Types.INTEGER),
                new SqlParameter("equipo", Types.VARCHAR),
                new SqlOutParameter("codret", Types.INTEGER),
                new SqlOutParameter("msj", Types.VARCHAR));
  }

  public void validateConfiguration() {
    if (jdbcTemplate == null) {
      throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
    }
    try {
      jdbcTemplate.execute("SELECT 1");
    } catch (Exception e) {
      throw new IllegalStateException(
          "No se pudo establecer conexión con la base de datos Dynamics", e);
    }
  }

  @Transactional(transactionManager = "dynamicsTransactionManager")
  public UserValidatorEntity validateUserDynamic(UserRequest userRequest) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("compania", userRequest.getIdCompany())
            .addValue("userid", userRequest.getUsername())
            .addValue("modo", 2)
            .addValue("equipo", "wserver");
    try {
      Map<String, Object> out = spConsultUsers.execute(in);
      Integer codret = (Integer) out.get("codret");
      String mensaje = (String) out.get("msj");
      if (codret != 0) {
        throw new RuntimeException(mensaje);
      }
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> resultList = (List<Map<String, Object>>) out.get("#result-set-1");
      if (resultList != null && !resultList.isEmpty()) {
        // 2. Obtenemos la primera fila (mapa de columna -> valor)
        Map<String, Object> row = resultList.get(0);

        // 3. Mapeamos manualmente a tu entidad
        UserValidatorEntity entity = new UserValidatorEntity();


        // Usamos las llaves en MAYÚSCULAS como aparecen en tu ejemplo (USERID, INTERID)
        entity.setCodRet(codret);
        entity.setUserId( CoerceHelperUtils.stringOrEmpty(row.get("USERID")));
        entity.setInterId(CoerceHelperUtils.stringOrEmpty(row.get("INTERID")));

        // Asignamos el mensaje general del SP
        entity.setMessage(mensaje);

        return entity;
      }

      return null;
    } catch (Exception e) {
      throw new IllegalStateException(
          "Error executing stored procedure sp_tuv_consulta_usuarios", e);
    }
  }
}
