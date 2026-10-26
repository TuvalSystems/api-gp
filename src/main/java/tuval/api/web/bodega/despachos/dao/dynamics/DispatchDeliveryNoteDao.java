package tuval.api.web.bodega.despachos.dao.dynamics;

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
import tuval.api.web.common.util.CoerceHelperUtils;
import tuval.api.web.bodega.despachos.entity.tuval.HeaderGuideTransferVtaResponse;
import tuval.api.web.bodega.despachos.request.DispatchVtaRequest;
import tuval.api.web.bodega.despachos.request.GuidePurchaseOrderRequest;

@Repository
public class DispatchDeliveryNoteDao {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spDispatchDeliveryNote;
  private final ObjectMapper objectMapper;

  public DispatchDeliveryNoteDao(
      @Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {

    this.objectMapper = objectMapper;
    this.jdbcTemplate = jdbcTemplate;
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

  public void insertGuidePurchaseOrder(GuidePurchaseOrderRequest request) {
    jdbcTemplate.update(
        """
        INSERT INTO [Dynamics2021].[dbo].[guia_remision_orden_compra]
            ([id], [id_guia_remision], [id_guia_reserva], [id_pedido_id],
             [tipo_guia], [id_oc], [fecha_ingreso])
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """,
        new Object[] {
          request.getId(), request.getIdGuiaRemision(), request.getIdGuiaReserva(),
          request.getIdPedidoId(), request.getTipoGuia(), request.getIdOc(),
          CoerceHelperUtils.toTimestamp(request.getFechaIngreso())
        },
        new int[] {
          Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER,
          Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP
        });
  }

  public HeaderGuideTransferVtaResponse processDispatch(DispatchVtaRequest request) {
    try {
      String json = objectMapper.writeValueAsString(request);

      MapSqlParameterSource parameters =
          new MapSqlParameterSource().addValue("json", json, Types.NVARCHAR);
      Map<String, Object> result = spDispatchDeliveryNote.execute(parameters);
      return getProcedureResponse(result, request);

    } catch (JsonProcessingException exception) {
      throw new RuntimeException("No se pudo convertir la guía de despacho a JSON.", exception);
    }
  }

  @SuppressWarnings("unchecked")
  private HeaderGuideTransferVtaResponse getProcedureResponse(
      Map<String, Object> result, DispatchVtaRequest request) {
    List<HeaderGuideTransferVtaResponse> responses =
        (List<HeaderGuideTransferVtaResponse>) result.get("resultado");

    if (responses == null || responses.isEmpty()) {
      throw new RuntimeException("El procedimiento sp_despacho_nuevo no devolvió una respuesta.");
    }

    HeaderGuideTransferVtaResponse response = responses.get(0);
    if (response.getCodRet() != 0) {
      String pedido =
          request.getCabecera() != null
              ? CoerceHelperUtils.defaultIfBlank(request.getCabecera().getPedido(), "sin pedido")
              : "sin pedido";
      String detalle =
          CoerceHelperUtils.defaultIfBlank(
              response.getMensaje(), "El procedimiento no devolvió detalle del error.");
      throw new IllegalStateException(
          ("No se pudo generar el movimiento de despacho para el pedido %s. "
                  + "ID movimiento: %d. Código de retorno: %d. Detalle: %s")
              .formatted(pedido, response.getIdMovimiento(), response.getCodRet(), detalle));
    }

    return response;
  }
}
