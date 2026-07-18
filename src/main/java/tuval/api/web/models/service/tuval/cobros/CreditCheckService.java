package tuval.api.web.models.service.tuval.cobros;

import java.util.List;
import tuval.api.web.models.dto.cobros.BusinessDaysDto;
import tuval.api.web.models.dto.cobros.CheckValidatorDto;
import tuval.api.web.models.dto.cobros.ClientOverdueDocumentDto;
import tuval.api.web.models.dto.cobros.CustomerPendingApplicationDocumentDto;
import tuval.api.web.models.dto.cobros.CustomerResumenAccountDto;
import tuval.api.web.models.dto.cobros.CustomerStatusAccountDto;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountResponseEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;

public interface CreditCheckService {

  CheckValidatorDto getConsultCreditValidation(String ruc, String idCompania);

  ClientOverdueDocumentDto getConsultExpiredClientDocument(String ruc, String idCompania);

  ListClientOverdueDocumentEntity getConsultExpiredClientDocumentList(
      String ruc, String idCompania);

  ClientSinSoporteEntity getConsultDocumentClientDontSupport(String ruc, String idCompania);

  ListaClientSinSoporteEntity getConsultDocumentClientDontSupport2(String ruc, String idCompania);

  ClientStatusAccountResponseEntity getConsultaEstadoCuentaDetalleCliente(String ruc);

  BusinessDaysDto getBusinessDaysCheck(String fecha);

  List<CustomerPendingApplicationDocumentDto> getPendingApplicationDocuments(String ruc);

  List<CustomerStatusAccountDto> getCustomerStatusAccount(String ruc);

  CustomerResumenAccountDto getCustomerResumenAccount(String ruc);
}
