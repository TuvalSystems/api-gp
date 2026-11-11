package tuval.api.web.reportes.inventario.varianza.service.dynamics.imp;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.inventario.varianza.dao.dynamics.VarianzaDao;
import tuval.api.web.reportes.inventario.varianza.dto.VarianzaDto;
import tuval.api.web.reportes.inventario.varianza.request.VarianzaRequest;
import tuval.api.web.reportes.inventario.varianza.service.dynamics.VarianzaService;

@Service
@RequiredArgsConstructor
public class VarianzaServiceImp implements VarianzaService {
  private final VarianzaDao varianzaDao;

  @Override
  public List<VarianzaDto> consultar(VarianzaRequest request) {
    if (request == null || !request.isConsultaValida()) {
      throw new IllegalArgumentException("Parametros invalidos del reporte spInv60");
    }
    return varianzaDao.consultar(request);
  }
}
