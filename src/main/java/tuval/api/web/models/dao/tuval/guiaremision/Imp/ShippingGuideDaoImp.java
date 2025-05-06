package tuval.api.web.models.dao.tuval.guiaremision.Imp;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
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
import tuval.api.web.models.dao.tuval.guiaremision.ShippingGuideDao;

@Repository
public class ShippingGuideDaoImp implements ShippingGuideDao {

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

    @Override

    public ResponseEntity<Map<String, Object>> checkGuiaRemision(String numDoc) {
           Map<String, Object> response = new HashMap<>();

           try {

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("MSW_SP_VALIDAR_GENERACION_GUIA_REMISION")
                    .declareParameters(
                            new SqlParameter("@docu", Types.VARCHAR))
                            .returningResultSet("response", (rs, rowNum) -> Map.of(
                                "documentoOrigen", rs.getString("I_vDOCU_ORIGEN"),
                                "codigo", rs.getString("I_vCODIGO"),
                                "mensaje", rs.getString("I_vMENSAJE"),
                                "documentoGenerado", rs.getString("DOC_GENERADO"),
                                "claveAcceso", rs.getString("CLAVE_ACCESO")
                            ));

                    Map<String, Object> params = new HashMap<>();
                    params.put("docu", numDoc != null ? numDoc.trim() : "");

                    Map<String, Object> result = jdbcCall.execute(params); 

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> resultSet = (List<Map<String, Object>>) result.get("response");

                    if (!resultSet.isEmpty()) {
                        Map<String, Object> firstResult = resultSet.get(0);
                        response.put("success", true);
                        response.put("message", "Guía de remisión generada correctamente.");
                        response.put("data",firstResult);
                        return ResponseEntity.ok(response);
                    }

                    response.put("success", false);
                    response.put("message", "No se encontró información para el documento.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            
           } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al validar la devolución.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
           }
    }
}
