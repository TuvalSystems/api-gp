package tuval.api.web.models.service.tuval.cobros.Imp;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tuval.api.web.models.dao.tuval.cobros.CreditCheckDao;
import tuval.api.web.models.dto.cobros.BusinessDaysDto;
import tuval.api.web.models.dto.cobros.CheckValidatorDto;
import tuval.api.web.models.dto.cobros.ClientOverdueDocumentDto;
import tuval.api.web.models.dto.cobros.CollectionSummaryDTO;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountDetailEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountHeaderEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountResponseEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;
import tuval.api.web.models.service.tuval.cobros.CreditCheckService;

@Service
public class CreditCheckServiceImp implements CreditCheckService {

  private final CreditCheckDao creditCheckDao;

  public CreditCheckServiceImp(CreditCheckDao creditCheckDao) {
    this.creditCheckDao = creditCheckDao;
  }

  public ClientOverdueDocumentDto getConsultExpiredClientDocument(String ruc, String idCompania) {
    int modo = 1;
    return creditCheckDao.executeSpConsultExpiredClientDocument(ruc, idCompania, modo);
  }

  public ListClientOverdueDocumentEntity getConsultExpiredClientDocumentList(
      String ruc, String idCompania) {
    int modo = 1;
    return creditCheckDao.executeSpConsultExpiredClientDocumentList(ruc, idCompania, modo);
  }

  public CheckValidatorDto getConsultCreditValidation(String ruc, String idCompania) {
    int modo = 3;
    return creditCheckDao.getConsultCreditValidation(ruc, idCompania, modo);
  }

  public ClientSinSoporteEntity getConsultDocumentClientDontSupport(String ruc, String idCompania) {
    int modo = 2;
    return creditCheckDao.executeSpConsultDocumentClientDontSupport(ruc, idCompania, modo);
  }

  public ListaClientSinSoporteEntity getConsultDocumentClientDontSupport2(
      String ruc, String idCompania) {
    int modo = 2;
    return creditCheckDao.executeSpConsultDocumentClientDontSupport2(ruc, idCompania, modo);
  }

  public ClientStatusAccountResponseEntity getConsultaEstadoCuentaDetalleCliente(String ruc) {
    ClientStatusAccountHeaderEntity respHeader =
        creditCheckDao.executeSpConsultClientStatusAccountHeader(ruc, 1);
    List<ClientStatusAccountDetailEntity> respDetail =
        creditCheckDao.executeSpConsultClientStatusAccountDetail(ruc, 2);

    ClientStatusAccountResponseEntity resp = new ClientStatusAccountResponseEntity();
    resp.setCabecera(respHeader);
    resp.setDetalle(respDetail);
    return resp;
  }

  public BusinessDaysDto getBusinessDaysCheck(String fecha) {
    int modo = 14;
    return creditCheckDao.executeSpGetBusinessDaysCheck(fecha, modo);
  }

  public List<CollectionSummaryDTO> getConsultCollectionSummaryService(String ruc, String idCompania) {
   return  creditCheckDao.executeSpGetCollectionSumaryClientDao(ruc, 9);
  }
}
