package tuval.api.web.models.entity.sucursales;

import java.util.List;

public class BranchesClientsResponseEntity {
    
    private List<BranchesClientEntity> data;
    
    public BranchesClientsResponseEntity() {
    }
    public List<BranchesClientEntity> getData() {
        return data;
    }
    public void setData(List<BranchesClientEntity> data) {
        this.data = data;
    }
}
