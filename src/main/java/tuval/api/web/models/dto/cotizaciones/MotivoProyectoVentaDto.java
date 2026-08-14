package tuval.api.web.models.dto.cotizaciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotivoProyectoVentaDto {

  private Integer coMotproven;
  private String txDescor;
  private String txDeslar;
  private String txObs;
}
