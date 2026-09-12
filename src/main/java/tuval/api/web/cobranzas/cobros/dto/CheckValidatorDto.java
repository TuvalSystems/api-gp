package tuval.api.web.cobranzas.cobros.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckValidatorDto {

    private String ruc;
    private double cupoAsignado;
    private double cupoDisponibleGp;
    private int suspendido;
  
  

   

}
