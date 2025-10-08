package tuval.api.web.models.service.tuval.guiaremision.Imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
import tuval.api.web.models.dao.tuval.guiaremision.ShippingGuideDao;
import tuval.api.web.models.dto.bodegas.guiaRemision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.DispatchVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.TransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.service.tuval.guiaremision.ShippingGuideService;

@Service
public class ShippingGuideServiceImp implements ShippingGuideService {
  @Autowired private ShippingGuideDao shippingGuideDao;

  public CheckCodeAccessDeliveryNoteResponse checkGuiaRemisionService(String numDoc) {
    return shippingGuideDao.checkGuiaRemision(numDoc);
  }

  // @Transactional(rollbackFor = Exception.class)
  public HeaderGuideTransferVtaResponse generateGuideTransferVtaService(TransferVtaRequest req) {
    HeaderGuideTransferVtaResponse responseHeader =
        shippingGuideDao.executeSpStoreHeaderTransferVta(req.getCabecera(), 1);

    if (req.getDetalle() != null) {
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailGuideTransferVta(
            det, req.getCabecera(), responseHeader.getIdMovimiento(), 1, 2);
      }
    }
    return shippingGuideDao.executeSpGenerateMovimentTransferVta(
        req.getCabecera(), responseHeader.getIdMovimiento(), 3);
  }

  //  @Transactional(rollbackFor = Exception.class)
  public HeaderGuideTransferVtaResponse generateGuideReposicionService(TransferVtaRequest req) {
    HeaderGuideTransferVtaResponse responseHeader =
        shippingGuideDao.executeSpStoreHeaderTransferVta(req.getCabecera(), 10);
    if (req.getDetalle() != null) {
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailGuideTransferVta(
            det, req.getCabecera(), responseHeader.getIdMovimiento(), 1, 11);
      }
    }
    return shippingGuideDao.executeSpGenerateMovimentTransferVta(
        req.getCabecera(), responseHeader.getIdMovimiento(), 12);
  }

  // @Transactional(rollbackFor = Exception.class)
  public HeaderGuideTransferVtaResponse generateGuideDispatchVtaService(DispatchVtaRequest req) {
    HeaderGuideTransferVtaResponse responseHeader =
        shippingGuideDao.executeSpStoreHeaderGuideDispatchVta(req.getCabecera(), 7);
    if (req.getDetalle() != null) {
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailGuideDispatchVta(
            det, req.getCabecera(), responseHeader.getIdMovimiento(), 1, 8);
      }
    }
    return shippingGuideDao.executeSpGenerateMovimentDispatchVta(
        req.getCabecera(), responseHeader.getIdMovimiento(), 9);
  }

  public HeaderCheckGuideDispatchResponse checkAjusteGuideDispatchVtaService(String orderN) {
    return shippingGuideDao.getAjusteGuideDispatchVtaDao(orderN);
  }
}
