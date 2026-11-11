package tuval.api.web.reportes.inventario.varianza.service.dynamics;

import java.util.List;
import tuval.api.web.reportes.inventario.varianza.dto.VarianzaDto;
import tuval.api.web.reportes.inventario.varianza.request.VarianzaRequest;

public interface VarianzaService {
  List<VarianzaDto> consultar(VarianzaRequest request);
}
