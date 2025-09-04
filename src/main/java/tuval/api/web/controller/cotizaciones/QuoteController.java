package tuval.api.web.controller.cotizaciones;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import tuval.api.web.models.entity.dynamics.cotizaciones.QuoteRecordEntity;
import tuval.api.web.models.service.dynamics.cotizaciones.quoteRecordService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping(value = "/api/cotizaciones", produces = "application/json")
public class QuoteController {

    @Autowired
    private quoteRecordService quoteRecordService;

    @CrossOrigin
    @PostMapping("/guardar-cotizacion-recordatorio")
    public ResponseEntity<String> saveCotizacionRecordatorio(@RequestBody QuoteRecordEntity entity) {
        quoteRecordService.save(entity);
        return ResponseEntity.ok("Registro guardado correctamente");
    }

    @CrossOrigin
    @GetMapping("/lista-items-alternativos")
    public ResponseEntity<Map<String, Object>> getListAlternativeItems(@RequestParam String item) {
        return quoteRecordService.listAlternativeItems(item);
    }

}
