package tuval.api.web.controller.guiaremision;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.service.tuval.guiaremision.ShippingGuideService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping(value = "/api" , produces = "application/json")
public class ShippingGuideController {
    
    @Autowired
    private ShippingGuideService shippingGuideService;

    @CrossOrigin
    @GetMapping("/check-guia-remision")
    public ResponseEntity<Map<String, Object>> respuestaGuiaRemision(@RequestParam String numDoc) {
        if (numDoc == null || numDoc.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Document number is required."));
        }
      
       ResponseEntity<Map<String, Object>> result = shippingGuideService.checkGuiaRemisionService(numDoc);
       Map<String, Object> response = new HashMap<>();
       if (result.getBody() != null) {
           response.putAll(result.getBody());
       }
       
       return ResponseEntity.ok(response);
        
    }
    
}
