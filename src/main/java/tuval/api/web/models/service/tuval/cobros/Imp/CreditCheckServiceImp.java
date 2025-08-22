package tuval.api.web.models.service.tuval.cobros.Imp;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.cobros.CreditCheckDao;
import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.service.tuval.cobros.CreditCheckService;

@Service
public class CreditCheckServiceImp implements CreditCheckService {
    
    @Autowired
    private CreditCheckDao creditCheckDao;


    public  CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania) {
        int modo = 3;
        return creditCheckDao.getConsultCreditValidation(ruc, idCompania, modo);
    }
}
