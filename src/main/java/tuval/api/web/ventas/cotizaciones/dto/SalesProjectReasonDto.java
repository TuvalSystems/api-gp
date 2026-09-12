package tuval.api.web.ventas.cotizaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesProjectReasonDto {

  private Integer coMotproven;
  private String txDescor;
  private String txDeslar;
  private String txObs;
}
