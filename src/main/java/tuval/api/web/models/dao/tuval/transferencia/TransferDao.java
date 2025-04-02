package tuval.api.web.models.dao.tuval.transferencia;

import java.sql.Date;
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

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailResponseEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderResponseEntity;

@Repository
public class TransferDao {

//Esta estructura de Constructor es para inyectar el JdbcTemplate

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

    public Map<String, Object> cabeceraTrfVenta(String numdoc, String bodorigen, String boddestino,
            String observacion, String userid, int modo) {

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_msw_movimientos_iv")
                .declareParameters(
                        new SqlParameter("@numdoc", Types.VARCHAR),
                        new SqlParameter("@bodorigen", Types.VARCHAR),
                        new SqlParameter("@boddestino", Types.VARCHAR),
                        new SqlParameter("@observacion", Types.VARCHAR),
                        new SqlParameter("@userid", Types.VARCHAR),
                        new SqlParameter("@modo", Types.INTEGER),
                        new SqlParameter("@idmov", Types.INTEGER),
                        new SqlParameter("@equipo", Types.INTEGER),
                        new SqlParameter("@codret", Types.INTEGER),
                        new SqlParameter("@msj", Types.VARCHAR));

        Map<String, Object> parms = new HashMap<>();
        parms.put("@numdoc", numdoc);
        parms.put("@bodorigen", bodorigen);
        parms.put("@boddestino", boddestino);
        parms.put("@observacion", observacion);
        parms.put("@userid", userid);
        parms.put("@modo", modo);
        parms.put("@idmov", 0);
        parms.put("@equipo", "wserver");
        return jdbcCall.execute(parms);

    }

    public Map<String, Object> detalleTrfVenta(
            int idMovimiento,
            String item,
            double cantidad,
            int secuencia,
            String compartimentoOrigen,
            String compartimentoDestino, int modo) {

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_msw_movimientos_iv")
                .declareParameters(
                        new SqlParameter("@idmov", Types.INTEGER),
                        new SqlParameter("@item", Types.VARCHAR),
                        new SqlParameter("@cantidad", Types.DOUBLE),
                        new SqlParameter("@secuencia", Types.INTEGER),
                        new SqlParameter("@comporigen", Types.VARCHAR),
                        new SqlParameter("@compdestino", Types.VARCHAR),
                        new SqlParameter("@modo", Types.INTEGER),
                        new SqlOutParameter("@codret", Types.INTEGER),
                        new SqlOutParameter("@msj", Types.VARCHAR));

        Map<String, Object> parms = new HashMap<>();
        parms.put("@idmov", idMovimiento);
        parms.put("@item", item);
        parms.put("@cantidad", cantidad);
        parms.put("@secuencia", secuencia);
        parms.put("@comporigen", compartimentoOrigen);
        parms.put("@compdestino", compartimentoDestino);
        parms.put("@modo", modo);
        return jdbcCall.execute(parms);
    }

    public Map<String, Object> generarMovimientoTrf(
            String numdoc,
            String bodorigen,
            String boddestino,
            String observacion,
            int idmov,
            String transaccionOriginal,
            String userid,
            int modo) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_msw_movimientos_iv")
                .declareParameters(
                        new SqlParameter("@numdoc", Types.VARCHAR),
                        new SqlParameter("@bodorigen", Types.VARCHAR),
                        new SqlParameter("@boddestino", Types.VARCHAR),
                        new SqlParameter("@observacion", Types.VARCHAR),
                        new SqlParameter("@idmov", Types.INTEGER),
                        new SqlOutParameter("@docgp", Types.VARCHAR),
                        new SqlParameter("@numdocori", Types.VARCHAR),
                        new SqlParameter("@userid", Types.CHAR),
                        new SqlParameter("@modo", Types.INTEGER),
                        new SqlParameter("@equipo", Types.VARCHAR),
                        new SqlOutParameter("@codret", Types.INTEGER),
                        new SqlOutParameter("@msj", Types.VARCHAR));

        Map<String, Object> params = new HashMap<>();
        params.put("@numdoc", numdoc);
        params.put("@bodorigen", bodorigen);
        params.put("@boddestino", boddestino);
        params.put("@observacion", observacion);
        params.put("@idmov", idmov);
        params.put("@docgp", ""); // Valor inicial vacío como en C#
        params.put("@numdocori", transaccionOriginal);
        params.put("@userid", userid);
        params.put("@modo", modo);
        params.put("@equipo", "wserver"); // Valor fijo como en C#
        return jdbcCall.execute(params);
    }

    // actualmente el sp no se encuentra en la base de pruebas
    public Map<String, Object> validarEmpresa(String clave) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("dbo")
                .withProcedureName("sp_mc_validaempresa")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("@clave", Types.VARCHAR),
                        new SqlOutParameter("@CMPANYID", Types.VARCHAR),
                        new SqlOutParameter("@INTERID", Types.INTEGER));

        Map<String, Object> params = new HashMap<>();
        params.put("@clave", clave);

        Map<String, Object> result = jdbcCall.execute(params);

        String interId = (String) result.get("@INTERID");
        if (interId == null || interId.isEmpty()) {
            throw new RuntimeException("No se pudo validar la empresa con la clave proporcionada");
        }

        return result;
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
}
