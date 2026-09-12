package tuval.api.web.bodega.transferencias.dao.dynamics.imp;

import java.sql.Date;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import tuval.api.web.bodega.transferencias.dao.dynamics.TransferDynamicDao;

@Repository
public class TransferDynamicDaoImp implements TransferDynamicDao {

    @PostConstruct
    public void init() {
        validateConfiguration();
    }

    @Autowired
    @Qualifier("dynamicsJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public void validateConfiguration() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateTrcNumdocAndTrcStatusTransfer(Date date, String numDoc) {
        Map<String, Object> response = new HashMap<>();
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_tuv_actualizaDocRep")
                    .declareParameters(
                            new SqlParameter("@numtra", Types.VARCHAR),
                            new SqlParameter("@fecpro", Types.DATE)
                    );

            Map<String, Object> params = new HashMap<>();
            params.put("@numtra", (numDoc != null && !numDoc.isEmpty()) ? numDoc : null);
            params.put("@fecpro", date);

            Map<String, Object> result = jdbcCall.execute(params);
            System.out.println("SP Result: " + result);

            response.put("success", true);
            response.put("message", "Transferencia actualizada correctamente.");
            response.put("data", result.get("result")); // Assuming the stored procedure returns a result set
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
