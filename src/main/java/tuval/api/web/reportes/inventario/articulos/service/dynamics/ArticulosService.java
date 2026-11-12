package tuval.api.web.reportes.inventario.articulos.service.dynamics;

import java.util.List;
import tuval.api.web.reportes.inventario.articulos.dto.ArticulosDto;
import tuval.api.web.reportes.inventario.articulos.request.ArticulosRequest;

public interface ArticulosService {
  List<ArticulosDto> consultar(ArticulosRequest request);
}
