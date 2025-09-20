package tuval.api.web.models.service.tuval.cobros.Imp;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.cobros.CreditCheckDao;
import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;
import tuval.api.web.models.service.tuval.cobros.CreditCheckService;

@Service
public class CreditCheckServiceImp implements CreditCheckService {

    @Autowired
    private CreditCheckDao creditCheckDao;

    public ClientOverdueDocumentEntity getConsultExpiredClientDocument(String ruc, String idCompania) {
        int modo = 1;
        return creditCheckDao.executeSpConsultExpiredClientDocument(ruc, idCompania, modo);
    }

    public CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania) {
        int modo = 3;
        return creditCheckDao.getConsultCreditValidation(ruc, idCompania, modo);
    }


    public ClientSinSoporteEntity getConsultDocumentClientDontSupport(String ruc, String idCompania) {
        int modo = 2;
        return creditCheckDao.executeSpConsultDocumentClientDontSupport(ruc, idCompania, modo);
    }

    
    public ListaClientSinSoporteEntity getConsultDocumentClientDontSupport2(String ruc, String idCompania) {
        int modo = 2;
        return creditCheckDao.executeSpConsultDocumentClientDontSupport2(ruc, idCompania, modo);
    }


    


}
