package tuval.api.web.bodega.transferencias.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TransferCompartmentOrdersRequest {
  @NotBlank(message = "El origen es obligatorio")
  private String origen;

  @NotBlank(message = "El destino es obligatorio")
  private String destino;

  @NotBlank(message = "La bodega es obligatoria")
  private String bodega;

  @NotBlank(message = "El item es obligatorio")
  private String item;

  @NotNull(message = "La cantidad es obligatoria")
  @Positive(message = "La cantidad debe ser mayor a 0")
  private BigDecimal cantidad;

  @NotNull(message = "La fecha es obligatoria")
  private LocalDate fecha;
}
