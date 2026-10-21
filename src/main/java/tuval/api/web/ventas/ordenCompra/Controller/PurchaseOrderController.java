package tuval.api.web.ventas.ordenCompra.Controller;

import java.util.Map;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.ventas.ordenCompra.dao.dynamics.PurchaseOrderDao;
import tuval.api.web.ventas.ordenCompra.request.CreatePurchaseOrderRequest;

@Slf4j
@RestController
@RequestMapping(value = "/api/ventas/orden-compra", produces = "application/json")
public class PurchaseOrderController {
  private final PurchaseOrderDao purchaseOrderDao;

  public PurchaseOrderController(PurchaseOrderDao purchaseOrderDao) {
    this.purchaseOrderDao = purchaseOrderDao;
  }

  @PostMapping(value = "/crear-orden-compra-guia", consumes = "application/json")
  public ResponseEntity<Map<String, Object>> create(
      @Valid @RequestBody CreatePurchaseOrderRequest request) {
    try {
      var result = purchaseOrderDao.create(request);
      if (result == null || result.resultado() == null
          || result.ordenGenerada() == null || result.ordenGenerada().isBlank()) {
        return ResponseJsonUtils.buildNoFound("El procedimiento no devolvio una orden de compra generada");
      }
      return ResponseJsonUtils.buildSuccessWithData("Orden de compra creada correctamente", result);
    } catch (DataAccessException e) {
      return ResponseJsonUtils.buildError("No se pudo crear la orden de compra en Dynamics");
    }
  }
}
