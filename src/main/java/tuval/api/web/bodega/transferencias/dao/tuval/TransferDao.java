package tuval.api.web.bodega.transferencias.dao.tuval;

import java.util.List;

import tuval.api.web.bodega.transferencias.dto.RepositionDetailDto;
import tuval.api.web.bodega.transferencias.dto.RepositionHeaderDto;
import tuval.api.web.bodega.transferencias.dto.TransfersCompartmentsOrderDto;
import tuval.api.web.bodega.transferencias.request.DetailRepositionRequest;
import tuval.api.web.bodega.transferencias.request.HeaderRepositionRequest;
import tuval.api.web.bodega.transferencias.request.TransferCompartmentOrdersRequest;

public interface TransferDao {

    public List<RepositionHeaderDto> consultDocumentTransfer(
            HeaderRepositionRequest req, int Modo);

    List<RepositionDetailDto> consultarDocumentodetalleTransferencia(
            DetailRepositionRequest req, int modo);

    TransfersCompartmentsOrderDto transferenciaCompartimento(
            TransferCompartmentOrdersRequest transfers);
}
