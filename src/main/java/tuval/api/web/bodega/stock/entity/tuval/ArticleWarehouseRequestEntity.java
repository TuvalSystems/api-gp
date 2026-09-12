package tuval.api.web.bodega.stock.entity.tuval;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ArticleWarehouseRequestEntity {
    private String articulo;
    private String bodega;

}
