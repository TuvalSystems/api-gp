package tuval.api.web.models.service.tuval.devoluciones;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface InvoiceReturnService {

    ResponseEntity<Map<String, Object>> generacionNotaCreditoVTA( Map<String, Object>  cabecera, List<Map<String, Object>>  detalle);

    ResponseEntity<Map<String, Object>> getResponseCreditNote(String document, String tipo);
} 
