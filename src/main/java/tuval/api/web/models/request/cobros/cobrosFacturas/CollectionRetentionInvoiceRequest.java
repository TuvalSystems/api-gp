package tuval.api.web.models.request.cobros.cobrosFacturas;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRetentionInvoiceRequest {

    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private CollectionHeaderRequest cabecera;

    @NotNull
    @JsonProperty("detalle")
    @Valid
    private CollectionDetailInvoicesRequest detalle;
}
