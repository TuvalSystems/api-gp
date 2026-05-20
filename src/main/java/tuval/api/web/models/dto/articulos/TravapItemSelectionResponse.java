package tuval.api.web.models.dto.articulos;

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
