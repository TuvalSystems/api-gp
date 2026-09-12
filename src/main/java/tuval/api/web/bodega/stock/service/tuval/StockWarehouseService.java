package tuval.api.web.bodega.stock.service.tuval;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tuval.api.web.bodega.stock.dao.dynamics.WarehouseDynamicsDao;
import tuval.api.web.bodega.stock.dao.tuval.StockWarehouseDao;
import tuval.api.web.clientes.dto.SegmentsAbcDto;
import tuval.api.web.bodega.stock.entity.tuval.ListPriceItemResponseEntity;
import tuval.api.web.bodega.stock.entity.tuval.StockWarehouseEntity;
import tuval.api.web.bodega.stock.entity.tuval.StockWarehouseResponseEntity;
import tuval.api.web.bodega.stock.request.StockWarehouseParamsRequest;

@Service
public class StockWarehouseService {

    private final  StockWarehouseDao stockWarehouseDao;
    private final WarehouseDynamicsDao warehouseDynamics;
    

    public StockWarehouseService(StockWarehouseDao stockWarehouseDao, WarehouseDynamicsDao warehouseDynamics) {
        this.stockWarehouseDao = stockWarehouseDao;
        this.warehouseDynamics = warehouseDynamics;
    }
    public StockWarehouseEntity consultarStockBodega(StockWarehouseParamsRequest request) {
        String type = "individual";
       return stockWarehouseDao.executeSpConsultStockWarehouse(request, type );
        
    }

    public StockWarehouseEntity consultarStockBodegaCompartimento(String articulo, String bodega, String compartimento) {
       
        return stockWarehouseDao.executeSpConsultStockByWarehouseAndCompartment(articulo, bodega, compartimento);
    }

    public StockWarehouseResponseEntity consultarStockBodegaBatch(List<StockWarehouseParamsRequest> requests) {
        List<StockWarehouseEntity> acumulado  = new ArrayList<>();
        String type = "lote";
         for (StockWarehouseParamsRequest req : requests) {
            // Tu DAO devuelve UN StockWarehouseEntity (el primero del resultset)
            StockWarehouseEntity item = stockWarehouseDao.executeSpConsultStockWarehouse(req, type);
            acumulado.add(item);
        }
         StockWarehouseResponseEntity resp = new StockWarehouseResponseEntity();
        resp.setStockWarehouseList(acumulado);
        return resp;

    }


    public ListPriceItemResponseEntity getListPricesService(String item) {
        return stockWarehouseDao.executeSpConsultListPrices(item);
    }


    public List<SegmentsAbcDto> getListSegmentacionAbcService(List<String> items) {
        return warehouseDynamics.findAllSegmentsAbcDao(items);
    }

}
