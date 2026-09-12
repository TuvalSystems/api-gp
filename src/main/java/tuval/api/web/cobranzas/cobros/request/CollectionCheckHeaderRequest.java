package tuval.api.web.cobranzas.cobros.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionCheckHeaderRequest {
  @NotBlank private String numeroRecibo;
  @NotBlank private String ruc;
  @NotBlank private String fechaCobro;
  private String tipoCobro;
  private String observacion;
  private String solicitudDescuento;
  private String idVendedor;
  @NotNull private BigDecimal montoCobro;
  private BigDecimal saldoPendiente;
}
