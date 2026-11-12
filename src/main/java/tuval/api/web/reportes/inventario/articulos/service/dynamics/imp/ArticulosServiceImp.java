package tuval.api.web.reportes.inventario.articulos.service.dynamics.imp;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.inventario.articulos.dao.dynamics.ArticulosDao;
import tuval.api.web.reportes.inventario.articulos.dto.ArticulosDto;
import tuval.api.web.reportes.inventario.articulos.request.ArticulosRequest;
import tuval.api.web.reportes.inventario.articulos.service.dynamics.ArticulosService;

@Service
@RequiredArgsConstructor
public class ArticulosServiceImp implements ArticulosService {
  private final ArticulosDao articulosDao;

  @Override
  public List<ArticulosDto> consultar(ArticulosRequest request) {
    if (request == null || request.getPage() == null || request.getPage() < 0
        || request.getSize() == null || request.getSize() < 1 || request.getSize() > 500
        || request.getSort() == null || request.getDirection() == null) {
      throw new IllegalArgumentException("Parámetros inválidos del reporte de artículos");
    }
    return articulosDao.consultar(request);
  }
}
