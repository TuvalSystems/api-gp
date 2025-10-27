package tuval.api.web.models.dao.tuval.guiaremision;

import tuval.api.web.models.dto.bodegas.guiaRemision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.HeaderGuideDispatchShipmentRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.HeaderGuideTransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailCheckGuideAnyResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;


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



 public HeaderCheckGuideDispatchResponse getAjusteGuideDispatchVtaDao(String orderNumber, String newOrderNumber);
 public HeaderCheckGuideDispatchResponse getAjusteGuideTransferVtaDao(String orderNumber, String newOrderNumber, String origen,  Integer idGuiaRemision, String guiaRemision);
 public HeaderCheckGuideDispatchResponse getAjusteGuideRepositionVtaDao(String orderNumber, String origen, Integer idMovimiento, Integer idGuiaRemision, String guiaRemision);
 public DetailCheckGuideAnyResponse getAjustmentDetailGuide(String orderNumber, String newOrderNumber, String item, Integer idMovimiento, Integer cantidad);
 
}
