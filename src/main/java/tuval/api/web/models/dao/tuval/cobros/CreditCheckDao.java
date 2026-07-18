package tuval.api.web.models.dao.tuval.cobros;

import java.util.List;
import tuval.api.web.models.dto.cobros.BusinessDaysDto;
import tuval.api.web.models.dto.cobros.CheckValidatorDto;
import tuval.api.web.models.dto.cobros.ClientOverdueDocumentDto;
import tuval.api.web.models.dto.cobros.CustomerPendingApplicationDocumentDto;
import tuval.api.web.models.dto.cobros.CustomerResumenAccountDto;
import tuval.api.web.models.dto.cobros.CustomerStatusAccountDto;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountDetailEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountHeaderEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;

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
