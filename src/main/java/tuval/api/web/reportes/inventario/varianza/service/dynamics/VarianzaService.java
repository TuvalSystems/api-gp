package tuval.api.web.reportes.inventario.varianza.service.dynamics;

import java.util.List;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.inventario.varianza.dao.dynamics.VarianzaDao;
import tuval.api.web.reportes.inventario.varianza.dto.VarianzaDto;
import tuval.api.web.reportes.inventario.varianza.request.VarianzaRequest;

@Service
public class VarianzaService {
  private final VarianzaDao dao;

  public VarianzaService(VarianzaDao dao) {
    this.dao = dao;
  }

  public List<VarianzaDto> consultar(VarianzaRequest request) {
    if (request == null || !request.isConsultaValida()) {
      throw new IllegalArgumentException("Parametros invalidos del reporte spInv60");
    }
    return dao.consultar(request);
  }
}
