package tuval.api.web.reportes.ven11.service.dynamics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.ven11.dao.dynamics.Ven11ReporteDao;
import tuval.api.web.reportes.ven11.dto.Ven11ReporteDto;
import tuval.api.web.reportes.ven11.request.Ven11ReporteRequest;

@Service
public class Ven11ReporteService {
  private final Ven11ReporteDao dao;

  public Ven11ReporteService(Ven11ReporteDao dao) {
    this.dao = dao;
  }

  public List<Ven11ReporteDto> consultar(Ven11ReporteRequest request) {
    if (request == null || !request.isConsultaValida()) {
      throw new IllegalArgumentException("Parámetros inválidos del reporte spVen11");
    }
    var result = new ArrayList<>(dao.consultar(request));
    // The SP has no ORDER BY. Sort the complete, size-checked report, without pagination.
    result.sort(Comparator.comparing(Ven11ReporteDto::tx_nom,
        Comparator.nullsFirst(Comparator.naturalOrder())));
    return result;
  }
}
