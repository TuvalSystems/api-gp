package tuval.api.web.models.dao.tuval.cobros;
import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
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
}
