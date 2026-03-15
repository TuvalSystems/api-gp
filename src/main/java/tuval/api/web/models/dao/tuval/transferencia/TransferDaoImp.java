package tuval.api.web.models.dao.tuval.transferencia;

import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dto.tranferencias.RepositionDetailDto;
import tuval.api.web.models.dto.tranferencias.RepositionHeaderDto;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.request.bodegas.reposiciones.DetailRepositionRequest;
import tuval.api.web.models.request.bodegas.reposiciones.HeaderRepositionRequest;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;

@Repository
public class TransferDaoImp implements TransferDao {

  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spTransferBinToBin;
  private final SimpleJdbcCall repositionJdbcTemplate;

  public TransferDaoImp(@Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "El JdbcTemplate no puede ser null");
    this.spTransferBinToBin = new SimpleJdbcCall(jdbcTemplate)
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

    this.repositionJdbcTemplate = new SimpleJdbcCall(jdbcTemplate)
        .withProcedureName("sp_tuv_consulta_transfer_iv")
        .withSchemaName("dbo")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(
            new SqlParameter("@numdoc", Types.VARCHAR),
            new SqlParameter("@fecpro", Types.DATE),
            new SqlParameter("@userid", Types.CHAR),
            new SqlParameter("@modo", Types.INTEGER),
            new SqlParameter("@submodo", Types.INTEGER),
            new SqlParameter("@equipo", Types.VARCHAR),
            new SqlOutParameter("@codret", Types.INTEGER),
            new SqlOutParameter("@msj", Types.VARCHAR));
  }

  // funcion de consulta de transferencias al servidor

  @Override
  public List<RepositionHeaderDto> consultDocumentTransfer(
      HeaderRepositionRequest req, int modo) {

    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("@numdoc", req.getNumDoc())
        .addValue("@fecpro", req.getFecha())
        .addValue("@userid", "usuario")
        .addValue("@modo", modo)
        .addValue("@submodo", 0)
        .addValue("@equipo", "wserver");

    try {
      Map<String, Object> out = repositionJdbcTemplate.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rows = (List<Map<String, Object>>) out.get("#result-set-1");
      List<RepositionHeaderDto> resultList = new ArrayList<>();
      if (rows != null) {
        for (Map<String, Object> row : rows) {
          RepositionHeaderDto dto = new RepositionHeaderDto();
          dto.setNumdoc((String) row.get("numdoc"));
          dto.setCodmotivo((String) row.get("codmotivo"));
          dto.setMotivo((String) row.get("motivo"));
          dto.setOrigen((String) row.get("origen"));
          dto.setDestino((String) row.get("destino"));
          Object fechaObj = row.get("fecha");
          dto.setFecha(fechaObj != null ? (java.util.Date) fechaObj : null);
          resultList.add(dto);
        }
      }
      return resultList;
    } catch (Exception e) {
      throw new RuntimeException("Error al consultar transferencias: " + e.getMessage(), e);
    }
  }

  @Override
  public List<RepositionDetailDto> consultarDocumentodetalleTransferencia(
      DetailRepositionRequest req, int modo) {

    MapSqlParameterSource in = new MapSqlParameterSource()
        .addValue("@numdoc", req.getNumDoc())
        .addValue("@fecpro", "")
        .addValue("@userid", "usuario")
        .addValue("@modo", modo)
        .addValue("@submodo", 0)
        .addValue("@equipo", "wserver");

    try {
      Map<String, Object> out = repositionJdbcTemplate.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rows = (List<Map<String, Object>>) out.get("#result-set-1");
      List<RepositionDetailDto> resultList = new ArrayList<>();
      if (rows != null) {
        for (Map<String, Object> row : rows) {
          RepositionDetailDto dto = new RepositionDetailDto();

          // Los nombres entre comillas deben coincidir EXACTAMENTE con los que te
          // devuelve el JSON actual
          dto.setDocumento((String) row.get("Documento"));
          dto.setOrigen((String) row.get("Origen"));
          dto.setDestino((String) row.get("Destino"));
          dto.setItem((String) row.get("Item"));
          dto.setNombreItem((String) row.get("NombreItem"));

          // Manejo seguro para la cantidad (por si viene como BigDecimal, Double o
          // Integer)
          Object cantidadObj = row.get("Cantidad");
          dto.setCantidad(cantidadObj != null ? String.valueOf(cantidadObj) : "0");
          dto.setTrdUbic((String) row.get("trd_ubic"));
          resultList.add(dto);
        }
      }
      return resultList;

    } catch (Exception e) {
      throw new RuntimeException("Error al consultar el detalle de transferencias: " + e.getMessage(), e);
    }

  }

  @Override
  public TransfersCompartmentsOrderDto transferenciaCompartimento(
      TransferCompartmentOrdersRequest transfers) {

    MapSqlParameterSource in = new MapSqlParameterSource()
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
