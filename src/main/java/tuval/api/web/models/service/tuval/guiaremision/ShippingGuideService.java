package tuval.api.web.models.service.tuval.guiaremision;



import tuval.api.web.models.dto.bodegas.guiaRemision.DispatchVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.TransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;

public interface ShippingGuideService {
    
    public CheckCodeAccessDeliveryNoteResponse checkGuiaRemisionService(String numDoc);

    public HeaderGuideTransferVtaResponse generateGuideTransferVtaService(TransferVtaRequest req);
    public HeaderGuideTransferVtaResponse generateGuideReposicionService(TransferVtaRequest req);
    public HeaderGuideTransferVtaResponse generateGuideDispatchVtaService(DispatchVtaRequest req);


    /** para obtener el ajuste de guias de despacho */
     public HeaderCheckGuideDispatchResponse checkAjusteGuideDispatchVtaService (String orderN, String newOrderN);
}
