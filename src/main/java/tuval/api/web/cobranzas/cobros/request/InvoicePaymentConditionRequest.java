package tuval.api.web.cobranzas.cobros.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvoicePaymentConditionRequest {
  @NotBlank(message = "La factura es obligatoria")
  private String factura;

  @NotBlank(message = "La condicion de pago es obligatoria")
  private String condicionPago;
}
