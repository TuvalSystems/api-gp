package tuval.api.web.controller.guiaremision;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.bodegas.guiaRemision.DispatchVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.TransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.service.tuval.guiaremision.ShippingGuideService;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class ShippingGuideController {

  @Autowired private ShippingGuideService shippingGuideService;

  @CrossOrigin
  @GetMapping("/inventario/guia-remision-obtener-claveacceso")
  public ResponseEntity<Map<String, Object>> respuestaGuiaRemision(@RequestParam String numDoc) {
    if (numDoc == null || numDoc.trim().isEmpty()) {
      return ResponseJsonUtils.buildError("El documento es requerido.");
    }
    try {
      CheckCodeAccessDeliveryNoteResponse result =
          shippingGuideService.checkGuiaRemisionService(numDoc);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  /** proceso de guias de remision - traslado de inventario --- pendiente de programar */
  @CrossOrigin
  @PostMapping("/inventario/guardar-generar-movimiento-transferencia-venta")
  public ResponseEntity<Map<String, Object>> generarTranasferenciaVenta(
      @Valid @RequestBody TransferVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response =
          shippingGuideService.generateGuideTransferVtaService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("/inventario/guardar-generar-movimiento-reposicion")
  public ResponseEntity<Map<String, Object>> generarDepachoReposicion(
      @Valid @RequestBody TransferVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response =
          shippingGuideService.generateGuideReposicionService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("/inventario/guardar-generar-movimiento-despacho")
  public ResponseEntity<Map<String, Object>> generarDespachoVta(
      @Valid @RequestBody DispatchVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response =
          shippingGuideService.generateGuideDispatchVtaService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  // validar existencia de ajuste de guia de remision
  @CrossOrigin
  @GetMapping("/inventario/validar-existencia-guia-despacho")
  public ResponseEntity<Map<String, Object>> postMethodName(
      @Valid @RequestParam String orderNumber) {

    try {
      HeaderCheckGuideDispatchResponse resp =
          shippingGuideService.checkAjusteGuideDispatchVtaService(orderNumber);

      if (resp == null) {
        return ResponseJsonUtils.buildNoFound("No existe guía para mc_numdoc=" + orderNumber);
      }
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);

    } catch (Exception e) {
      e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }
}
