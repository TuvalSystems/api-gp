package tuval.api.web.bodega.reservas.service.dynamics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tuval.api.web.bodega.reservas.dao.dynamics.ReservedDao;
import tuval.api.web.bodega.reservas.entity.dynamics.NotifyLocalEntity;

@Service
public class ReservedService {
  @Autowired private ReservedDao reservedDao;

  public NotifyLocalEntity findByTxPtoVta(String ptovta, int conot) {
    return reservedDao.findByTxPtoVta(ptovta, conot);
  }
}
