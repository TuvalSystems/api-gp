package tuval.api.web.models.service.tuval.guiaremision;



import tuval.api.web.models.dto.bodegas.guiaRemision.TransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;

public interface ShippingGuideService {
    
    public CheckCodeAccessDeliveryNoteResponse checkGuiaRemisionService(String numDoc);

    public HeaderGuideTransferVtaResponse generateGuideTransferVtaService(TransferVtaRequest req);
}
