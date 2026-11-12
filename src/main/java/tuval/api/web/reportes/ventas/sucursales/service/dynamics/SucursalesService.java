package tuval.api.web.reportes.ventas.sucursales.service.dynamics;

import java.util.List;
import tuval.api.web.reportes.ventas.sucursales.dto.VentasComparativasDto;
import tuval.api.web.reportes.ventas.sucursales.request.VentasComparativasRequest;

public interface SucursalesService {
  List<VentasComparativasDto> consultarComparativo(VentasComparativasRequest request);
}
