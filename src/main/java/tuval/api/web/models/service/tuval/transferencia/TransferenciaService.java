package tuval.api.web.models.service.tuval.transferencia;

import java.util.List;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderEntity;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;

public interface TransferenciaService {

  List<TransferHeaderEntity> consultaCabeceraTransferencia(
      String idCompany, String fecha, String numDoc);

  List<TransferDetailEntity> consultarDetalleTransferencia(String idCompany, String numDoc);

  TransfersCompartmentsOrderDto transferenciaCompartimentoService(
      TransferCompartmentOrdersRequest transfers);
}
