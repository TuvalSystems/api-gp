package tuval.api.web.models.request.cobros.cobrosFacturas;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDetailInvoicesRequest {

  public String recibo;
  public String idcliente;
  public String factura;
  public BigDecimal monto;
  public String observacion;
}
