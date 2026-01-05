package tuval.api.web.models.dto.devolucion;

import java.util.List;
import lombok.Data;

@Data
public class ItemBinResponseDto {
  private ItemBinDevolutionDto detalle;
  private List<ItemBinDevolutionDto> lista;
}
