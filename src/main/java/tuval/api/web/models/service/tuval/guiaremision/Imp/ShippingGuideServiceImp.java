package tuval.api.web.models.service.tuval.guiaremision.Imp;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tuval.api.web.models.dao.tuval.guiaremision.ShippingGuideDao;
import tuval.api.web.models.dto.bodegas.guiaRemision.DetailGuideTransferVtaRequest;
import tuval.api.web.models.dto.bodegas.guiaRemision.TransferVtaRequest;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.service.tuval.guiaremision.ShippingGuideService;

@Service
public class ShippingGuideServiceImp implements ShippingGuideService {
    @Autowired
    private ShippingGuideDao shippingGuideDao;

    public CheckCodeAccessDeliveryNoteResponse checkGuiaRemisionService(String numDoc) {
        return shippingGuideDao.checkGuiaRemision(numDoc);
    }


    @Transactional(rollbackFor = Exception.class)
    public HeaderGuideTransferVtaResponse generateGuideTransferVtaService(TransferVtaRequest req) {
        HeaderGuideTransferVtaResponse responseHeader = shippingGuideDao.executeSpStoreHeaderTransferVta(req.getCabecera());
        int sec = 1;
        if (req.getDetalle() != null) {
            for (DetailGuideTransferVtaRequest det : req.getDetalle()) {
                shippingGuideDao.executeSpStoreDetailGuideTransferVta(det,  req.getCabecera(), responseHeader.getIdMovimiento(),  sec);
                 sec++;
            }
        }
        return  shippingGuideDao.executeSpGenerateMovimentTransferVta( req.getCabecera(), responseHeader.getIdMovimiento());       
    }
}
