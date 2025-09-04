package tuval.api.web.models.dto.transacciones.facturacionVenta;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


public class ReceivedInvoiceRequest {
    
    
    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private HeaderInvoiceRequest cabecera;

    @JsonProperty("detalle")
    @Valid
    private List<DetailInvoiceRequest> detalle = List.of();

    public HeaderInvoiceRequest getCabecera() {
        return cabecera;
    }

    public void setCabecera(HeaderInvoiceRequest cabecera) {
        this.cabecera = cabecera;
    }

    public List<DetailInvoiceRequest> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetailInvoiceRequest> detalle) {
        this.detalle = detalle;
    }
}
