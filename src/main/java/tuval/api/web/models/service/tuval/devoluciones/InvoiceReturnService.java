package tuval.api.web.models.service.tuval.devoluciones;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import tuval.api.web.models.dto.devolucion.ItemBinResponseDto;
import tuval.api.web.models.dto.devolucion.notaCredito.DocumentNcDto;
import tuval.api.web.models.entity.tuval.devoluciones.DevolutionResponse;
import tuval.api.web.models.request.devoluciones.DetailNoteCreditRequest;
import tuval.api.web.models.request.devoluciones.DevolutionRequest;
import tuval.api.web.models.request.devoluciones.HeaderNoteCreditRequest;

public interface InvoiceReturnService {

  ResponseEntity<Map<String, Object>> generacionNotaCreditoVTA(
      Map<String, Object> cabecera, List<Map<String, Object>> detalle);

  // ResponseEntity<Map<String, Object>> getResponseCreditNote(String document, String tipo);

  public DevolutionResponse generateAjustmentDevolutionService(DevolutionRequest request);

  ItemBinResponseDto getResponseItemBin(String item, String bodega);

  ResponseEntity<Map<String, Object>> getClientGroupEconomicService(String ruc);

  /** pruebas generacion de nota de credito */
  DocumentNcDto getExistenceNoteCreditService(String document, String tipo);

  DocumentNcDto generateNoteCreditService(
      HeaderNoteCreditRequest cab, List<DetailNoteCreditRequest> det);
}
