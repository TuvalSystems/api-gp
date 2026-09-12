package tuval.api.web.bodega.stock.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class StockWarehouseParamsRequest {

    @NotNull
    public String articulo;
    @Valid
    public String bodega;

    public String getArticulo() {
        return articulo;
    }

    public void setArticulo(String articulo) {
        this.articulo = articulo;
    }

    public String getBodega() {
        return bodega;
    }

    public void setBodega(String bodega) {
        this.bodega = bodega;
    }

}
