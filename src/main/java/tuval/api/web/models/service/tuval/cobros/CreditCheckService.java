package tuval.api.web.models.service.tuval.cobros;


import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;

public interface CreditCheckService {

    public CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania);
    
} 
