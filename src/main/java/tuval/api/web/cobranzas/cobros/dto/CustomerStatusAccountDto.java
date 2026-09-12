package tuval.api.web.cobranzas.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusAccountDto {
  private String identificacion;
  private String tipo;
  private String documento;
  private LocalDate fechaEmision;
  private LocalDate fechaVencimiento;
  private String condicionPago;
  private BigDecimal montoOriginal;
  private BigDecimal abono;
  private BigDecimal saldoPendiente;
  private float dias;
  private String observacion;
}
