package tuval.api.web.models.service.tuval.devoluciones;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import tuval.api.web.models.dto.devolucion.DevolutionRequest;
import tuval.api.web.models.entity.tuval.devoluciones.DevolutionResponse;

public interface InvoiceReturnService {

  ResponseEntity<Map<String, Object>> generacionNotaCreditoVTA(
      Map<String, Object> cabecera, List<Map<String, Object>> detalle);

  ResponseEntity<Map<String, Object>> getResponseCreditNote(String document, String tipo);

  public DevolutionResponse generateAjustmentDevolutionService(DevolutionRequest request);

  ResponseEntity<Map<String, Object>> getResponseItemBin(String item, String bodega);

  ResponseEntity<Map<String, Object>> getClientGroupEconomicService(String ruc);
}
