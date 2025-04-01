package tuval.api.web.models.entity.sucursales;

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
