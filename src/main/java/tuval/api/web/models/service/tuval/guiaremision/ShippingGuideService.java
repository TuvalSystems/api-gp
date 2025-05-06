package tuval.api.web.models.service.tuval.guiaremision;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface ShippingGuideService {
    
    public ResponseEntity<Map<String, Object>> checkGuiaRemisionService(String numDoc);
}
