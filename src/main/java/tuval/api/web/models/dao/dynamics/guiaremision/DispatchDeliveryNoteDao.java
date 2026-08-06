package tuval.api.web.models.dao.dynamics.guiaremision;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.request.bodegas.guiaremision.DispatchVtaRequest;

@Repository
public class DispatchDeliveryNoteDao {
  private final SimpleJdbcCall spDispatchDeliveryNote;
  private final ObjectMapper objectMapper;

  public DispatchDeliveryNoteDao(
      @Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {

    this.objectMapper = objectMapper;
    jdbcTemplate.setResultsMapCaseInsensitive(true);

    this.spDispatchDeliveryNote =
        new SimpleJdbcCall(jdbcTemplate)
            .withSchemaName("dbo")
            .withProcedureName("sp_despacho_nuevo")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(new SqlParameter("json", Types.NVARCHAR))
            .returningResultSet(
                "resultado",
                (resultSet, rowNumber) ->
                    new HeaderGuideTransferVtaResponse(
                        CoerceHelperUtils.getInt(resultSet.getObject("codret", Integer.class), 0),
                        CoerceHelperUtils.stringOrEmpty(resultSet.getString("mensaje")),
                        CoerceHelperUtils.getInt(resultSet.getObject("idmov", Integer.class), 0),
                        CoerceHelperUtils.stringOrEmpty(resultSet.getString("docgp"))));
  }

  public HeaderGuideTransferVtaResponse processDispatch(DispatchVtaRequest request) {
    try {
      String json = objectMapper.writeValueAsString(request);

      MapSqlParameterSource parameters =
          new MapSqlParameterSource().addValue("json", json, Types.NVARCHAR);
      Map<String, Object> result = spDispatchDeliveryNote.execute(parameters);
      return getProcedureResponse(result);

    } catch (JsonProcessingException exception) {
      throw new RuntimeException("No se pudo convertir la guía de despacho a JSON.", exception);
    }
  }

  @SuppressWarnings("unchecked")
  private HeaderGuideTransferVtaResponse getProcedureResponse(Map<String, Object> result) {
    List<HeaderGuideTransferVtaResponse> responses =
        (List<HeaderGuideTransferVtaResponse>) result.get("resultado");

    if (responses == null || responses.isEmpty()) {
      throw new RuntimeException("El procedimiento sp_despacho_nuevo no devolvió una respuesta.");
    }

    return responses.get(0);
  }
}
