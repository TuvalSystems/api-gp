package tuval.api.web.bodega.stock.service.dynamics;

import org.springframework.stereotype.Service;

import tuval.api.web.bodega.stock.dao.dynamics.WarehouseDynamicsDao;
import tuval.api.web.bodega.articulos.dto.ItemExpoDto;
import tuval.api.web.bodega.articulos.request.ItemsExpoRequest;

@Service
public class WarehouseDynamicsService {

    private final WarehouseDynamicsDao warehouseDynamicsDao;

    public WarehouseDynamicsService(WarehouseDynamicsDao warehouseDynamicsDao) {
        this.warehouseDynamicsDao = warehouseDynamicsDao;
    }


    public ItemExpoDto findItemExpoByPtoVta(ItemsExpoRequest req) {
        return warehouseDynamicsDao.findItemExpoByPtoVta(req);
    }
}
