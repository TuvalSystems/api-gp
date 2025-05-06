package tuval.api.web.models.dao.tuval.guiaremision;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface ShippingGuideDao {

    public ResponseEntity<Map<String, Object>> checkGuiaRemision(String numDoc);
} 
