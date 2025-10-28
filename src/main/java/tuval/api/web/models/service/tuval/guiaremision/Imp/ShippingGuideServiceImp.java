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
    HeaderGuideTransferVtaResponse header;
    try {
      header = shippingGuideDao.executeSpStoreHeaderTransferVta(req.getCabecera(), 10);
    } catch (Exception e) {
      throw new RuntimeException(
          "No se pudo guardar la cabecera de la guía de transferencia: " + e);
    }

    if (req.getDetalle() != null && !req.getDetalle().isEmpty()) {
      int secItem = 1;
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        try {
          shippingGuideDao.executeSpStoreDetailGuideTransferVta(
              det, req.getCabecera(), header.getIdMovimiento(), 1, 11);
          secItem++;
        } catch (Exception e) {
          throw new RuntimeException("Error al guardar el detalle (secItem=" + secItem + "): " + e);
        }
      }
    }

    try {
      return shippingGuideDao.executeSpGenerateMovimentTransferVta(
          req.getCabecera(), header.getIdMovimiento(), 12);
    } catch (Exception e) {
      throw new RuntimeException("Error al generar el movimiento de transferencia: " + e);
    }
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
  public HeaderCheckGuideDispatchResponse checkAjusteGuideDispatchVtaService(
      String orderN, String newOrderN, Integer idGuiaRemision, String guiaRemision) {
    return shippingGuideDao.getAjusteGuideDispatchVtaDao(orderN, newOrderN, idGuiaRemision, guiaRemision);
  }

  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideTransferVtaService(
      String orderN,
      String newOrderN,
      String origen,
      Integer idGuiaRemision,
      String guiaRemision) {
    return shippingGuideDao.getAjusteGuideTransferVtaDao(
        orderN, newOrderN, origen,  idGuiaRemision, guiaRemision);
  }

  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideRepositionVtaService(
      String orderN, String origen, Integer idMovimiento,Integer idGuiaRemision, String guiaRemision) {
    return shippingGuideDao.getAjusteGuideRepositionVtaDao(orderN, origen, idMovimiento, idGuiaRemision, guiaRemision);
  }

  @Override
  public DetailCheckGuideAnyResponse checkAjustmentDetailGuideService(
      String orderNumber,
      String newOrderNumber,
      String item,
      Integer idMovimiento,
      Integer cantidad) {
    return shippingGuideDao.getAjustmentDetailGuide(
        orderNumber, newOrderNumber, item, idMovimiento, cantidad);
  }
}
