package tuval.api.web.reportes.ven11.controller;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.reportes.ven11.request.Ven11ReporteRequest;
import tuval.api.web.reportes.ven11.service.dynamics.Ven11ReporteService;

@RestController
@RequestMapping(value = "/api/reportes/ven11", produces = "application/json")
public class Ven11ReporteController {
  private final Ven11ReporteService service;

  public Ven11ReporteController(Ven11ReporteService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> consultar(
      @Valid @ModelAttribute Ven11ReporteRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", service.consultar(request));
    } catch (IncorrectResultSizeDataAccessException exception) {
      return ResponseJsonUtils.buildNoFound(
          "El reporte supera el máximo de 500 filas; reduzca las sucursales consultadas");
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (Exception exception) {
      return ResponseJsonUtils.buildError("No se pudo consultar el reporte spVen11");
    }
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parámetros inválidos del reporte spVen11");
  }
}
