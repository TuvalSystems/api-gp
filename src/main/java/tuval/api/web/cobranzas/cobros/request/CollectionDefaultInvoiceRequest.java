package tuval.api.web.cobranzas.cobros.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;


import jakarta.validation.Valid;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor

public class CollectionDefaultInvoiceRequest {
    
    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private CollectionHeaderRequest cabecera;

    @JsonProperty("detalle")
    @Valid
    private List<CollectionDetailInvoicesRequest> detalle = List.of();

}
