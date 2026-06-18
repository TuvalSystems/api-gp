package tuval.api.web.models.dto.articulos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSegmentConfigurationDto {
    
    private Integer codigoSubsegmento;
    private String segmentoCliente;
    private Integer codigoClaseAtributoItem;
    private String codigoCategoria;
    private String codigoLinea;
    private String codigoClase;
    private String codigoMarca;
    private Integer codigoNivel;
    private Integer nivel;
    private String descripcionNivel;
}
    