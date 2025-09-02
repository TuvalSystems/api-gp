package tuval.api.web.models.service.tuval.bodegas;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.bodegas.StockWarehouseDao;
import tuval.api.web.models.dto.bodegas.StockWarehouseParamsRequest;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseEntity;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseResponseEntity;

@Service
public class StockWarehouseService {

    @Autowired
    private StockWarehouseDao stockWarehouseDao;

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

}
