package tuval.api.web.bodega.articulos.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravapItemSelectionResponse {
  private TravapItemSelectionDto selectedItem;
  private List<TravapItemOriginDto> originItems;
}
