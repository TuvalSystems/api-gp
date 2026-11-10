package tuval.api.web.reportes.listadoitems.service.dynamics;

import java.util.List;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.listadoitems.dao.dynamics.ListadoItemsReporteDao;
import tuval.api.web.reportes.listadoitems.dto.ListadoItemsReporteDto;
import tuval.api.web.reportes.listadoitems.request.ListadoItemsReporteRequest;

@Service
public class ListadoItemsReporteService {
  private final ListadoItemsReporteDao dao;

  public ListadoItemsReporteService(ListadoItemsReporteDao dao) {
    this.dao = dao;
  }

  public List<ListadoItemsReporteDto> consultar(ListadoItemsReporteRequest request) {
    if (request == null || request.getPage() == null || request.getPage() < 0
        || request.getPage() > 10000 || request.getSize() == null
        || request.getSize() < 1 || request.getSize() > 500
        || !validText(request.getCodigo(), 31) || !validText(request.getDescripcion(), 101)
        || !validText(request.getMarca(), 100) || !validText(request.getMaterial(), 100)) {
      throw new IllegalArgumentException("Parametros invalidos del listado de items");
    }
    return dao.consultar(request);
  }

  private static boolean validText(String value, int maxLength) {
    return value == null || (!value.isBlank() && value.length() <= maxLength);
  }
}
