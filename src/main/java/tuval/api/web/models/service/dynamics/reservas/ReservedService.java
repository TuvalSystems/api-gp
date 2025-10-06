package tuval.api.web.models.service.dynamics.reservas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tuval.api.web.models.dao.dynamics.reservas.ReservedDao;
import tuval.api.web.models.entity.dynamics.reservas.NotifyLocalEntity;

@Service
public class ReservedService {
  @Autowired private ReservedDao reservedDao;

  public NotifyLocalEntity findByTxPtoVta(String ptovta, int conot) {
    return reservedDao.findByTxPtoVta(ptovta, conot);
  }
}
