package tuval.api.web.models.service.tuval.guiaremision;

import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailCheckGuideAnyResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.ValidateExistsDeliveryNoteResponse;
import tuval.api.web.models.request.bodegas.guiaremision.DispatchDeliveryNoteMovementRequest;
import tuval.api.web.models.request.bodegas.guiaremision.DispatchVtaRequest;
import tuval.api.web.models.request.bodegas.guiaremision.TransferVtaRequest;
import tuval.api.web.models.request.bodegas.guiaremision.ValidateGuideDispatchRequest;
import tuval.api.web.models.request.bodegas.guiaremision.ValidateGuideSalesTransferedRequest;
import tuval.api.web.models.request.bodegas.guiaremision.crearGuia.DeliveryNoteRequest;

public interface ShippingGuideService {

    public CheckCodeAccessDeliveryNoteResponse checkGuiaRemisionService(String numDoc);

    public HeaderGuideTransferVtaResponse generateGuideTransferVtaService(TransferVtaRequest req);

    public HeaderGuideTransferVtaResponse generateGuideReposicionService(TransferVtaRequest req);

    public HeaderGuideTransferVtaResponse generateGuideDispatchVtaService(DispatchVtaRequest req);

    public HeaderGuideTransferVtaResponse generateMovementDispatchGuide(DispatchDeliveryNoteMovementRequest req);

    /** para obtener el ajuste de guias de despacho */
    public HeaderCheckGuideDispatchResponse checkAjusteGuideDispatchVtaService(
            ValidateGuideDispatchRequest req);

    public HeaderCheckGuideDispatchResponse checkAjusteGuideTransferVtaService(
            String orderN, String newOrderN, String origen, Integer idGuiaRemision, String guiaRemision);

    public HeaderCheckGuideDispatchResponse checkAjusteGuideRepositionVtaService(
            String orderN,
            String origen,
            Integer idMovimiento,
            Integer idGuiaRemision,
            String guiaRemision);

    public DetailCheckGuideAnyResponse checkAjustmentDetailGuideService(
            String orderNumber,
            String newOrderNumber,
            String item,
            Integer idMovimiento,
            Integer cantidad);

    public void createDeliveryNoteService(DeliveryNoteRequest req);

    public ValidateExistsDeliveryNoteResponse checkExistenceDeliveryNoteService(String orderNumber);

    /** reservas */
    public HeaderCheckGuideDispatchResponse checkAjusteGuideReservationService(
            String orderN, String newOrderN, String origen, Integer idGuiaRemision, String guiaRemision);

    public HeaderGuideTransferVtaResponse generateGuideReservationService(TransferVtaRequest req);

    /** reservas - recibir */
    public HeaderCheckGuideDispatchResponse checkAjusteGuideReservationReceivedService(
            String orderN, String newOrderN, String origen, Integer idGuiaRemision, String guiaRemision);

    public HeaderGuideTransferVtaResponse receiveGuideReservationService(TransferVtaRequest req);

    public HeaderGuideTransferVtaResponse receivedGuideSalesTransferService(TransferVtaRequest req);

    public HeaderCheckGuideDispatchResponse checkAjusteGuideSalesTransferReceivedService(
            ValidateGuideSalesTransferedRequest req);

    public HeaderGuideTransferVtaResponse receivedGuideReplenishmentTransferService(TransferVtaRequest req);
}
