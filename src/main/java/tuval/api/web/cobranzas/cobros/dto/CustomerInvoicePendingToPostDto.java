package tuval.api.web.cobranzas.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInvoicePendingToPostDto {
  private String numeroRecibo; // cq_numrecibo
  private String ruc; // cq_idcliente
  private String documentoGp; // cf_cobrogp
  private String factura; // cf_numfac
  private BigDecimal montoAplicado; // cf_montoapl
  private String estado; // cq_estado
  private LocalDate fechaModificacion; // cf_fecmod
}
