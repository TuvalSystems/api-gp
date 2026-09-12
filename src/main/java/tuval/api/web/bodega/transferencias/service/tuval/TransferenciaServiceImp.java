package tuval.api.web.bodega.transferencias.service.tuval;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import tuval.api.web.bodega.transferencias.dao.tuval.TransferDao;
import tuval.api.web.bodega.transferencias.dto.RepositionDetailDto;
import tuval.api.web.bodega.transferencias.dto.RepositionHeaderDto;
import tuval.api.web.bodega.transferencias.dto.TransfersCompartmentsOrderDto;
import tuval.api.web.bodega.transferencias.request.DetailRepositionRequest;
import tuval.api.web.bodega.transferencias.request.HeaderRepositionRequest;
import tuval.api.web.bodega.transferencias.request.TransferCompartmentOrdersRequest;

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
