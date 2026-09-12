package tuval.api.web.clientes.sucursales.entity.tuval;

import java.util.List;

public class CoordinateListResponseEntity {
    
    public List<CoordinateListEntity> data;

    public List<CoordinateListEntity> getData() {
        return data;
    }

    public void setData(List<CoordinateListEntity> data) {
        this.data = data;
    }
}
