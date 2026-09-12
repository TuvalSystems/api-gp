package tuval.api.web.ventas.devoluciones.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import tuval.api.web.ventas.devoluciones.entity.tuval.RetornoEntity;

@Component
public class InvoiceReturnUtils {
    
    
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


       public static boolean validarDatos(String documento, String pedidoOrigen, String bodega, String usuario, List<RetornoEntity> response) {
        if (documento == null || documento.isBlank()) {
            response.add(new RetornoEntity(false, 10, "No se ha enviado el número de Documento en la cabecera de la transacción."));
            return false;
        }

        if (pedidoOrigen == null || pedidoOrigen.isBlank()) {
            response.add(new RetornoEntity(false, 11, "No se ha enviado el número de pedido original en la cabecera de la transacción."));
            return false;
        }

        if (bodega == null || bodega.isBlank()) {
            response.add(new RetornoEntity(false, 12, "No se ha enviado la bodega en la cabecera de la transacción."));
            return false;
        }

        if (usuario == null || usuario.isBlank()) {
            response.add(new RetornoEntity(false, 13, "No se ha enviado el usuario en la cabecera de la transacción."));
            return false;
        }

        return true;
    }

}
