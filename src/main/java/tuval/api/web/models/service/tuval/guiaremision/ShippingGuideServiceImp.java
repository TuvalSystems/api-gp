package tuval.api.web.models.service.tuval.guiaremision;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import tuval.api.web.models.dao.tuval.guiaremision.ShippingGuideDao;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailCheckGuideAnyResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.ValidateExistsDeliveryNoteResponse;
import tuval.api.web.models.request.bodegas.guiaremision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.request.bodegas.guiaremision.DispatchDeliveryNoteMovementRequest;
import tuval.api.web.models.request.bodegas.guiaremision.DispatchVtaRequest;
import tuval.api.web.models.request.bodegas.guiaremision.TransferVtaRequest;
import tuval.api.web.models.request.bodegas.guiaremision.ValidateGuideSalesTransferedRequest;
import tuval.api.web.models.request.bodegas.guiaremision.crearGuia.DeliveryNoteRequest;
import tuval.api.web.models.request.bodegas.guiaremision.crearGuia.DetailDeliveryNoteRequest;

@Service
@RequiredArgsConstructor
public class ShippingGuideServiceImp implements ShippingGuideService {
  private final ShippingGuideDao shippingGuideDao;

  @Override
  public CheckCodeAccessDeliveryNoteResponse checkGuiaRemisionService(String numDoc) {
    return shippingGuideDao.checkGuiaRemision(numDoc);
  }

