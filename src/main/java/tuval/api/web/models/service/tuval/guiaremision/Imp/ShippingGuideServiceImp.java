package tuval.api.web.models.service.tuval.guiaremision.Imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.guiaremision.ShippingGuideDao;
import tuval.api.web.models.dto.bodegas.guiaRemision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.DispatchVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.TransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailCheckGuideAnyResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.ListHeaderCheckGuideDispatchResponse;
import tuval.api.web.models.service.tuval.guiaremision.ShippingGuideService;

@Service
public class ShippingGuideServiceImp implements ShippingGuideService {
  @Autowired private ShippingGuideDao shippingGuideDao;

  @Override
  public CheckCodeAccessDeliveryNoteResponse checkGuiaRemisionService(String numDoc) {
    return shippingGuideDao.checkGuiaRemision(numDoc);
  }

  // @Transactional(rollbackFor = Exception.class)
  @Override
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
  @Override
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
  @Override
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

  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideDispatchVtaService(String orderN, String newOrderN) {
    return shippingGuideDao.getAjusteGuideDispatchVtaDao(orderN, newOrderN);
  }
  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideTransferVtaService(String orderN, String newOrderN, String origen, Integer idMovimiento) {
    return shippingGuideDao.getAjusteGuideTransferVtaDao(orderN, newOrderN, origen, idMovimiento);
  }

  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideRepositionVtaService(String orderN, String origen, Integer idMovimiento) {
    return shippingGuideDao.getAjusteGuideRepositionVtaDao(orderN, origen, idMovimiento);
  }


  @Override
  public DetailCheckGuideAnyResponse checkAjustmentDetailGuideService(String orderNumber, String newOrderNumber, String item, Integer idMovimiento, Integer cantidad) {
    return shippingGuideDao.getAjustmentDetailGuide(orderNumber, newOrderNumber, item, idMovimiento, cantidad);
  }
}
