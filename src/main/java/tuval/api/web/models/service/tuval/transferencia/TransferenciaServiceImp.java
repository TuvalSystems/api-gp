package tuval.api.web.models.service.tuval.transferencia;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import tuval.api.web.models.dao.tuval.transferencia.TransferDao;
import tuval.api.web.models.dto.tranferencias.RepositionDetailDto;
import tuval.api.web.models.dto.tranferencias.RepositionHeaderDto;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.request.bodegas.reposiciones.DetailRepositionRequest;
import tuval.api.web.models.request.bodegas.reposiciones.HeaderRepositionRequest;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;

@Service
@RequiredArgsConstructor
public class TransferenciaServiceImp implements TransferenciaService {

  private final TransferDao transferDao;


  @Override
  public List<RepositionHeaderDto> consultaCabeceraTransferencia(
      HeaderRepositionRequest req) {

    return transferDao.consultDocumentTransfer(
        req, 1);

  }
  @Override
  public List<RepositionDetailDto> consultarDetalleTransferencia(DetailRepositionRequest req) {
     return transferDao.consultarDocumentodetalleTransferencia(
          req, 2);
  }
  @Override
  public TransfersCompartmentsOrderDto transferenciaCompartimentoService(
      TransferCompartmentOrdersRequest transfers) {
    return transferDao.transferenciaCompartimento(transfers);
  }
}
