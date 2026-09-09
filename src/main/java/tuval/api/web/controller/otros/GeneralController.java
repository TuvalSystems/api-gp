package tuval.api.web.controller.otros;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.otros.pedidos.OrderDetailDto;
import tuval.api.web.models.dto.otros.pedidos.OrderHeaderDto;
import tuval.api.web.models.request.otros.cotizaciones.QuoteHeaderRequest;
import tuval.api.web.models.request.otros.pedidos.OrderDetailListRequest;
import tuval.api.web.models.request.otros.pedidos.OrderListRequest;
import tuval.api.web.models.request.otros.pedidos.OrdersDetailListUpdateRequest;
import tuval.api.web.models.request.otros.pedidos.OrdersHeadersListUpdateRequest;
import tuval.api.web.models.service.tuval.otros.GeneralService;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
@RequiredArgsConstructor
public class GeneralController {

  /** estas api son para actualizar datos historicos de facturas */
  private final GeneralService generalService;

  @CrossOrigin
  @PostMapping("facturas-cabecera-pendientes")
  public ResponseEntity<Map<String, Object>> obtenerListaCabeceraPedidosPendientes(
      @RequestBody OrderListRequest request) {
    try {
      List<OrderHeaderDto> response = generalService.getAllOrderPendingUpdate(request);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("facturas-detalles-pendientes")
  public ResponseEntity<Map<String, Object>> obtenerListaDetallePedidosPendientes(
      @RequestBody OrderDetailListRequest payload) {
    try {
      List<OrderDetailDto> response = generalService.getAllOrderDetailsPendingUpdate(payload);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @PostMapping("update-headers")
  public ResponseEntity<Map<String, Object>> updateHeaders(
      @RequestBody OrdersHeadersListUpdateRequest payload) {
    try {
      int[] result = generalService.processMassiveHeaderUpdate(payload);

      int totalActualizados = Arrays.stream(result).sum();

      Map<String, Object> data =
          Map.of(
              "totalEnviados", payload.getHeaders().size(),
              "totalActualizados", totalActualizados,
              "resultadoBatch", result);

      return ResponseJsonUtils.buildSuccessWithData("Actualización de cabeceras exitosa", data);

    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  // Endpoint para DETALLES
  @PostMapping("update-details")
  public ResponseEntity<Map<String, Object>> updateDetails(
      @RequestBody OrdersDetailListUpdateRequest payload) {
    try {
      int[] result = generalService.processMassiveDetailUpdate(payload);

      int totalActualizados = Arrays.stream(result).sum();

      Map<String, Object> data =
          Map.of(
              "totalEnviados", payload.getDetalles().size(),
              "totalActualizados", totalActualizados,
              "resultadoBatch", result);

      return ResponseJsonUtils.buildSuccessWithData("Actualización de detalles exitosa", data);

    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  // actualizacion de estado de cotizaciones
  @PostMapping("update-quotes-status")
  public ResponseEntity<Map<String, Object>> updateQuotesStatus(
      @RequestBody List<QuoteHeaderRequest> payload) {
    try {
      int[] result = generalService.processMassiveQuoteHeaderUpdate(payload);
      int totalActualizados = Arrays.stream(result).sum();
      Map<String, Object> data =
          Map.of(
              "totalEnviados", payload.size(),
              "totalActualizados", totalActualizados,
              "resultadoBatch", result);

      return ResponseJsonUtils.buildSuccessWithData(
          "Actualización de estado de cotizaciones exitosa", data);

    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }
}
