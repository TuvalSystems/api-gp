package tuval.api.web.models.service.tuval.cobros.Imp;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.models.dao.tuval.cobros.CreditCheckDao;
import tuval.api.web.models.dto.cobros.BusinessDaysDto;
import tuval.api.web.models.dto.cobros.CheckValidatorDto;
import tuval.api.web.models.dto.cobros.ClientOverdueDocumentDto;
import tuval.api.web.models.dto.cobros.CustomerPendingApplicationDocumentDto;
import tuval.api.web.models.dto.cobros.CustomerResumenAccountDto;
import tuval.api.web.models.dto.cobros.CustomerStatusAccountDto;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountDetailEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountHeaderEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountResponseEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;
import tuval.api.web.models.service.tuval.cobros.CreditCheckService;

@Service
@RequiredArgsConstructor
public class CreditCheckServiceImp implements CreditCheckService {

  private final CreditCheckDao creditCheckDao;

  @Override
  public ClientOverdueDocumentDto getConsultExpiredClientDocument(String ruc, String idCompania) {
    int modo = 1;
    return creditCheckDao.executeSpConsultExpiredClientDocument(ruc, idCompania, modo);
  }

  @Override
  public ListClientOverdueDocumentEntity getConsultExpiredClientDocumentList(
      String ruc, String idCompania) {
    int modo = 1;
    return creditCheckDao.executeSpConsultExpiredClientDocumentList(ruc, idCompania, modo);
  }

  @Override
  public CheckValidatorDto getConsultCreditValidation(String ruc, String idCompania) {
    int modo = 3;
    return creditCheckDao.getConsultCreditValidation(ruc, idCompania, modo);
  }

  @Override
  public ClientSinSoporteEntity getConsultDocumentClientDontSupport(String ruc, String idCompania) {
    int modo = 2;
    return creditCheckDao.executeSpConsultDocumentClientDontSupport(ruc, idCompania, modo);
  }

  @Override
  public ListaClientSinSoporteEntity getConsultDocumentClientDontSupport2(
      String ruc, String idCompania) {
    int modo = 2;
    return creditCheckDao.executeSpConsultDocumentClientDontSupport2(ruc, idCompania, modo);
  }

  @Override
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

  @Override
  public BusinessDaysDto getBusinessDaysCheck(String fecha) {
    int modo = 14;
    return creditCheckDao.executeSpGetBusinessDaysCheck(fecha, modo);
  }

  @Override
  public List<CustomerPendingApplicationDocumentDto> getPendingApplicationDocuments(String ruc) {
    return creditCheckDao.executeSpGetApplyPendingPayment(ruc, 9);
  }

  @Override
  public List<CustomerStatusAccountDto> getCustomerStatusAccount(String ruc) {
    return creditCheckDao.executeSpGetCustomerStatusAccount(ruc, 11);
  }

 @Override
  public CustomerResumenAccountDto getCustomerResumenAccount(String ruc){
    return creditCheckDao.executeSpGetCustomerResumenAccount(ruc, 12);
  }
}
