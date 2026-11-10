package tuval.api.web.reportes.listadoitems.controller;

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
import tuval.api.web.reportes.listadoitems.request.ListadoItemsReporteRequest;
import tuval.api.web.reportes.listadoitems.service.dynamics.ListadoItemsReporteService;

@RestController
@RequestMapping(value = "/api/reportes/listado-items", produces = "application/json")
public class ListadoItemsReporteController {
  private final ListadoItemsReporteService service;

  public ListadoItemsReporteController(ListadoItemsReporteService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> consultar(
      @Valid @ModelAttribute ListadoItemsReporteRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", service.consultar(request));
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (Exception exception) {
      return ResponseJsonUtils.buildError("No se pudo consultar el listado de items");
    }
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parametros invalidos del listado de items");
  }
}
