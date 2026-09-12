package tuval.api.web.bodega.transferencias.service.tuval;

import java.util.List;

import tuval.api.web.bodega.transferencias.dto.RepositionDetailDto;
import tuval.api.web.bodega.transferencias.dto.RepositionHeaderDto;
import tuval.api.web.bodega.transferencias.dto.TransfersCompartmentsOrderDto;
import tuval.api.web.bodega.transferencias.request.DetailRepositionRequest;
import tuval.api.web.bodega.transferencias.request.HeaderRepositionRequest;
import tuval.api.web.bodega.transferencias.request.TransferCompartmentOrdersRequest;

public interface TransferenciaService {

  List<RepositionHeaderDto> consultaCabeceraTransferencia(
      HeaderRepositionRequest req);

  List<RepositionDetailDto> consultarDetalleTransferencia(DetailRepositionRequest req);

  TransfersCompartmentsOrderDto transferenciaCompartimentoService(
      TransferCompartmentOrdersRequest transfers);
}
