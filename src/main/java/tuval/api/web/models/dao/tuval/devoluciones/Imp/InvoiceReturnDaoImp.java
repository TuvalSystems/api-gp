package tuval.api.web.models.dao.tuval.devoluciones.Imp;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.dao.tuval.devoluciones.InvoiceReturnDao;

public class InvoiceReturnDaoImp implements InvoiceReturnDao {

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

    public ResponseEntity<Map<String, Object>> ValidateGenerationDocumentVTANC(String document, String tipo) 
    {
        Map<String, Object> response = new HashMap<>();
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("MSW_SP_VALIDAR_GENERACION_NC_VTA")
                .declareParameters(
                    new SqlParameter("@docu", Types.VARCHAR),
                    new SqlParameter("@tipo", Types.VARCHAR)
                );
            Map<String, Object> params = new HashMap<>();
            params.put("docu", document != null ? document.trim() : "");
            params.put("tipo", tipo != null ? tipo.trim() : "");
            Map<String, Object> result = jdbcCall.execute(params);
            if (result != null && result.get("RESULTADO") != null) {
                response.put("success", true);
                response.put("message", "Devolucion validada correctamente.");
                response.put("data", result.get("RESULTADO"));
            }
           return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al al validar la devolucion.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    public ResponseEntity<Map<String, Object>> eliminacionNcCabDet(String document, String tipo) 
    {
        Map<String, Object> response = new HashMap<>();
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("MSW_SP_ELIMINACION_NC_VTA_CAB_DET")
                .declareParameters(
                    new SqlParameter("@docu", Types.VARCHAR),
                    new SqlParameter("@tipo", Types.VARCHAR)
                );
            Map<String, Object> params = new HashMap<>();
            params.put("docu", document != null ? document.trim() : "");
            params.put("tipo", tipo != null ? tipo.trim() : "");
            Map<String, Object> result = jdbcCall.execute(params);
            if (result != null && result.get("RESULTADO") != null) {
                response.put("success", true);
                response.put("message", "Devolucion eliminada correctamente.");
                response.put("data", result.get("RESULTADO"));
            }
           return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar la devolucion.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        
    }
}
