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
import tuval.api.web.models.dto.transacciones.reciboCompraL.DetailReceivedLRequest;
import tuval.api.web.models.dto.transacciones.reciboCompraL.HeaderReceivedLRequest;
import tuval.api.web.models.dto.transacciones.reciboCompraL.validateReceidLRequestEntity;
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
                        response.setCodigo(
                                (rs.getString("I_vCODIGO") != null && !rs.getString("I_vCODIGO").trim().isEmpty())
                                        ? rs.getString("I_vCODIGO").trim()
                                        : "0");
                        response.setMensaje(
                                (rs.getString("I_vMENSAJE") != null && !rs.getString("I_vMENSAJE").trim().isEmpty())
                                        ? rs.getString("I_vMENSAJE").trim()
                                        : "Documento generado correctamente");
                        response.setTipoDocGenerado(
                                rs.getString("TIPO_DOC_GENERADO") != null ? rs.getString("TIPO_DOC_GENERADO").trim()
                                        : "");
                        response.setNumDocGenerado(
                                rs.getString("DOC_GENERADO") != null ? rs.getString("DOC_GENERADO").trim() : "");
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
            throw new RuntimeException(
                    "Error al ejecutar el sp: MSW_SP_VALIDAR_GENERACION_RECEPCION: " + e.getMessage(), e);
        }
    }

    public validateResponseEntity executeSpDeleteReceivedHeaderAndDetail(validateReceidLRequestEntity request) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("MSW_SP_ELIMINACION_RECEPCION_CAB_DET")
                    .declareParameters(
                            // Parámetros de entrada
                            new SqlParameter("@docu", Types.VARCHAR),
                            new SqlParameter("@tipo", Types.INTEGER));

            Map<String, Object> params = new HashMap<>();
            params.put("docu", request.getDocumento() != null ? request.getDocumento().trim() : "");
            params.put("tipo", request.getTipo() != null ? request.getTipo().trim() : "");
            jdbcCall.execute(params);

            validateResponseEntity response = new validateResponseEntity();
            response.setCodigo("0"); // por convención, código 0 = OK
            response.setMensaje("Recibo eliminado correctamente");
            response.setTipoDocGenerado(""); // opcional
            response.setNumDocGenerado(""); // opcional
            return response;
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "Error al ejecutar el SP: MSW_SP_ELIMINACION_RECEPCION_CAB_DET. " + e.getMessage(), e);
        }
    }

    public void executeSpStoreHeaderReceivedL(HeaderReceivedLRequest request) {

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("MSW_SP_GUARDAR_RECEPCION_CAB")
                .declareParameters(
                        // Parámetros de entrada
                        new SqlParameter("@docu", Types.VARCHAR),
                        new SqlParameter("@id_tipo", Types.INTEGER),
                        new SqlParameter("@proveedor", Types.VARCHAR),
                        new SqlParameter("@fecha", Types.DATE));

        Map<String, Object> params = new HashMap<>();
        params.put("docu", request.getRecComDocuOrigen() != null ? request.getRecComDocuOrigen().trim() : "");
        params.put("id_tipo", request.getRecComIdTipo() != null ? request.getRecComIdTipo().trim() : "");
        params.put("proveedor", request.getRecComIdProveedor() != null ? request.getRecComIdProveedor().trim() : "");
        params.put("fecha", request.getRecComFecha() != null ? request.getRecComFecha() : "");
        jdbcCall.execute(params);
    }

    public void executeSpStoreDetailReceivedL(DetailReceivedLRequest request) {

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("MSW_SP_GUARDAR_RECEPCION_DET")
                .declareParameters(
                        // Parámetros de entrada
                        new SqlParameter("@docu", Types.VARCHAR),
                        new SqlParameter("@tipo", Types.INTEGER),
                        new SqlParameter("@articulo", Types.VARCHAR),
                        new SqlParameter("@descripcion_articulo", Types.VARCHAR),
                        new SqlParameter("@unidad", Types.VARCHAR),
                        new SqlParameter("@bodega", Types.VARCHAR),
                        new SqlParameter("@cantidad", Types.DECIMAL));

        Map<String, Object> params = new HashMap<>();
        params.put("docu", request.getRecComDocuOrigen() != null ? request.getRecComDocuOrigen().trim() : "");
        params.put("tipo", request.getRecComIdTipo() != null ? request.getRecComIdTipo() : "");
        params.put("articulo", request.getRecCOmIdArticulo() != null ? request.getRecCOmIdArticulo().trim() : "");
        params.put("descripcion_articulo",
                request.getRecComDescripcionArticulo() != null ? request.getRecComDescripcionArticulo() : "");
        params.put("unidad", request.getRecComUnidadMedida() != null ? request.getRecComUnidadMedida().trim() : "");
        params.put("bodega", request.getRecComBodega() != null ? request.getRecComBodega().trim() : "");
        params.put("cantidad", request.getRecComCantidad() != null ? request.getRecComCantidad() : "");
        jdbcCall.execute(params);

    }

    public void executeSpGenerateProcessReceivedL(validateReceidLRequestEntity request) {

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("MSW_SP_CREACION_RECEPCION_OC")
                .declareParameters(
                        // Parámetros de entrada
                        new SqlParameter("@DOCUMENTO", Types.VARCHAR));

        Map<String, Object> params = new HashMap<>();
        params.put("DOCUMENTO", request.getDocumento() != null ? request.getDocumento().trim() : "");
        jdbcCall.execute(params);
    }

    public validateResponseEntity executeSpResponseReceivedL(validateReceidLRequestEntity request) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("MSW_SP_VALIDAR_GENERACION_RECEPCION")
                    .declareParameters(
                            // Parámetros de entrada
                            new SqlParameter("@docu", Types.VARCHAR),
                            new SqlParameter("@tipo", Types.INTEGER))
                    .returningResultSet("resultados", (rs, rowNum) -> {
                        validateResponseEntity response = new validateResponseEntity();
                        response.setCodigo(
                                (rs.getString("I_vCODIGO") != null && !rs.getString("I_vCODIGO").trim().isEmpty())
                                        ? rs.getString("I_vCODIGO").trim()
                                        : "0");
                        response.setMensaje(
                                (rs.getString("I_vMENSAJE") != null && !rs.getString("I_vMENSAJE").trim().isEmpty())
                                        ? rs.getString("I_vMENSAJE").trim()
                                        : "Documento generado correctamente");
                        response.setTipoDocGenerado(
                                rs.getString("TIPO_DOC_GENERADO") != null ? rs.getString("TIPO_DOC_GENERADO").trim()
                                        : "");
                        response.setNumDocGenerado(
                                rs.getString("DOC_GENERADO") != null ? rs.getString("DOC_GENERADO").trim() : "");
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
            throw new RuntimeException(
                    "Error al ejecutar el sp: MSW_SP_VALIDAR_GENERACION_RECEPCION: " + e.getMessage(), e);
        }
    }

}
