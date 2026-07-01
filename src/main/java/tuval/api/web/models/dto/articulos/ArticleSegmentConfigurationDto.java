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
    private Integer nivelCategoria;
    private String codigoLinea;
    private Integer nivelLinea;
    private String codigoClase;
    private Integer nivelClase;
    private String codigoMarca;
    private Integer codigoNivel;
    private Integer nivelMarca;
    private String descripcionNivel;
}
    