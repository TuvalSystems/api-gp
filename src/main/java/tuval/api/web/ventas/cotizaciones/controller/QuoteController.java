package tuval.api.web.ventas.cotizaciones.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.ventas.cotizaciones.dto.ItemDescripcionAlternativaDto;
import tuval.api.web.ventas.cotizaciones.dto.SalesProjectReasonDto;
import tuval.api.web.ventas.cotizaciones.entity.dynamics.QuoteRecordEntity;
import tuval.api.web.ventas.cotizaciones.request.SalesProjectReasonRequest;
import tuval.api.web.ventas.cotizaciones.service.dynamics.quoteRecordService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/cotizaciones", produces = "application/json")
public class QuoteController {

  private final quoteRecordService quoteRecordService;

  @PostMapping("/guardar-cotizacion-recordatorio")
  public ResponseEntity<Map<String, Object>> saveCotizacionRecordatorio(
      @RequestBody QuoteRecordEntity entity) {
    try {
      quoteRecordService.save(entity);
      return ResponseJsonUtils.buildSuccess("Registro guardado correctamente");
    } catch (Exception e) {
      return ResponseJsonUtils.buildError(e.getMessage());
    }
  }

  @GetMapping("/lista-items-alternativos")
  public ResponseEntity<Map<String, Object>> getListAlternativeItems(@RequestParam String item) {
    try {
      List<ItemDescripcionAlternativaDto> resp = quoteRecordService.listAlternativeItems(item);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);
    } catch (Exception e) {
      return ResponseJsonUtils.buildError(e.getMessage());
    }
  }

  @GetMapping("lista-proyectos-venta")
  public ResponseEntity<Map<String, Object>> getSalesProjectReasons() {
    try {
      List<SalesProjectReasonDto> resp = quoteRecordService.listSalesProjectReasons();
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);
    } catch (Exception e) {
      return ResponseJsonUtils.buildError(e.getMessage());
    }
  }

  @PostMapping("proyecto-venta")
  public ResponseEntity<Map<String, Object>> getSalesProjectReason(
      @RequestBody SalesProjectReasonRequest request) {
    try {
      if (request == null || request.getCoMotproven() == null) {
        return ResponseJsonUtils.buildNoFound("El parametro coMotproven es requerido");
      }
      Integer coMotproven = request.getCoMotproven();
      SalesProjectReasonDto resp =
          quoteRecordService.getSalesProjectReasonByCoMotproven(coMotproven);
      if (resp == null) {
        return ResponseJsonUtils.buildNoFound(
            "No se encontro proyecto de venta para coMotproven=" + coMotproven);
      }
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);
    } catch (Exception e) {
      return ResponseJsonUtils.buildError(e.getMessage());
    }
  }
}
