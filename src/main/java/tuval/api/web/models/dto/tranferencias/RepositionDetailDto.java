package tuval.api.web.models.dto.tranferencias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositionDetailDto {
    
    private String documento;
    private String origen ;
    private String destino;
    private String item;
    private String nombreItem;
    private String cantidad;
    private String trdUbic;
}
