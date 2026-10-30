package tuval.api.web.reportes.articulos.service.dynamics;

import java.util.List;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.articulos.dao.dynamics.ArticulosReporteDao;
import tuval.api.web.reportes.articulos.dto.ArticulosReporteDto;
import tuval.api.web.reportes.articulos.request.ArticulosReporteRequest;

@Service
public class ArticulosReporteService {
  private final ArticulosReporteDao dao;

  public ArticulosReporteService(ArticulosReporteDao dao) {
    this.dao = dao;
  }

  public List<ArticulosReporteDto> consultar(ArticulosReporteRequest request) {
    if (request == null || request.getPage() == null || request.getPage() < 0
        || request.getSize() == null || request.getSize() < 1 || request.getSize() > 500
        || request.getSort() == null || request.getDirection() == null) {
      throw new IllegalArgumentException("Parámetros inválidos del reporte de artículos");
    }
    return dao.consultar(request);
  }
}
