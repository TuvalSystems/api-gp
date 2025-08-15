package tuval.api.web.models.dao.tuval.transacciones;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.entity.tuval.transacciones.reciboCompraL.validateReceidLRequestEntity;
import tuval.api.web.models.entity.tuval.transacciones.reciboCompraL.validateResponseEntity;

@Repository
public class transactionDao {

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

    public validateResponseEntity executeSpValidateGenerateReceivedItemL(validateReceidLRequestEntity request) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("MSW_SP_VALIDAR_GENERACION_RECEPCION")
                    .declareParameters(
                            // Parámetros de entrada
                            new SqlParameter("@docu", Types.VARCHAR),
                            new SqlParameter("@tipo", Types.VARCHAR))
                    .returningResultSet("resultados", (rs, rowNum) -> {
                        validateResponseEntity response = new validateResponseEntity();
                        response.setCodigo((rs.getString("I_vCODIGO") != null && !rs.getString("I_vCODIGO").trim().isEmpty()) ? rs.getString("I_vCODIGO").trim() : "0");
                        response.setMensaje((rs.getString("I_vMENSAJE") != null && !rs.getString("I_vMENSAJE").trim().isEmpty()) ? rs.getString("I_vMENSAJE").trim() : "Documento generado correctamente");
                        response.setTipoDocGenerado(rs.getString("TIPO_DOC_GENERADO") != null ? rs.getString("TIPO_DOC_GENERADO").trim() : "");
                        response.setNumDocGenerado(rs.getString("DOC_GENERADO") != null ? rs.getString("DOC_GENERADO").trim() : "");
                        return response;
                    });
            Map<String, Object> params = new HashMap<>();
            params.put("docu", request.getDocumento() != null ? request.getDocumento().trim() : "");
            params.put("tipo", request.getTipo() != null ? request.getTipo().trim() : "");
            Map<String, Object> out = jdbcCall.execute(params);
            @SuppressWarnings("unchecked")
            List<validateResponseEntity> resultados = (List<validateResponseEntity>) out.get("resultados");

            if (resultados == null || resultados.isEmpty()) {
                // Lanza excepción si no hay registros
                throw new RuntimeException("No existe registro para los parámetros proporcionados");
            }
            return resultados.get(0);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al ejecutar el sp: MSW_SP_VALIDAR_GENERACION_RECEPCION: " + e.getMessage(), e);
        }
    }

    public validateResponseEntity executeSpDeleteReceivedHeaderAndDetail(validateReceidLRequestEntity request) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("MSW_SP_ELIMINACION_RECEPCION_CAB_DET")
                    .declareParameters(
                            // Parámetros de entrada
                            new SqlParameter("@docu", Types.VARCHAR),
                            new SqlParameter("@tipo", Types.VARCHAR));

            Map<String, Object> params = new HashMap<>();
            params.put("docu", request.getDocumento() != null ? request.getDocumento().trim() : "");
            params.put("tipo", request.getTipo() != null ? request.getTipo().trim() : "");
            jdbcCall.execute(params);

            validateResponseEntity response = new validateResponseEntity();
            response.setCodigo("0");  // por convención, código 0 = OK
            response.setMensaje("Recibo eliminado correctamente");
            response.setTipoDocGenerado(""); // opcional
            response.setNumDocGenerado("");  // opcional
            return response;
        } catch (RuntimeException e) {
              throw new RuntimeException("Error al ejecutar el SP: MSW_SP_ELIMINACION_RECEPCION_CAB_DET. " + e.getMessage(), e);
        }
    }


    
}
