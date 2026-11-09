package tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend;

import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.cotizacionborrador.client.CotizacionBorradorClient;
import tuval.api.web.reportes.cotizacionborrador.request.CotizacionBorradorReporteRequest;

@Service
public class CotizacionBorradorReporteService {
  private final CotizacionBorradorClient client;
  public CotizacionBorradorReporteService(CotizacionBorradorClient client) { this.client = client; }

  public CotizacionBorradorClient.Resultado crear(CotizacionBorradorReporteRequest request, List<String> authorization) {
    if (authorization == null || authorization.size() != 1 || authorization.getFirst().length() > 4103
        || !authorization.getFirst().matches("(?i:Bearer) [A-Za-z0-9._~+|/=-]{1,4096}")) {
      throw new CotizacionBorradorException(401);
    }
    var codigos = new HashSet<String>();
    for (var item : request.items()) {
      if (!codigos.add(item.codigo())) throw new IllegalArgumentException();
    }
    return client.crear(request, authorization.getFirst());
  }
}
