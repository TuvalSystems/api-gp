package tuval.api.web.models.dao.tuval.cobros.Imp;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.dao.tuval.cobros.CreditCheckDao;
import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;

@Repository
public class CreditCheckDaoImp implements CreditCheckDao {

    @PostConstruct
    public void init() {
        validateConfiguration();
    }

    @Autowired
    @Qualifier("tuvalJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public void validateConfiguration() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
        }
    }

    public ResponseEntity<Map<String, Object>> getConsultCreditValidation(String ruc, String idCompania, int modo) {
        Map<String, Object> response = new HashMap<>();
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_mc_validacion_creditica")
                    .declareParameters(
                            new SqlParameter("idcliente", Types.VARCHAR),
                            new SqlParameter("userid", Types.VARCHAR),
                            new SqlParameter("modo", Types.VARCHAR),
                            new SqlParameter("equipo", Types.VARCHAR),
                            new SqlParameter("codret", Types.VARCHAR),
                            new SqlParameter("msj", Types.VARCHAR))
                            .returningResultSet("resultSet", BeanPropertyRowMapper.newInstance(CheckValidatorEntity.class));;
            Map<String, Object> params = Map.of(
                    "idcliente", ruc,
                    "userid", "wsrvmc",
                    "modo", modo,
                    "equipo", "wserver",
                    "idvendedor", "",
                    "documento", "", 
                    "fecini","", 
                    "fecfin","",
                    "codret", 0,
                    "msj", "");

            Map<String, Object> result = jdbcCall.execute(params);
            System.out.println(result);
            @SuppressWarnings("unchecked")
           List<CheckValidatorEntity> listaCupos = (List<CheckValidatorEntity>) result.get("resultSet");

          if (listaCupos == null || listaCupos.isEmpty()) {
              return buildError("No se encontraron cupos.");
            }

            response.put("success", true);
            response.put("message", "Cupos encontrados.");
            response.put("cupos", listaCupos);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return buildException(e);
        }

    }

    private ResponseEntity<Map<String, Object>> buildError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ResponseEntity<Map<String, Object>> buildException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
