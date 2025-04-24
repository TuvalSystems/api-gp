package tuval.api.web.models.service.tuval.devoluciones.Utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class InvoiceUtils {
    
    
    public static ResponseEntity<Map<String, Object>> buildError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @SuppressWarnings("unchecked")
    public static String getStringFromMap(Object obj, String key) {
        if (obj instanceof Map<?, ?> map) {
            try {
                // Intentamos hacer cast seguro a Map<String, Object>
                Map<String, Object> castedMap = (Map<String, Object>) map;
                Object value = castedMap.get(key);
                return value != null ? value.toString() : null;
            } catch (ClassCastException e) {
                // Si no es un Map con claves String, lo ignoramos
                return null;
            }
        }
        return null;
    }

    public static boolean isSuccess(ResponseEntity<Map<String, Object>> response) {
        if (response != null && response.getBody() != null) {
            Object success = (response.getBody() != null) ? response.getBody().get("success") : null;
            return success instanceof Boolean && (Boolean) success;
        }
        return false;
    }

    public static ResponseEntity<Map<String, Object>> buildException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    public static String getRequiredField(Map<String, Object> map, String key, String code, String msg) throws Exception {
        String value = getStringFromMap(map, key);
        if (value == null || value.isEmpty()) {
            throw new Exception("(" + code + ") " + msg);
        }
        return value;
    }

}
