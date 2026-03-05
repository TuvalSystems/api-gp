package tuval.api.web.models.service.tuval.transferencia;

import java.util.List;

import tuval.api.web.models.dto.tranferencias.RepositionDetailDto;
import tuval.api.web.models.dto.tranferencias.RepositionHeaderDto;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.request.bodegas.reposiciones.DetailRepositionRequest;
import tuval.api.web.models.request.bodegas.reposiciones.HeaderRepositionRequest;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;

public interface TransferenciaService {

  List<RepositionHeaderDto> consultaCabeceraTransferencia(
      HeaderRepositionRequest req);

  List<RepositionDetailDto> consultarDetalleTransferencia(DetailRepositionRequest req);

  TransfersCompartmentsOrderDto transferenciaCompartimentoService(
      TransferCompartmentOrdersRequest transfers);
}
