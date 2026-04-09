package tuval.api.web.models.service.dynamics.bodegas;

import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.dynamics.bodegas.WarehouseDynamicsDao;
import tuval.api.web.models.dto.articulos.ItemExpoDto;
import tuval.api.web.models.request.bodegas.articulos.ItemsExpoRequest;

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
