package tuval.api.web.bodega.stock.entity.tuval;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;

public class StockWarehouseResponseEntity {
    
    private List<StockWarehouseEntity> stockWarehouseList;

    @JsonValue
    public List<StockWarehouseEntity> getStockWarehouseList() {
        return stockWarehouseList;
    }

    public void setStockWarehouseList(List<StockWarehouseEntity> stockWarehouseList) {
        this.stockWarehouseList = stockWarehouseList;
    }

}
