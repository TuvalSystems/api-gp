package tuval.api.web.models.dao.tuval.guiaremision;

import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailCheckGuideAnyResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.ValidateExistsDeliveryNoteResponse;
import tuval.api.web.models.request.bodegas.guiaRemision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.request.bodegas.guiaRemision.DispatchDeliveryNoteMovementRequest;
import tuval.api.web.models.request.bodegas.guiaRemision.HeaderGuideDispatchShipmentRequest;
import tuval.api.web.models.request.bodegas.guiaRemision.HeaderGuideTransferVtaRequest;
import tuval.api.web.models.request.bodegas.guiaRemision.crearGuia.DestinationDeliveryNoteRequest;
import tuval.api.web.models.request.bodegas.guiaRemision.crearGuia.DetailDeliveryNoteRequest;
import tuval.api.web.models.request.bodegas.guiaRemision.crearGuia.HeaderDeliveryNoteRequest;

public interface ShippingGuideDao {

    public CheckCodeAccessDeliveryNoteResponse checkGuiaRemision(String numDoc);

    public HeaderGuideTransferVtaResponse executeSpStoreHeaderTransferVta(
            HeaderGuideTransferVtaRequest req, int modo);

    public DetailGuideTransferVtaResponse executeSpStoreDetailGuideTransferVta(
            DetailGuideTransferVtaRequest det,
            HeaderGuideTransferVtaRequest cab,
            int idmov,
            int sec,
            int modo);

    public HeaderGuideTransferVtaResponse executeSpGenerateMovimentTransferVta(
            HeaderGuideTransferVtaRequest req, int idmov, int modo);

    public HeaderGuideTransferVtaResponse executeSpStoreHeaderGuideDispatchVta(
            HeaderGuideDispatchShipmentRequest req, int modo);

    public DetailGuideTransferVtaResponse executeSpStoreDetailGuideDispatchVta(
            DetailGuideTransferVtaRequest det,
            HeaderGuideDispatchShipmentRequest cab,
            int idmov,
            int secitem,
            int modo);

    public HeaderGuideTransferVtaResponse executeSpGenerateMovimentDispatchVta(
            HeaderGuideDispatchShipmentRequest req, int idmov, int modo);

    public HeaderGuideTransferVtaResponse executeFixSpGenerateMovementDispatchDeliveryNote(
            DispatchDeliveryNoteMovementRequest req, int modo);

    public HeaderCheckGuideDispatchResponse getAjusteGuideDispatchVtaDao(
            String orderNumber, String newOrderNumber, Integer idGuiaRemision, String guiaRemision);

    public HeaderCheckGuideDispatchResponse getAjusteGuideTransferVtaDao(
            String orderNumber,
            String newOrderNumber,
            String origen,
            Integer idGuiaRemision,
            String guiaRemision);

    public HeaderCheckGuideDispatchResponse getAjusteGuideRepositionVtaDao(
            String orderNumber,
            String origen,
            Integer idMovimiento,
            Integer idGuiaRemision,
            String guiaRemision);

    public DetailCheckGuideAnyResponse getAjustmentDetailGuide(
            String orderNumber,
            String newOrderNumber,
            String item,
            Integer idMovimiento,
            Integer cantidad);

    /** proceso de guia de remision */
    public void executeSpStoreHeaderDeliveryNote(HeaderDeliveryNoteRequest h);

    public void executeSpStoreDestinationDeliveryNote(DestinationDeliveryNoteRequest dh);

    public void executeSpStoreDetailDeliveryNote(DetailDeliveryNoteRequest d);

    public void executeSpCreateAccesoCodeDeliveryNote(HeaderDeliveryNoteRequest h);

    public void executeSpUpdateAccesoCodeDeliveryNote(HeaderDeliveryNoteRequest h);

    public ValidateExistsDeliveryNoteResponse executeSpValidateExistsDeliveryNoteResponse(
            String docu);

    /** reservas */
    public HeaderCheckGuideDispatchResponse getAjusteGuideReservedDao(
            String orderNumber,
            String newOrderNumber,
            String origen,
            Integer idGuiaRemision,
            String guiaRemision);

    /** VALIDAR EXISTENCIA DE AJUSTE DE RECIBIDO */
    public HeaderCheckGuideDispatchResponse getAjusttmentReceivedReservedDao(
            String orderNumber,
            String newOrderNumber,
            String origen,
            Integer idGuiaRemision,
            String guiaRemision);

    public HeaderGuideTransferVtaResponse executeSpStoreHeaderReceivedGuideTransfered(
            HeaderGuideTransferVtaRequest req, int modo);

    public DetailGuideTransferVtaResponse executeSpStoreDetailReceivedGuideTransfered(
            DetailGuideTransferVtaRequest det,
            HeaderGuideTransferVtaRequest cab,
            int idmov,
            int secitem,
            int modo);

    public HeaderGuideTransferVtaResponse executeSpGenerateMovimentReceivedGuideTransfered(
            HeaderGuideTransferVtaRequest req, int idmov, int modo);

    public HeaderGuideTransferVtaResponse executeSpStoreHeaderReceivedSalesTransferedDao(
            HeaderGuideTransferVtaRequest req,
            int modo);

    public DetailGuideTransferVtaResponse executeSpStoreDetailReceivedGuideSalesTransferedDao(
            DetailGuideTransferVtaRequest det,
            HeaderGuideTransferVtaRequest cab,
            int idmov,
            int secitem,
            int modo);

    public HeaderGuideTransferVtaResponse executeSpGenerateMovimentReceivedGuideTransferedDao(
            HeaderGuideTransferVtaRequest req, int idmov, int modo);
}
