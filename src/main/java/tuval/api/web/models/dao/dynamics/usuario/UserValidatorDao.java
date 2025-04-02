package tuval.api.web.models.dao.dynamics.usuario;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.entity.dynamics.usuario.UserValidatorEntity;
import tuval.api.web.models.entity.dynamics.usuario.UserValidatorResponseEntity;

@Repository
public class UserValidatorDao {

    //esta variable se inyecta en el constructor- prueba de uso de constructor directamente con transacciones
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UserValidatorDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void validateConfiguration() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
        }
        try {
            jdbcTemplate.execute("SELECT 1");
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo establecer conexión con la base de datos Dynamics", e);
        }
    }

    @Transactional(transactionManager = "dynamicsTransactionManager")
    public UserValidatorResponseEntity validateUserDynamic(String idCompany, String username) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_tuv_consulta_usuarios")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("@compania", Types.VARCHAR),
                            new SqlParameter("@userid", Types.VARCHAR),
                            new SqlParameter("@modo", Types.INTEGER),
                            new SqlParameter("@equipo", Types.VARCHAR),
                            new SqlOutParameter("@codret", Types.INTEGER),
                            new SqlOutParameter("@msj", Types.VARCHAR))
                    .returningResultSet("resultados",
                            new BeanPropertyRowMapper<>(UserValidatorEntity.class));

            Map<String, Object> params = new HashMap<>();
            params.put("@compania", idCompany);
            params.put("@userid", username);
            params.put("@modo", 2);
            params.put("@equipo", "wserver");

            Map<String, Object> response = jdbcCall.execute(params);

            UserValidatorResponseEntity responseEntity = new UserValidatorResponseEntity();

            responseEntity.setCodRet((Integer) response.get("@codret"));
            responseEntity.setMsj((String) response.get("@msj"));

            @SuppressWarnings("unchecked")

            List<UserValidatorEntity> results = (List<UserValidatorEntity>) response.get("resultados");
            UserValidatorEntity data = (results != null && !results.isEmpty()) ? results.get(0) : null;
            responseEntity.setData(data);
            return responseEntity;

        } catch (Exception e) {
            throw new RuntimeException("Error al consultar transferencias: " + e.getMessage(), e);
        }

    }
}
