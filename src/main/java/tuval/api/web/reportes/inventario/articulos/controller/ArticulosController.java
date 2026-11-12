package tuval.api.web.reportes.inventario.articulos.controller;

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
import tuval.api.web.reportes.inventario.articulos.request.ArticulosRequest;
import tuval.api.web.reportes.inventario.articulos.service.dynamics.ArticulosService;

@RestController
@Slf4j
@RequestMapping(value = "/api/reportes/inventario/articulos", produces = "application/json")
public class ArticulosController {
  private final ArticulosService service;

  public ArticulosController(ArticulosService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> consultar(
      @Valid @ModelAttribute ArticulosRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", service.consultar(request));
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (Exception exception) {
      log.error("No se pudo consultar el reporte de artículos", exception);
      return ResponseJsonUtils.buildError("No se pudo consultar el reporte de artículos");
    }
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parámetros inválidos del reporte de artículos");
  }
}
