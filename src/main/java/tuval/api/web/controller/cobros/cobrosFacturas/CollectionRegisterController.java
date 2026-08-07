package tuval.api.web.controller.cobros.cobrosFacturas;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionDefaultInvoiceRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionDetailInvoicesRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionRetentionInvoiceRequest;
import tuval.api.web.models.service.tuval.cobros.CollectionRegisterService;

@RestController
@RequestMapping(value = "api/cobranzas/cobros-facturas", produces = "application/json")
@RequiredArgsConstructor
public class CollectionRegisterController {

  private final CollectionRegisterService collectionRegisterService;

  // registrar efectivo
  @PostMapping("registrar-efectivo")
  public ResponseEntity<Map<String, Object>> registerCashCollection(
      @Valid @RequestBody CollectionDefaultInvoiceRequest payload) {
    try {
      String message = collectionRegisterService.registerCashCollection(payload);
      return ResponseJsonUtils.buildSuccess(message);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @PostMapping("registrar-aplicacion")
  public ResponseEntity<Map<String, Object>> registerApplicationCollection(
      @Valid @RequestBody CollectionDetailInvoicesRequest payload) {
    try {
      String message = collectionRegisterService.registerApplicationCollection(payload);
      return ResponseJsonUtils.buildSuccess(message);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @PostMapping("registrar-retencion")
  public ResponseEntity<Map<String, Object>> registerRetentionCollection(
     @Valid @RequestBody CollectionRetentionInvoiceRequest payload) {

    try {
      String message = collectionRegisterService.registerRetentionCollection(payload);
      return ResponseJsonUtils.buildSuccess(message);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }
}
