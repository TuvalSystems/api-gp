package tuval.api.web.models.service.tuval.cobros;

import tuval.api.web.models.dto.cobros.BusinessDaysDto;
import tuval.api.web.models.dto.cobros.CheckValidatorDto;
import tuval.api.web.models.dto.cobros.ClientOverdueDocumentDto;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountResponseEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;

public interface CreditCheckService {

  public CheckValidatorDto getConsultCreditValidation(String ruc, String idCompania);

  public ClientOverdueDocumentDto getConsultExpiredClientDocument(String ruc, String idCompania);

  public ListClientOverdueDocumentEntity getConsultExpiredClientDocumentList(
      String ruc, String idCompania);

  public ClientSinSoporteEntity getConsultDocumentClientDontSupport(String ruc, String idCompania);

  public ListaClientSinSoporteEntity getConsultDocumentClientDontSupport2(
      String ruc, String idCompania);

  public ClientStatusAccountResponseEntity getConsultaEstadoCuentaDetalleCliente(String ruc);


  public BusinessDaysDto getBusinessDaysCheck(String fecha);
}
