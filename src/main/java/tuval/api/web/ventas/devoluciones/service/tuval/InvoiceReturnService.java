package tuval.api.web.ventas.devoluciones.service.tuval;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import tuval.api.web.ventas.devoluciones.dto.ItemBinResponseDto;
import tuval.api.web.ventas.devoluciones.dto.notacredito.DocumentNcDto;
import tuval.api.web.ventas.devoluciones.entity.tuval.DevolutionResponse;
import tuval.api.web.ventas.devoluciones.request.DetailNoteCreditRequest;
import tuval.api.web.ventas.devoluciones.request.DevolutionRequest;
import tuval.api.web.ventas.devoluciones.request.HeaderNoteCreditRequest;

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
