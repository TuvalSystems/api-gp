package tuval.api.web.reportes.inv60.controller;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.reportes.inv60.request.Inv60ReporteRequest;
import tuval.api.web.reportes.inv60.service.dynamics.Inv60ReporteService;

@RestController
@RequestMapping(value = "/api/reportes/listado-varianzas-trabajo", produces = "application/json")
public class Inv60ReporteController {
  private final Inv60ReporteService service;

  public Inv60ReporteController(Inv60ReporteService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> consultar(
      @Valid @ModelAttribute Inv60ReporteRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", service.consultar(request));
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (Exception exception) {
      return ResponseJsonUtils.buildError("No se pudo consultar el reporte spInv60");
    }
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parametros invalidos del reporte spInv60");
  }
}
