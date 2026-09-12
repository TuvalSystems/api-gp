package tuval.api.web.ventas.cotizaciones.service.dynamics;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuval.api.web.ventas.cotizaciones.dao.dynamics.QuoteRecordDao;
import tuval.api.web.ventas.cotizaciones.dto.ItemDescripcionAlternativaDto;
import tuval.api.web.ventas.cotizaciones.dto.SalesProjectReasonDto;
import tuval.api.web.ventas.cotizaciones.entity.dynamics.QuoteRecordEntity;

@Service
public class quoteRecordService {

  @Autowired
  private QuoteRecordDao quoteRecordDao;

  @Transactional("dynamicsTransactionManager")
  public void save(QuoteRecordEntity entity) {
    quoteRecordDao.save(entity);
  }


  /** items alternativos */

  public List<ItemDescripcionAlternativaDto> listAlternativeItems(String item) {
    return quoteRecordDao.findByItem(item);
  }

  /** sales project reasons */
  public List<SalesProjectReasonDto> listSalesProjectReasons() {
    return quoteRecordDao.findAllSalesProjectReasons();
  }

  /** sales project reason by code */
  public SalesProjectReasonDto getSalesProjectReasonByCoMotproven(Integer coMotproven) {
    return quoteRecordDao.findSalesProjectReasonByCoMotproven(coMotproven);
  }
}
