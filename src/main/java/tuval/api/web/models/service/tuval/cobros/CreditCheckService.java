package tuval.api.web.models.service.tuval.cobros;


import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.pruebaEntity;

public interface CreditCheckService {

    public CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania);

    public ClientOverdueDocumentEntity getConsultExpiredClientDocument(String ruc, String idCompania);
    

    public pruebaEntity funcionprueba();
} 
