package tuval.api.web.models.dao.tuval.cobros;
import java.util.List;

import tuval.api.web.models.dto.cobros.BusinessDaysDto;
import tuval.api.web.models.dto.cobros.CheckValidatorDto;
import tuval.api.web.models.dto.cobros.ClientOverdueDocumentDto;
import tuval.api.web.models.dto.cobros.CollectionSummaryDTO;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountDetailEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountHeaderEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;

import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;

public interface CreditCheckDao {
    public CheckValidatorDto getConsultCreditValidation(String ruc, String idCompania, int modo);

    public ClientOverdueDocumentDto executeSpConsultExpiredClientDocument(String ruc, String idCompania, int modo);
     public ListClientOverdueDocumentEntity executeSpConsultExpiredClientDocumentList(
      String ruc, String idCompania, int modo);

    public ClientSinSoporteEntity executeSpConsultDocumentClientDontSupport(String ruc, String idCompania, int modo);

    public ListaClientSinSoporteEntity executeSpConsultDocumentClientDontSupport2(
      String ruc, String idCompania, int modo);

    public ClientStatusAccountHeaderEntity executeSpConsultClientStatusAccountHeader(String ruc, int modo);
    public List<ClientStatusAccountDetailEntity> executeSpConsultClientStatusAccountDetail(String ruc, int modo);
    

    public BusinessDaysDto executeSpGetBusinessDaysCheck(String fecha, int modo);
     public List<CollectionSummaryDTO> executeSpGetCollectionSumaryClientDao(String ruc, int modo);
}
