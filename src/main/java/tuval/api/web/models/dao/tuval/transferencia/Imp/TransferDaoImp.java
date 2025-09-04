package tuval.api.web.models.dao.tuval.transferencia.Imp;

import java.sql.Date;
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
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.dao.tuval.transferencia.TransferDao;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailResponseEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderResponseEntity;

@Repository
public class TransferDaoImp implements TransferDao {

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

    // funcion de consulta de transferencias al servidor

    public TransferHeaderResponseEntity consultDocumentTransfer(Date date, String userid, int Modo, String numDoc) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_tuv_consulta_transfer_iv")
                    .declareParameters(
                            // Parámetros de entrada
                            new SqlParameter("@numdoc", Types.VARCHAR),
                            new SqlParameter("@fecpro", Types.DATE),
                            new SqlParameter("@userid", Types.CHAR),
                            new SqlParameter("@modo", Types.INTEGER),
                            new SqlParameter("@submodo", Types.INTEGER),
                            new SqlParameter("@equipo", Types.VARCHAR),
                            new SqlOutParameter("@codret", Types.INTEGER),
                            new SqlOutParameter("@msj", Types.VARCHAR)

                    ).returningResultSet("resultados",
                            new BeanPropertyRowMapper<>(TransferHeaderEntity.class)); // para hacerme el mapeo de la
                                                                                      // data que esta en el Entity ya
                                                                                      // que me esta dando el tipado de
                                                                                      // la data

            Map<String, Object> params = new HashMap<>();
            params.put("@numdoc", (numDoc != null && !numDoc.isEmpty()) ? numDoc : null);
            params.put("@fecpro", date);
            params.put("@userid", userid != null ? userid : "");
            params.put("@modo", Modo);
            params.put("@submodo", 0);
            params.put("@equipo", "wserver");

            Map<String, Object> result = jdbcCall.execute(params);

            TransferHeaderResponseEntity response = new TransferHeaderResponseEntity();

            response.setCodRet((Integer) result.get("@codret"));
            response.setMsj((String) result.get("@msj"));
            @SuppressWarnings("unchecked")
            List<TransferHeaderEntity> data = (List<TransferHeaderEntity>) result.get("resultados");
            response.setData(data);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar transferencias: " + e.getMessage(), e);
        }
    }

    public TransferDetailResponseEntity consultarDocumentodetalleTransferencia(String userid, int Modo, String numDoc) {

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_tuv_consulta_transfer_iv")
                    .declareParameters(
                            // Parámetros de entrada
                            new SqlParameter("@numdoc", Types.VARCHAR),
                            new SqlParameter("@fecpro", Types.DATE),
                            // new SqlParameter("@userid", Types.CHAR),
                            new SqlParameter("@modo", Types.INTEGER),
                            // new SqlParameter("@submodo", Types.INTEGER),
                            new SqlParameter("@equipo", Types.VARCHAR),
                            new SqlOutParameter("@codret", Types.INTEGER),
                            new SqlOutParameter("@msj", Types.VARCHAR)

                    ).returningResultSet("resultados",
                            new BeanPropertyRowMapper<>(TransferDetailEntity.class)); // para hacerme el mapeo de la
                                                                                      // data que esta en el Entity ya
                                                                                      // que me esta dando el tipado de
                                                                                      // la data

            Map<String, Object> params = new HashMap<>();
            params.put("@numdoc", numDoc);
            params.put("@fecpro", "");
            params.put("@userid", userid != null ? userid : "");
            params.put("@modo", Modo);
            params.put("@submodo", 0);
            params.put("@equipo", "wserver");

            Map<String, Object> result = jdbcCall.execute(params);

            TransferDetailResponseEntity response = new TransferDetailResponseEntity();

            response.setCodRet((Integer) result.get("@codret"));
            response.setMsj((String) result.get("@msj"));
            @SuppressWarnings("unchecked")
            List<TransferDetailEntity> data = (List<TransferDetailEntity>) result.get("resultados");
            response.setData(data);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar transferencias: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Map<String, Object>> TransferenciaCompartimento(Date fecha, String item, String bodega,
            Double cantidad, String BinOrigen, String BinDestino) {
        Map<String, Object> response = new HashMap<>();
        try {

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_transfer_bin_to_bin")
                    .declareParameters(
                            new SqlParameter("@DATE", Types.DATE),
                            new SqlParameter("@ITEMNMBR", Types.VARCHAR),
                            new SqlParameter("@LOCNCODE", Types.VARCHAR),
                            new SqlParameter("@BIN", Types.VARCHAR),
                            new SqlParameter("@TOBIN", Types.VARCHAR),
                            new SqlParameter("@QUANTITY", Types.DECIMAL),
                            new SqlParameter("@RESULT", Types.INTEGER),
                            new SqlParameter("@NUM_DOC", Types.VARCHAR)

                    );
            Map<String, Object> params = new HashMap<>();
            params.put("@DATE", fecha);
            params.put("@ITEMNMBR", item);
            params.put("@LOCNCODE", bodega);
            params.put("@BIN", BinOrigen);
            params.put("@TOBIN", BinDestino);
            params.put("@QUANTITY", cantidad);
            params.put("@RESULT", 0);
            params.put("@NUM_DOC", "");
            Map<String, Object> result = jdbcCall.execute(params);

            response.put("result", result.get("#update-count-1"));
            response.put("success", true);
            response.put("message", "Transferencia registrada correctamente.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al registrar la Transferencia.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        }

    }
}
