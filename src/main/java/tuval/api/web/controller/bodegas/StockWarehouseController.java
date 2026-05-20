package tuval.api.web.controller.bodegas;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.cliente.SegmentsAbcDto;
import tuval.api.web.models.entity.tuval.bodegas.ListPriceItemResponseEntity;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseEntity;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseResponseEntity;
import tuval.api.web.models.request.bodegas.ItemRequest;
import tuval.api.web.models.request.bodegas.StockWarehouseParamsRequest;
import tuval.api.web.models.request.bodegas.articulos.ItemsExpoRequest;
import tuval.api.web.models.service.dynamics.bodegas.WarehouseDynamicsService;
import tuval.api.web.models.service.tuval.bodegas.StockWarehouseService;

@RestController
@RequestMapping("/api")
public class StockWarehouseController {

  private final StockWarehouseService stockWarehouseService;
  private final WarehouseDynamicsService warehouseDynamicsService;

  public StockWarehouseController(
      WarehouseDynamicsService warehouseDynamicsService,
      StockWarehouseService stockWarehouseService) {
    this.warehouseDynamicsService = warehouseDynamicsService;
    this.stockWarehouseService = stockWarehouseService;
  }

  @CrossOrigin
  @PostMapping("/consultar-stock-bodega")
  public ResponseEntity<Map<String, Object>> consultaStockBodegas(
      @Valid @RequestBody StockWarehouseParamsRequest request) {
    try {
      StockWarehouseEntity response = stockWarehouseService.consultarStockBodega(request);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
    } catch (Exception e) {
      e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @RequestMapping("/consultar-stock-articulo-bodega-compartimento")
  public ResponseEntity<Map<String, Object>> consultaStockBodegaCompartimento(
      @RequestParam String articulo,
      @RequestParam String bodega,
      @RequestParam String compartimento) {
    try {
      StockWarehouseEntity response = stockWarehouseService.consultarStockBodegaCompartimento(articulo, bodega,
          compartimento);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
    } catch (Exception e) {
      e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  /**
   * utiliza el mismo sp que consultar stock bodega pero en este caso lo hace por
   * lote
   */
  @CrossOrigin
  @PostMapping("/consultar-stock-bodega-lotes")
  public ResponseEntity<Map<String, Object>> consultaStockBodegasBatch(
      @RequestBody List<StockWarehouseParamsRequest> requests) {
    try {
      StockWarehouseResponseEntity response = stockWarehouseService.consultarStockBodegaBatch(requests);
      return ResponseJsonUtils.buildSuccessWithData("Lote Obtenido exitosamente", response);
    } catch (Exception e) {
      e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/consultar-lista-precio-articulos")
  public ResponseEntity<Map<String, Object>> getListaPrecioArticulos(
      @Valid @RequestParam String item) {

    if (item.isEmpty()) {
      return ResponseJsonUtils.buildError("El campo de items no puede estar vacio.");
    }
    try {
      ListPriceItemResponseEntity response = stockWarehouseService.getListPricesService(item);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response.getData());

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("consultar-lista-segmentacion-articulo-abc")
  public ResponseEntity<Map<String, Object>> getMethodName(@Valid @RequestBody ItemRequest request) {
    if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
      return ResponseJsonUtils.buildError("El campo de items no puede estar vacío.");
    }
    try {
      List<SegmentsAbcDto> response = stockWarehouseService.getListSegmentacionAbcService(request.getItems());
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("consultas/items-exposiciones")
  public ResponseEntity<Map<String, Object>> obtenerStockItemExposiciones(@Valid @RequestBody ItemsExpoRequest req, BindingResult result) {
    
    if (result.hasErrors()) {
        String mensaje = result.getFieldError().getDefaultMessage();
        return ResponseJsonUtils.buildError(mensaje);
    }
    
    try {
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa",
          warehouseDynamicsService.findItemExpoByPtoVta(req));
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }

  }

}
