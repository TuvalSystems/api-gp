package tuval.api.web.ventas.facturacion.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConsultInvoiceRequest {

    @JsonProperty("documentoOrigen")
    public String documento;

    @JsonProperty("tipoDocumento")
    public String tipoDocumento;

}
