package tuval.api.web.models.dto.cobros;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionSummaryDTO {
  private String ruc;
  private String documento;
  private String tipo;
  private String observacion;
  public LocalDate fechaEmision;
  private BigDecimal montoOriginal;
  private BigDecimal saldoCliente;
}
