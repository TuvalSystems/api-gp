package tuval.api.web.models.dto.cobros;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInvoiceCheckReceiptDto {
  private String receiptNum; // cq_numrecibo
  private String ruc; // cq_idcliente
  private String documentGp; // cf_cobrogp
  private String invoice; // cf_numfac
  private String checkNumber; // cq_numchq
  private BigDecimal checkAmount; // montoCheque
  private BigDecimal appliedAmount; // cf_montoapl
  private String status; // cq_estado
  private LocalDate modifiedAt; // cf_fecmod
}
