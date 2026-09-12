package tuval.api.web.cobranzas.cobros.dao.tuval;

import java.util.List;
import tuval.api.web.cobranzas.cobros.dto.BusinessDaysDto;
import tuval.api.web.cobranzas.cobros.dto.CheckValidatorDto;
import tuval.api.web.cobranzas.cobros.dto.ClientOverdueDocumentDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerPendingApplicationDocumentDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerResumenAccountDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerStatusAccountDto;
import tuval.api.web.cobranzas.cobros.entity.tuval.ClientSinSoporteEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ClientStatusAccountDetailEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ClientStatusAccountHeaderEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ListClientOverdueDocumentEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ListaClientSinSoporteEntity;

public interface CreditCheckDao {
  CheckValidatorDto getConsultCreditValidation(String ruc, String idCompania, int modo);

  ClientOverdueDocumentDto executeSpConsultExpiredClientDocument(
      String ruc, String idCompania, int modo);

  ListClientOverdueDocumentEntity executeSpConsultExpiredClientDocumentList(
      String ruc, String idCompania, int modo);

  ClientSinSoporteEntity executeSpConsultDocumentClientDontSupport(
      String ruc, String idCompania, int modo);

  ListaClientSinSoporteEntity executeSpConsultDocumentClientDontSupport2(
      String ruc, String idCompania, int modo);

  ClientStatusAccountHeaderEntity executeSpConsultClientStatusAccountHeader(String ruc, int modo);

  List<ClientStatusAccountDetailEntity> executeSpConsultClientStatusAccountDetail(
      String ruc, int modo);

  BusinessDaysDto executeSpGetBusinessDaysCheck(String fecha, int modo);

  List<CustomerPendingApplicationDocumentDto> executeSpGetApplyPendingPayment(String ruc, int modo);
 List<CustomerStatusAccountDto> executeSpGetCustomerStatusAccount(String ruc, int modo);
 CustomerResumenAccountDto executeSpGetCustomerResumenAccount(String ruc, int modo);

}
