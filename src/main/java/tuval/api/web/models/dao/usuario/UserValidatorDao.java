package tuval.api.web.models.dao.usuario;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.entity.usuario.UserValidatorEntity;
import tuval.api.web.models.entity.usuario.UserValidatorResponseEntity;

@Repository
public class UserValidatorDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UserValidatorResponseEntity validateUserDynamic(String idCompany, String username) {

        try {

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_msw_consulta_usuarios_tuv1")
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
