package tuval.api.web.models.service.dynamics.cotizaciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tuval.api.web.models.dao.dynamics.cotizaciones.QuoteRecordDao;
import tuval.api.web.models.entity.dynamics.cotizaciones.QuoteRecordEntity;

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
}
