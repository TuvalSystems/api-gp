package tuval.api.web.models.dto.devolucion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemBinDevolutionDto {
  public String item;
  public String bodega;
  public String compartimento;
}
