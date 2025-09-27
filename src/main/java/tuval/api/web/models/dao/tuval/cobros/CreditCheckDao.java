package tuval.api.web.models.dao.tuval.cobros;
import java.util.List;

import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountDetailEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountHeaderEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;

import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;

public interface CreditCheckDao {
    public CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania, int modo);

    public ClientOverdueDocumentEntity executeSpConsultExpiredClientDocument(String ruc, String idCompania, int modo);
     public ListClientOverdueDocumentEntity executeSpConsultExpiredClientDocumentList(
      String ruc, String idCompania, int modo);

    public ClientSinSoporteEntity executeSpConsultDocumentClientDontSupport(String ruc, String idCompania, int modo);

    public ListaClientSinSoporteEntity executeSpConsultDocumentClientDontSupport2(
      String ruc, String idCompania, int modo);

    public ClientStatusAccountHeaderEntity executeSpConsultClientStatusAccountHeader(String ruc, int modo);
    public List<ClientStatusAccountDetailEntity> executeSpConsultClientStatusAccountDetail(String ruc, int modo);
    
}
