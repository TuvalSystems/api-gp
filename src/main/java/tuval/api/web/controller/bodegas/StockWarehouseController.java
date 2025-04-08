package tuval.api.web.controller.bodegas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseEntity;
import tuval.api.web.models.service.tuval.bodegas.StockWarehouseService;

@RestController
@RequestMapping("/api")
public class StockWarehouseController {

    @Autowired
    private StockWarehouseService stockWarehouseService;

    @CrossOrigin
    @RequestMapping("/consultar-stock-bodega")
    public List<StockWarehouseEntity> consultaStockBodegas(@RequestParam String articulo,
            @RequestParam String bodega) {
        List<StockWarehouseEntity> response = stockWarehouseService.consultarStockBodega(articulo, bodega);
        return response;
    }

    @CrossOrigin
    @RequestMapping("/consultar-stock-articulo-bodega-compartimento")
    public List<StockWarehouseEntity> consultaStockBodegaCompartimento(@RequestParam String articulo,
            @RequestParam String bodega, @RequestParam String compartimento) {
        List<StockWarehouseEntity> response = stockWarehouseService.consultarStockBodegaCompartimento(articulo, bodega, compartimento);
        return response;
    }
   

}
