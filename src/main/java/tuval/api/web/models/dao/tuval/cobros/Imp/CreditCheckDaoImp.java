package tuval.api.web.models.dao.tuval.cobros.Imp;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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

    public CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania, int modo) {
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
                    .returningResultSet("resultSet", (rs, rowNum) -> {

                        CheckValidatorEntity e = new CheckValidatorEntity();

                        e.setRuc((rs.getString("ruc") != null && !rs.getString("ruc").trim().isEmpty())
                                ? rs.getString("ruc").trim()
                                : "0");
                        Double cupo = rs.getObject("CUPO", Double.class);
                        e.setCupoAsignado(cupo != null ? cupo : 0d);

                        Double cupoPermitido = rs.getObject("CUPO_PERMITIDO", Double.class);
                        e.setCupoDisponibleGp(cupoPermitido != null ? cupoPermitido : 0d);

                        Integer susp = rs.getObject("SUSPENDIDO", Integer.class);
                        e.setSuspendido(susp != null ? susp : 0);
                        return e;
                    });
            Map<String, Object> result = jdbcCall.execute(
                    new MapSqlParameterSource()
                            .addValue("idcliente", ruc)
                            .addValue("userid", "wsrvmc")
                            .addValue("modo", modo)
                            .addValue("equipo", "wserver")
                            .addValue("idvendedor", "")
                            .addValue("documento", "")
                            .addValue("fecini", "")
                            .addValue("fecfin", "")
                            .addValue("codret", "")
                            .addValue("msj", ""));

            @SuppressWarnings("unchecked")
            List<CheckValidatorEntity> listaCupos = (List<CheckValidatorEntity>) result.get("resultSet");

            if (listaCupos == null || listaCupos.isEmpty()) {
                throw new RuntimeException("No existe cupo disponible para el cliente con RUC: " + ruc);
            }

            return listaCupos.get(0);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al ejecutar el sp: sp_mc_validacion_creditica: " + e.getMessage(), e);
        }

    }

}
