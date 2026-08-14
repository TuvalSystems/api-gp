package tuval.api.web.models.service.dynamics.cotizaciones;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuval.api.web.models.dao.dynamics.cotizaciones.QuoteRecordDao;
import tuval.api.web.models.dto.cotizaciones.ItemDescripcionAlternativaDto;
import tuval.api.web.models.dto.cotizaciones.MotivoProyectoVentaDto;
import tuval.api.web.models.entity.dynamics.cotizaciones.QuoteRecordEntity;

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

  /** motivos / proyectos de venta */
  public List<MotivoProyectoVentaDto> listProyectosVenta() {
    return quoteRecordDao.findAllMotivosProyectosVenta();
  }
}
