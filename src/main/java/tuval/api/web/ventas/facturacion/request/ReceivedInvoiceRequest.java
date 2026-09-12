package tuval.api.web.ventas.facturacion.request;

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
public class ReceivedInvoiceRequest {
    
    
    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private HeaderInvoiceRequest cabecera;

    @JsonProperty("detalle")
    @Valid
    private List<DetailInvoiceRequest> detalle = List.of();

   
}
