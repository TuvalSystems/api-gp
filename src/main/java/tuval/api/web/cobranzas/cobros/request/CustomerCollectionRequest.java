package tuval.api.web.cobranzas.cobros.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCollectionRequest {

  @NotBlank(message = "El documento es obligatorio")
  private String documento;

  @NotBlank(message = "El ruc es obligatorio")
  private String ruc;
}
