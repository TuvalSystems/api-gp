package tuval.api.web.reportes.inventario.articulos.controller;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.reportes.inventario.articulos.request.ArticulosRequest;
import tuval.api.web.reportes.inventario.articulos.request.ListadoItemsRequest;
import tuval.api.web.reportes.inventario.articulos.request.ResolucionArticulosRequest;
import tuval.api.web.reportes.inventario.articulos.service.dynamics.ArticulosService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/reportes/inventario/articulos", produces = "application/json")
public class ArticulosController {
  private final ArticulosService articulosService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> consultar(
      @Valid @ModelAttribute ArticulosRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", articulosService.consultar(request));
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (Exception exception) {
      log.error("No se pudo consultar el reporte de artículos", exception);
      return ResponseJsonUtils.buildError("No se pudo consultar el reporte de artículos");
    }
  }

  @GetMapping("/listado-items")
  public ResponseEntity<Map<String, Object>> consultarListadoItems(
      @Valid @ModelAttribute ListadoItemsRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData(
          "Respuesta exitosa", articulosService.consultarListadoItems(request));
    } catch (IllegalArgumentException exception) {
      return invalidListadoItemsRequest();
    } catch (Exception exception) {
      log.error("No se pudo consultar el listado de items", exception);
      return ResponseJsonUtils.buildError("No se pudo consultar el listado de items");
    }
  }

  @PostMapping(value = "/resolver", consumes = "application/json")
  public ResponseEntity<Map<String, Object>> resolverArticulos(
      @Valid @RequestBody ResolucionArticulosRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData(
          "Respuesta exitosa", articulosService.resolverArticulos(request));
    } catch (IllegalArgumentException exception) {
      return invalidResolucionRequest();
    } catch (Exception exception) {
      log.error("No se pudo resolver la consulta de articulos", exception);
      return ResponseJsonUtils.buildError("No se pudo resolver la consulta de articulos");
    }
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidBody() {
    return invalidResolucionRequest();
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidRequest(BindException exception) {
    if (exception.getTarget() instanceof ResolucionArticulosRequest) {
      return invalidResolucionRequest();
    }
    return exception.getTarget() instanceof ListadoItemsRequest
        ? invalidListadoItemsRequest()
        : invalidRequest();
  }

  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parámetros inválidos del reporte de artículos");
  }

  private ResponseEntity<Map<String, Object>> invalidListadoItemsRequest() {
    return ResponseJsonUtils.buildNoFound("Parametros invalidos del listado de items");
  }

  private ResponseEntity<Map<String, Object>> invalidResolucionRequest() {
    return ResponseJsonUtils.buildNoFound("Parametros invalidos de resolucion de articulos");
  }
}
