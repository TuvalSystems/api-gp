package tuval.api.web.reportes.resolucionarticulos.controller;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.reportes.resolucionarticulos.request.ResolucionArticulosReporteRequest;
import tuval.api.web.reportes.resolucionarticulos.service.dynamics.ResolucionArticulosReporteService;

@RestController
@RequestMapping(value = "/api/reportes/resolucion-articulos", produces = "application/json")
public class ResolucionArticulosReporteController {
  private final ResolucionArticulosReporteService service;

  public ResolucionArticulosReporteController(ResolucionArticulosReporteService service) {
    this.service = service;
  }

  @PostMapping(value = "/consultar", consumes = "application/json")
  public ResponseEntity<Map<String, Object>> consultar(
      @Valid @RequestBody ResolucionArticulosReporteRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", service.consultar(request));
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (Exception exception) {
      return ResponseJsonUtils.buildError("No se pudo resolver la consulta de articulos");
    }
  }

  @ExceptionHandler({BindException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parametros invalidos de resolucion de articulos");
  }
}
