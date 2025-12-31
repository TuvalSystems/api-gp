package tuval.api.web.models.dao.tuval.transferencia;

import java.sql.Date;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailResponseEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderResponseEntity;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;

@Repository
public class TransferDaoImp implements TransferDao {

  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spTransferBinToBin;

  public TransferDaoImp(@Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "El JdbcTemplate no puede ser null");
    this.spTransferBinToBin =
        new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("sp_transfer_bin_to_bin")
            .withSchemaName("dbo")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("@DATE", Types.DATE),
                new SqlParameter("@ITEMNMBR", Types.VARCHAR),
                new SqlParameter("@LOCNCODE", Types.VARCHAR),
                new SqlParameter("@BIN", Types.VARCHAR),
                new SqlParameter("@QUANTITY", Types.DECIMAL),
                new SqlParameter("@TOBIN", Types.VARCHAR),
                new SqlOutParameter("@RESULT", Types.INTEGER),
                new SqlOutParameter("@NUM_DOC", Types.VARCHAR));
  }

  // funcion de consulta de transferencias al servidor

  @Override
  public TransferHeaderResponseEntity consultDocumentTransfer(
      Date date, String userid, int Modo, String numDoc) {
    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(Objects.requireNonNull(jdbcTemplate))
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
                  new SqlOutParameter("@msj", Types.VARCHAR))
              .returningResultSet(
                  "resultados",
                  new BeanPropertyRowMapper<>(
                      TransferHeaderEntity.class)); // para hacerme el mapeo de la
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

  @Override
  public TransferDetailResponseEntity consultarDocumentodetalleTransferencia(
      String userid, int Modo, String numDoc) {

    try {
      SimpleJdbcCall jdbcCall =
          new SimpleJdbcCall(Objects.requireNonNull(jdbcTemplate))
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
                  new SqlOutParameter("@msj", Types.VARCHAR))
              .returningResultSet(
                  "resultados",
                  new BeanPropertyRowMapper<>(
                      TransferDetailEntity.class)); // para hacerme el mapeo de la
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

  @Override
  public TransfersCompartmentsOrderDto TransferenciaCompartimento(
      TransferCompartmentOrdersRequest transfers) {

    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("@DATE", transfers.getFecha())
            .addValue("@ITEMNMBR", transfers.getItem())
            .addValue("@LOCNCODE", transfers.getBodega())
            .addValue("@BIN", transfers.getOrigen())
            .addValue("@TOBIN", transfers.getDestino())
            .addValue("@QUANTITY", transfers.getCantidad());

    try {
      Map<String, Object> out = spTransferBinToBin.execute(in);

      Object resultObj = out.get("@RESULT");
      Object numDocObj = out.get("@NUM_DOC");

      if (numDocObj == null) {
        throw new RuntimeException("El SP no devolvió un número de documento.");
      }
      TransfersCompartmentsOrderDto dto = new TransfersCompartmentsOrderDto();
      if (resultObj != null) {
        dto.setResult(Integer.parseInt(resultObj.toString()));
      }
      dto.setNumDoc(numDocObj.toString());
      return dto;
    } catch (Exception e) {
      throw new RuntimeException("SP TransferenciaCompartimento falló: " + e.getMessage(), e);
    }
  }
}
