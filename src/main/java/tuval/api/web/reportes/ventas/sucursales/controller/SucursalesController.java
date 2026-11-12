package tuval.api.web.reportes.ventas.sucursales.controller;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.reportes.ventas.sucursales.request.VentasComparativasRequest;
import tuval.api.web.reportes.ventas.sucursales.service.dynamics.SucursalesService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/reportes/ventas/sucursales", produces = "application/json")
public class SucursalesController {
  private final SucursalesService sucursalesService;

  @GetMapping("/comparativo")
  public ResponseEntity<Map<String, Object>> consultarComparativo(
      @Valid @ModelAttribute VentasComparativasRequest request) {
    try {
      return ResponseJsonUtils.buildSuccessWithData(
          "Respuesta exitosa", sucursalesService.consultarComparativo(request));
    } catch (IncorrectResultSizeDataAccessException exception) {
      return ResponseJsonUtils.buildNoFound(
          "El reporte supera el máximo de 500 filas; reduzca las sucursales consultadas");
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (Exception exception) {
      log.error("No se pudo consultar el reporte spVen11", exception);
      return ResponseJsonUtils.buildError("No se pudo consultar el reporte spVen11");
    }
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parámetros inválidos del reporte spVen11");
  }
}