  @Override
  public HeaderGuideTransferVtaResponse generateGuideTransferVtaService(TransferVtaRequest req) {
    HeaderGuideTransferVtaResponse responseHeader = shippingGuideDao.executeSpStoreHeaderTransferVta(req.getCabecera(),
        1);

    if (req.getDetalle() != null) {
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailGuideTransferVta(
            det, req.getCabecera(), responseHeader.getIdMovimiento(), 1, 2);
      }
    }
    return shippingGuideDao.executeSpGenerateMovimentTransferVta(
        req.getCabecera(), responseHeader.getIdMovimiento(), 3);
  }

  // @Transactional(rollbackFor = Exception.class)
  @Override
  public HeaderGuideTransferVtaResponse generateGuideReposicionService(TransferVtaRequest req) {
    HeaderGuideTransferVtaResponse header;
    header = shippingGuideDao.executeSpStoreHeaderTransferVta(req.getCabecera(), 10);
    if (req.getDetalle() != null && !req.getDetalle().isEmpty()) {
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailGuideTransferVta(
            det, req.getCabecera(), header.getIdMovimiento(), 1, 11);
      }
    }
    return shippingGuideDao.executeSpGenerateMovimentTransferVta(
        req.getCabecera(), header.getIdMovimiento(), 12);

  }

  // @Transactional(rollbackFor = Exception.class)
  @Override
  public HeaderGuideTransferVtaResponse generateGuideDispatchVtaService(DispatchVtaRequest req) {
    HeaderGuideTransferVtaResponse responseHeader = shippingGuideDao
        .executeSpStoreHeaderGuideDispatchVta(req.getCabecera(), 7);
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
  public HeaderGuideTransferVtaResponse generateMovementDispatchGuide(DispatchDeliveryNoteMovementRequest req) {
    return shippingGuideDao.executeFixSpGenerateMovementDispatchDeliveryNote(
        req, 9);
  }

  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideDispatchVtaService(
      String orderN, String newOrderN, Integer idGuiaRemision, String guiaRemision) {
    return shippingGuideDao.getAjusteGuideDispatchVtaDao(
        orderN, newOrderN, idGuiaRemision, guiaRemision);
  }

  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideTransferVtaService(
      String orderN, String newOrderN, String origen, Integer idGuiaRemision, String guiaRemision) {
    return shippingGuideDao.getAjusteGuideTransferVtaDao(
        orderN, newOrderN, origen, idGuiaRemision, guiaRemision);
  }

  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideRepositionVtaService(
      String orderN,
      String origen,
      Integer idMovimiento,
      Integer idGuiaRemision,
      String guiaRemision) {
    return shippingGuideDao.getAjusteGuideRepositionVtaDao(
        orderN, origen, idMovimiento, idGuiaRemision, guiaRemision);
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

  @Override
  public void createDeliveryNoteService(DeliveryNoteRequest req) {

    shippingGuideDao.executeSpStoreHeaderDeliveryNote(req.getCabecera());
    shippingGuideDao.executeSpStoreDestinationDeliveryNote(req.getDestinatario());

    if (req.getDetalle() != null && !req.getDetalle().isEmpty()) {
      for (DetailDeliveryNoteRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailDeliveryNote(det);
      }
    }
    shippingGuideDao.executeSpCreateAccesoCodeDeliveryNote(req.getCabecera());
    shippingGuideDao.executeSpUpdateAccesoCodeDeliveryNote(req.getCabecera());

  }

  @Override
  public ValidateExistsDeliveryNoteResponse checkExistenceDeliveryNoteService(String orderNumber) {
    return shippingGuideDao.executeSpValidateExistsDeliveryNoteResponse(orderNumber);
  }

  /** proceso de validacion de existencia de guia de remision - reservas */
  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideReservationService(
      String orderN, String newOrderN, String origen, Integer idGuiaRemision, String guiaRemision) {
    return shippingGuideDao.getAjusteGuideReservedDao(
        orderN, newOrderN, origen, idGuiaRemision, guiaRemision);
  }

  public HeaderGuideTransferVtaResponse generateGuideReservationService(TransferVtaRequest req) {
    // cabecera
    HeaderGuideTransferVtaResponse header;
    header = shippingGuideDao.executeSpStoreHeaderTransferVta(req.getCabecera(), 20);

    // detale
    if (req.getDetalle() != null && !req.getDetalle().isEmpty()) {
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailGuideTransferVta(
            det, req.getCabecera(), header.getIdMovimiento(), 1, 21);
      }
    }

    // movimiento
    return shippingGuideDao.executeSpGenerateMovimentTransferVta(
        req.getCabecera(), header.getIdMovimiento(), 22);

  }

  /** validar existencia de guia de reserva recibida */
  @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideReservationReceivedService(
      String orderN, String newOrderN, String origen, Integer idGuiaRemision, String guiaRemision) {
    return shippingGuideDao.getAjusttmentReceivedReservedDao(
        orderN, newOrderN, origen, idGuiaRemision, guiaRemision);
  }

  @Override
  public HeaderGuideTransferVtaResponse receiveGuideReservationService(TransferVtaRequest req) {
    HeaderGuideTransferVtaResponse header;
    header = shippingGuideDao.executeSpStoreHeaderReceivedGuideTransfered(req.getCabecera(), 23);

    if (req.getDetalle() != null && !req.getDetalle().isEmpty()) {
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailReceivedGuideTransfered(
            det, req.getCabecera(), header.getIdMovimiento(), 1, 24);
      }
    }

    return shippingGuideDao.executeSpGenerateMovimentReceivedGuideTransfered(
        req.getCabecera(), header.getIdMovimiento(), 25);
  }

  @Override
  public HeaderGuideTransferVtaResponse receivedGuideSalesTransferService(TransferVtaRequest req) {
    // cabecera
    HeaderGuideTransferVtaResponse header;
    header = shippingGuideDao.executeSpStoreHeaderReceivedSalesTransferedDao(req.getCabecera(), 4);

    // detalle
    if (req.getDetalle() != null && !req.getDetalle().isEmpty()) {
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailReceivedGuideSalesTransferedDao(
            det, req.getCabecera(), header.getIdMovimiento(), 1, 5);
      }
    }
    // //movimiento
    return shippingGuideDao.executeSpGenerateMovimentReceivedGuideTransferedDao(
        req.getCabecera(), header.getIdMovimiento(), 6);

  }


   @Override
  public HeaderCheckGuideDispatchResponse checkAjusteGuideSalesTransferReceivedService(
       ValidateGuideSalesTransferedRequest req) {
    return shippingGuideDao.getAdjustmentGuideSalesTransferedDao(
        req);
  }


  @Override
  public HeaderGuideTransferVtaResponse receivedGuideReplenishmentTransferService(TransferVtaRequest req) {
    // cabecera
    HeaderGuideTransferVtaResponse header;
    header = shippingGuideDao.executeSpStoreHeaderReceivedSalesTransferedDao(req.getCabecera(), 13);

    // detalle
    if (req.getDetalle() != null && !req.getDetalle().isEmpty()) {
      for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
        shippingGuideDao.executeSpStoreDetailReceivedGuideReplenishmentTransferDao(
            det, req.getCabecera(), header.getIdMovimiento(), 1, 14);
      }
    }
    // //movimiento
    return shippingGuideDao.executeSpGenerateMovimentReceivedGuideTransferedDao(
        req.getCabecera(), header.getIdMovimiento(), 15);

  }
}
