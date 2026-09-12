package tuval.api.web.ventas.devoluciones.dto;

import java.util.List;
import lombok.Data;

@Data
public class ItemBinResponseDto {
  private ItemBinDevolutionDto detalle;
  private List<ItemBinDevolutionDto> lista;
}
