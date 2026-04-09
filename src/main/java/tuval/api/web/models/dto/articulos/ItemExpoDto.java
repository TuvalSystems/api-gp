package tuval.api.web.models.dto.articulos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemExpoDto {
    private String item;
    private BigDecimal cantidad;
    private String puntoVenta;
    private String bodega;

}
