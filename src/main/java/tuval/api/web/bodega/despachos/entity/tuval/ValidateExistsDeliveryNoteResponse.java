package tuval.api.web.bodega.despachos.entity.tuval;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateExistsDeliveryNoteResponse {

  private int codRet; // @codret OUT
  private String mensaje; // @msj OUT

}
