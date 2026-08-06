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
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionCashInvoiceRequest;
import tuval.api.web.models.service.tuval.cobros.CollectionRegisterService;

@RestController
@RequestMapping(value = "api/cobranzas/cobros-facturas", produces = "application/json")
@RequiredArgsConstructor
public class CollectionRegisterController {

  private final CollectionRegisterService collectionRegisterService;

  // registrar efectivo
  @PostMapping("registrar-efectivo")
  public ResponseEntity<Map<String, Object>> registerCashCollection(
      @Valid @RequestBody CollectionCashInvoiceRequest payload) {
    try {
      String message = collectionRegisterService.registerCashCollection(payload);
      return ResponseJsonUtils.buildSuccess(message);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }
}
