package tuval.api.web.bodega.despachos.service.tuval;

import tuval.api.web.bodega.despachos.entity.tuval.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.bodega.despachos.entity.tuval.DetailCheckGuideAnyResponse;
import tuval.api.web.bodega.despachos.entity.tuval.HeaderCheckGuideDispatchResponse;
import tuval.api.web.bodega.despachos.entity.tuval.HeaderGuideTransferVtaResponse;
import tuval.api.web.bodega.despachos.entity.tuval.ValidateExistsDeliveryNoteResponse;
import tuval.api.web.bodega.despachos.request.DispatchDeliveryNoteMovementRequest;
import tuval.api.web.bodega.despachos.request.DispatchVtaRequest;
import tuval.api.web.bodega.despachos.request.GuidePurchaseOrderRequest;
import tuval.api.web.bodega.despachos.request.TransferVtaRequest;
import tuval.api.web.bodega.despachos.request.ValidateGuideDispatchRequest;
import tuval.api.web.bodega.despachos.request.ValidateGuideSalesTransferedRequest;
import tuval.api.web.bodega.despachos.request.crearguia.DeliveryNoteRequest;

public interface ShippingGuideService {
    void insertGuidePurchaseOrder(GuidePurchaseOrderRequest request);

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
           ValidateGuideSalesTransferedRequest req);

    public HeaderGuideTransferVtaResponse generateGuideReservationService(TransferVtaRequest req);

    /** reservas - recibir */

    public HeaderGuideTransferVtaResponse receiveGuideReservationService(TransferVtaRequest req);

    public HeaderGuideTransferVtaResponse receivedGuideSalesTransferService(TransferVtaRequest req);

    public HeaderCheckGuideDispatchResponse checkAjusteGuideSalesTransferReceivedService(
            ValidateGuideSalesTransferedRequest req);

    public HeaderGuideTransferVtaResponse receivedGuideReplenishmentTransferService(TransferVtaRequest req);
}
