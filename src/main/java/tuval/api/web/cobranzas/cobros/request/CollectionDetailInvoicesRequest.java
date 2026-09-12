package tuval.api.web.cobranzas.cobros.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDetailInvoicesRequest {

  @NotBlank public String recibo;
  @NotBlank public String idcliente;
  @NotBlank public String factura;
  @NotNull @Positive public BigDecimal monto;
  public String observacion;
}
