package tuval.api.web.models.service.tuval.cobros;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface CreditCheckService {

    public ResponseEntity<Map<String, Object>> getConsultCreditValidation(String ruc, String idCompania);
    
} 
