package tuval.api.web.models.dao.tuval.guiaremision;

import tuval.api.web.models.dto.bodegas.guiaRemision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.HeaderGuideTransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;

public interface ShippingGuideDao {

    public CheckCodeAccessDeliveryNoteResponse checkGuiaRemision(String numDoc);

     public HeaderGuideTransferVtaResponse executeSpStoreHeaderTransferVta(HeaderGuideTransferVtaRequest req);
      public DetailGuideTransferVtaResponse executeSpStoreDetailGuideTransferVta(DetailGuideTransferVtaRequest det, HeaderGuideTransferVtaRequest cab,  int idmov, int sec);
      public HeaderGuideTransferVtaResponse executeSpGenerateMovimentTransferVta(HeaderGuideTransferVtaRequest req, int idmov);
} 
