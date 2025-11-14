package tuval.api.web.models.entity.tuval.bodegas;

import java.util.List;

public class ListPriceItemResponseEntity {

 private List<PriceItemEntity> data;

 public List<PriceItemEntity> getData() {
    return data;
 }

 public void setData(List<PriceItemEntity> data) {
    this.data = data;
 }

}
