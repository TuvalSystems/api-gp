package tuval.api.web.models.service.tuval.cobros;


import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;

public interface CreditCheckService {

    public CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania);

    public ClientOverdueDocumentEntity getConsultExpiredClientDocument(String ruc, String idCompania);
      public ListClientOverdueDocumentEntity getConsultExpiredClientDocumentList(String ruc, String idCompania);

    public ClientSinSoporteEntity getConsultDocumentClientDontSupport(String ruc, String idCompania);
    
     public ListaClientSinSoporteEntity getConsultDocumentClientDontSupport2(String ruc, String idCompania);
    
} 
