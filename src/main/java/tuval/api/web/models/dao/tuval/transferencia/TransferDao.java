package tuval.api.web.models.dao.tuval.transferencia;

import java.util.List;

import tuval.api.web.models.dto.tranferencias.RepositionDetailDto;
import tuval.api.web.models.dto.tranferencias.RepositionHeaderDto;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.request.bodegas.reposiciones.DetailRepositionRequest;
import tuval.api.web.models.request.bodegas.reposiciones.HeaderRepositionRequest;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;

public interface TransferDao {

    public List<RepositionHeaderDto> consultDocumentTransfer(
            HeaderRepositionRequest req, int Modo);

    List<RepositionDetailDto> consultarDocumentodetalleTransferencia(
            DetailRepositionRequest req, int modo);

    TransfersCompartmentsOrderDto transferenciaCompartimento(
            TransferCompartmentOrdersRequest transfers);
}
