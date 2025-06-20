package tuval.api.web.models.service.dynamics.cotizaciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tuval.api.web.models.dao.dynamics.cotizaciones.QuoteRecordDao;
import tuval.api.web.models.entity.dynamics.cotizaciones.QuoteRecordEntity;

import java.util.Map;

@Service
public class quoteRecordService {

    @Autowired
    private QuoteRecordDao quoteRecordDao;

    @Transactional("dynamicsTransactionManager")
    public void save(QuoteRecordEntity entity) {
        // QuoteRecordEntity entity = new QuoteRecordEntity();
        // BeanUtils.copyProperties(dto, entity);
        quoteRecordDao.save(entity);
    }


    /** items alternativos */

    public ResponseEntity<Map<String, Object>> listAlternativeItems(String item) {
    return quoteRecordDao.findByItem(item);
}
}
