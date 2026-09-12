package tuval.api.web.cobranzas.cobros.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionCheckRequest {
  @NotBlank private String recibo;
  @NotBlank private String idcliente;
  @NotBlank private String bancocliente;
  @NotBlank private String cuenta;
  @NotBlank private String vencimiento;
  @NotBlank private String cheque;
  @NotNull @Positive private BigDecimal monto;
  private String origen;
  private String observacion;
}
