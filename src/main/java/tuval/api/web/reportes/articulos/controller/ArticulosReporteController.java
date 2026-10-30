package tuval.api.web.reportes.articulos.controller;

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
import tuval.api.web.reportes.articulos.request.ArticulosReporteRequest;
import tuval.api.web.reportes.articulos.service.dynamics.ArticulosReporteService;

@RestController
@RequestMapping(value = "/api/reportes/articulos", produces = "application/json")
public class ArticulosReporteController {
  private final ArticulosReporteService service;

  public ArticulosReporteController(ArticulosReporteService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> consultar(
      @Valid @ModelAttribute ArticulosReporteRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", service.consultar(request));
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (Exception exception) {
      return ResponseJsonUtils.buildError("No se pudo consultar el reporte de artículos");
    }
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parámetros inválidos del reporte de artículos");
  }
}
