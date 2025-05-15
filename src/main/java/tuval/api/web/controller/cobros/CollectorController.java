package tuval.api.web.controller.cobros;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.service.tuval.cobros.CreditCheckService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping(value = "/api/cobros", produces = "application/json")
public class CollectorController {
    
    @Autowired
    private CreditCheckService creditCheckService;

    @GetMapping("/consultar-cupo-disponible-cliente")
    public ResponseEntity<Map<String, Object>> getConsultCreditValidation(@RequestParam String ruc,  @RequestParam(required = false, defaultValue = "") String  idCompania) {
               Map<String, Object> response = new HashMap<>();
               try {
                
                if (ruc == null || ruc.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Falta el campo 'ruc' o está vacío.");
                    return ResponseEntity.badRequest().body(response);
                }

                if (idCompania == null || idCompania.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Falta el campo 'idCompania' o está vacío.");
                    return ResponseEntity.badRequest().body(response);
                    
                }
                ResponseEntity<Map<String, Object>> result = creditCheckService.getConsultCreditValidation(ruc, idCompania);
                return result;
               } catch (Exception e) {
                   response.put("success", false);
                   response.put("message", "Error interno al procesar la solicitud.");
                   response.put("error", e.getMessage());
                   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

               }

    }
    
}
