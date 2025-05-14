package tuval.api.web.models.dao.tuval.cobros;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface CreditCheckDao {
    public ResponseEntity<Map<String, Object>> getConsultCreditValidation(String ruc, String idCompania, int modo);
}
