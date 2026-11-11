package tuval.api.web.reportes.inventario.varianza.controller;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.reportes.inventario.varianza.request.VarianzaRequest;
import tuval.api.web.reportes.inventario.varianza.service.dynamics.VarianzaService;

@RestController
@Slf4j
@RequestMapping(value = "/api/reportes/inventario/varianza", produces = "application/json")
public class VarianzaController {
  private final VarianzaService service;

  public VarianzaController(VarianzaService service) {
    this.service = service;
  }

  @GetMapping("/listado-trabajo")
  public ResponseEntity<Map<String, Object>> consultar(
      @Valid @ModelAttribute VarianzaRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", service.consultar(request));
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (Exception exception) {
      log.error("No se pudo consultar el reporte spInv60", exception);
      return ResponseJsonUtils.buildError("No se pudo consultar el reporte spInv60");
    }
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parametros invalidos del reporte spInv60");
  }
}
