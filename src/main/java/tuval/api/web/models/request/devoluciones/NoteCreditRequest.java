package tuval.api.web.models.request.devoluciones;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreditRequest {
  private HeaderNoteCreditRequest cabecera;
  private List<DetailNoteCreditRequest> detalle;
}
