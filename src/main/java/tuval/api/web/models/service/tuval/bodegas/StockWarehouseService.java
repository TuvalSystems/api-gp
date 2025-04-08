package tuval.api.web.models.service.tuval.bodegas;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.bodegas.StockWarehouseDao;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseEntity;
import tuval.api.web.models.entity.tuval.bodegas.StockWarehouseResponseEntity;

@Service
public class StockWarehouseService {

    @Autowired
    private StockWarehouseDao stockWarehouseDao;

    public List<StockWarehouseEntity> consultarStockBodega(String articulo, String bodega) {
        List<StockWarehouseEntity> resp = new ArrayList<>();
        try {
            StockWarehouseResponseEntity response = stockWarehouseDao.consultStockWarehouseList(articulo, bodega);
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

}
