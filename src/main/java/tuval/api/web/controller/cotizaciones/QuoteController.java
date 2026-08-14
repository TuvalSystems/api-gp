package tuval.api.web.controller.cotizaciones;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.cotizaciones.ItemDescripcionAlternativaDto;
import tuval.api.web.models.dto.cotizaciones.SalesProjectReasonDto;
import tuval.api.web.models.entity.dynamics.cotizaciones.QuoteRecordEntity;
import tuval.api.web.models.request.cotizaciones.SalesProjectReasonRequest;
import tuval.api.web.models.service.dynamics.cotizaciones.quoteRecordService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/cotizaciones", produces = "application/json")
public class QuoteController {

    private final quoteRecordService quoteRecordService;


    @PostMapping("/guardar-cotizacion-recordatorio")
    public ResponseEntity<String> saveCotizacionRecordatorio(@RequestBody QuoteRecordEntity entity) {
        quoteRecordService.save(entity);
        return ResponseEntity.ok("Registro guardado correctamente");
    }

    @GetMapping("/lista-items-alternativos")
    public ResponseEntity<Map<String, Object>> getListAlternativeItems(@RequestParam String item) {
        try {
            List<ItemDescripcionAlternativaDto> resp =  quoteRecordService.listAlternativeItems(item);
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
            SalesProjectReasonDto resp = quoteRecordService.getSalesProjectReasonByCoMotproven(coMotproven);
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
