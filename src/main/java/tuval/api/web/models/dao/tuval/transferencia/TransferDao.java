package tuval.api.web.models.dao.tuval.transferencia;

import java.sql.Date;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailResponseEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderResponseEntity;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;

public interface TransferDao {

  TransferHeaderResponseEntity consultDocumentTransfer(
      Date date, String userid, int Modo, String numDoc);

  TransferDetailResponseEntity consultarDocumentodetalleTransferencia(
      String userid, int Modo, String numDoc);

  TransfersCompartmentsOrderDto TransferenciaCompartimento(
      TransferCompartmentOrdersRequest transfers);
}
