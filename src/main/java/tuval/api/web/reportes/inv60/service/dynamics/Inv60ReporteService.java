package tuval.api.web.reportes.inv60.service.dynamics;

import java.util.List;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.inv60.dao.dynamics.Inv60ReporteDao;
import tuval.api.web.reportes.inv60.dto.Inv60ReporteDto;
import tuval.api.web.reportes.inv60.request.Inv60ReporteRequest;

@Service
public class Inv60ReporteService {
  private final Inv60ReporteDao dao;

  public Inv60ReporteService(Inv60ReporteDao dao) {
    this.dao = dao;
  }

  public List<Inv60ReporteDto> consultar(Inv60ReporteRequest request) {
    if (request == null || !request.isConsultaValida()) {
      throw new IllegalArgumentException("Parametros invalidos del reporte spInv60");
    }
    return dao.consultar(request);
  }
}
