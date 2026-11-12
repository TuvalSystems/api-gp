package tuval.api.web.reportes.ventas.sucursales.service.dynamics.imp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.ventas.sucursales.dao.dynamics.SucursalesDao;
import tuval.api.web.reportes.ventas.sucursales.dto.VentasComparativasDto;
import tuval.api.web.reportes.ventas.sucursales.request.VentasComparativasRequest;
import tuval.api.web.reportes.ventas.sucursales.service.dynamics.SucursalesService;

@Service
@RequiredArgsConstructor
public class SucursalesServiceImp implements SucursalesService {
  private final SucursalesDao sucursalesDao;

  @Override
  public List<VentasComparativasDto> consultarComparativo(VentasComparativasRequest request) {
    if (request == null || !request.isConsultaValida()) {
      throw new IllegalArgumentException("Parámetros inválidos del reporte spVen11");
    }
    var result = new ArrayList<>(sucursalesDao.consultarComparativo(request));
    result.sort(Comparator.comparing(VentasComparativasDto::txNom,
        Comparator.nullsFirst(Comparator.naturalOrder())));
    return result;
  }
}
