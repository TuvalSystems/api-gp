package tuval.api.web.models.entity.tuval.bodegas;

import java.util.List;

public class StockWarehouseResponseEntity {
    
    private List<StockWarehouseEntity> stockWarehouseList;

    public List<StockWarehouseEntity> getStockWarehouseList() {
        return stockWarehouseList;
    }

    public void setStockWarehouseList(List<StockWarehouseEntity> stockWarehouseList) {
        this.stockWarehouseList = stockWarehouseList;
    }

}
