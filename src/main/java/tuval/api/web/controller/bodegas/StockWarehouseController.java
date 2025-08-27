package tuval.api.web.controller.bodegas;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tuval.api.web.models.entity.tuval.bodegas.ArticleWarehouseRequestEntity;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseEntity;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseResponseEntity;
import tuval.api.web.models.service.tuval.bodegas.StockWarehouseService;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.bodegas.StockWarehouseParamsRequest;

@RestController
@RequestMapping("/api")
public class StockWarehouseController {

    @Autowired
    private StockWarehouseService stockWarehouseService;

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
    public List<StockWarehouseEntity> consultaStockBodegaCompartimento(@RequestParam String articulo,
            @RequestParam String bodega, @RequestParam String compartimento) {
        List<StockWarehouseEntity> response = stockWarehouseService.consultarStockBodegaCompartimento(articulo, bodega,
                compartimento);
        return response;
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

}
