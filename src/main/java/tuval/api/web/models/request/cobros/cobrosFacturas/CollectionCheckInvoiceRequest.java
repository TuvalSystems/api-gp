package tuval.api.web.models.request.cobros.cobrosFacturas;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionCheckInvoiceRequest {
  @NotNull
  @JsonProperty("cabecera")
  @Valid
  private CollectionCheckHeaderRequest cabecera;

  @JsonProperty("detalle")
  @Valid
  private List<CollectionDetailInvoicesRequest> detalle = List.of();

  @JsonProperty("cheques")
  @Valid
  private List<CollectionCheckRequest> cheques = List.of();
}
