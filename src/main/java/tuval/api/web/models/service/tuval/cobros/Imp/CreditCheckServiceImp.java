package tuval.api.web.models.service.tuval.cobros.Imp;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.cobros.CreditCheckDao;
import tuval.api.web.models.service.tuval.cobros.CreditCheckService;

@Service
public class CreditCheckServiceImp implements CreditCheckService {
    
    @Autowired
    private CreditCheckDao creditCheckDao;


    public  ResponseEntity<Map<String, Object>> getConsultCreditValidation(String ruc, String idCompania) {
        int modo = 3;
        return creditCheckDao.getConsultCreditValidation(ruc, idCompania, modo);
    }
}
