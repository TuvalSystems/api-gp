package tuval.api.web.models.service.tuval.guiaremision.Imp;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.guiaremision.ShippingGuideDao;
import tuval.api.web.models.service.tuval.guiaremision.ShippingGuideService;

@Service
public class ShippingGuideServiceImp implements ShippingGuideService {
    @Autowired
    private ShippingGuideDao shippingGuideDao;


    public ResponseEntity<Map<String, Object>>  checkGuiaRemisionService(String numDoc) {
        return shippingGuideDao.checkGuiaRemision(numDoc);
    }
}
