package tuval.api.web.cobranzas.cobros.controller;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.cobranzas.cobros.request.CollectionCheckInvoiceRequest;
import tuval.api.web.cobranzas.cobros.request.CollectionDefaultInvoiceRequest;
import tuval.api.web.cobranzas.cobros.request.CollectionDetailInvoicesRequest;
import tuval.api.web.cobranzas.cobros.request.CollectionRetentionInvoiceRequest;
import tuval.api.web.cobranzas.cobros.service.tuval.CollectionRegisterService;

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

  @PostMapping("registrar-cheque")
  public ResponseEntity<Map<String, Object>> registerCheckCollection(
      @Valid @RequestBody CollectionCheckInvoiceRequest payload) {
    try {
      String message = collectionRegisterService.registerCheckCollection(payload);
      return ResponseJsonUtils.buildSuccess(message);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }
}
