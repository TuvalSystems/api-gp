package tuval.api.web.cobranzas.cobros.service.tuval;

import java.util.List;
import tuval.api.web.cobranzas.cobros.dto.BusinessDaysDto;
import tuval.api.web.cobranzas.cobros.dto.CheckValidatorDto;
import tuval.api.web.cobranzas.cobros.dto.ClientOverdueDocumentDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerPendingApplicationDocumentDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerResumenAccountDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerStatusAccountDto;
import tuval.api.web.cobranzas.cobros.entity.tuval.ClientSinSoporteEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ClientStatusAccountResponseEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ListClientOverdueDocumentEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ListaClientSinSoporteEntity;

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
