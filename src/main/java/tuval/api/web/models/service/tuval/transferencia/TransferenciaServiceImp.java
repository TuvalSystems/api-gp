package tuval.api.web.models.service.tuval.transferencia;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.models.dao.tuval.transferencia.TransferDao;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailResponseEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderResponseEntity;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;

@Service
@RequiredArgsConstructor
public class TransferenciaServiceImp implements TransferenciaService {

  private final TransferDao transferDao;

  // private  final TransferDynamicDao transferDynamicDao;

  public List<TransferHeaderEntity> consultaCabeceraTransferencia(
      String idCompany, String fecha, String numDoc) {
    List<TransferHeaderEntity> response = new ArrayList<>();

    if (fecha == null || fecha.isEmpty() || idCompany == null || idCompany.isEmpty()) {
      return response;
    }

    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date fechaDate;
      try {
        fechaDate = sdf.parse(fecha);
      } catch (Exception e) {
        fechaDate = new Date();
      }
      // if (numDoc != null && !numDoc.isEmpty()) {
      //     transferDynamicDao.updateTrcNumdocAndTrcStatusTransfer(new
      // java.sql.Date(fechaDate.getTime()), numDoc);
      // }

      TransferHeaderResponseEntity resultEntity =
          transferDao.consultDocumentTransfer(
              new java.sql.Date(fechaDate.getTime()), "usuario", 1, numDoc);
      if (resultEntity != null && resultEntity.getData() instanceof List) {
        Object data = resultEntity.getData();
        List<TransferHeaderEntity> tempResponse = new ArrayList<>();
        if (data instanceof List<?>) {
          for (Object item : (List<?>) data) {
            if (item instanceof TransferHeaderEntity) {
              tempResponse.add((TransferHeaderEntity) item);
            }
          }
        }
        response = tempResponse;
      }

    } catch (Exception e) {
      System.err.println("Error en servicio: " + e.getMessage());
    }
    return response;
  }

  public List<TransferDetailEntity> consultarDetalleTransferencia(String idCompany, String numDoc) {
    List<TransferDetailEntity> response = new ArrayList<>();

    if (numDoc == null || numDoc.isEmpty() || idCompany == null || idCompany.isEmpty()) {
      return response;
    }

    try {

      TransferDetailResponseEntity resultEntity =
          transferDao.consultarDocumentodetalleTransferencia("usuario", 2, numDoc);

      if (resultEntity != null && resultEntity.getData() instanceof List) {
        Object data = resultEntity.getData();
        List<TransferDetailEntity> tempResponse = new ArrayList<>();
        if (data instanceof List<?>) {
          for (Object item : (List<?>) data) {
            if (item instanceof TransferDetailEntity) {
              tempResponse.add((TransferDetailEntity) item);
            }
          }
        }
        response = tempResponse;
      }

    } catch (Exception e) {
      System.err.println("Error en servicio: " + e.getMessage());
    }
    return response;
  }

  public TransfersCompartmentsOrderDto transferenciaCompartimentoService(
      TransferCompartmentOrdersRequest transfers) {
    return transferDao.TransferenciaCompartimento(transfers);
  }
}
