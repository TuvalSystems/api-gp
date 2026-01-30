package tuval.api.web.models.dto.cotizaciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDescripcionAlternativaDto {
    
    private String tx_itm ;
    private Integer co_reg;
    private String tx_desLar;
    

}
