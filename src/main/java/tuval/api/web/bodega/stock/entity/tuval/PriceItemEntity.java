package tuval.api.web.bodega.stock.entity.tuval;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceItemEntity {
    private String item;
    private String curncyId;
    private String uofm;
    private String idLista;
    private BigDecimal valor;
}