package tuval.api.web.models.service.tuval.bodegas;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.bodegas.StockWarehouseDao;
import tuval.api.web.models.dto.bodegas.StockWarehouseParamsRequest;
import tuval.api.web.models.entity.tuval.bodegas.ArticleWarehouseRequestEntity;
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

    public List<StockWarehouseEntity> consultarStockBodegaCompartimento(String articulo, String bodega, String compartimento) {
        List<StockWarehouseEntity> resp = new ArrayList<>();
        try {
            StockWarehouseResponseEntity response = stockWarehouseDao.consultStockByWarehouseAndCompartment(articulo, bodega, compartimento);
            if (response != null && response.getStockWarehouseList() instanceof List) {
                Object data = response.getStockWarehouseList();
                List<StockWarehouseEntity> tempResponse = new ArrayList<>();
                if (data instanceof List<?>) {
                    for (Object item : (List<?>) data) {
                        if (item instanceof StockWarehouseEntity) {
                            tempResponse.add((StockWarehouseEntity) item);
                        }
                    }
                }
                resp = tempResponse;
            }
        } catch (Exception e) {
            System.err.println("Error en servicio: " + e.getMessage());
        }
        return resp;
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
