package tuval.api.web.models.entity.tuval.bodegas;

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
