package tuval.api.web.bodega.articulos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticlePurchaseClassificationDto  {
    private String item;
    private String marca;
    private String categoriaCompra;
    private String lineaCompra;
    private String claseCompra;
}
