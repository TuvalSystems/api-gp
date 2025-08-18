package tuval.api.web.models.dto.transacciones.reciboCompraL;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ReceivedSalesLRequest {

    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private HeaderReceivedLRequest cabecera;

    @JsonProperty("detalle")
    @Valid
    private List<DetailReceivedLRequest> detalle = List.of();

    public HeaderReceivedLRequest getCabecera() {
        return cabecera;
    }

    public void setCabecera(HeaderReceivedLRequest cabecera) {
        this.cabecera = cabecera;
    }

    public List<DetailReceivedLRequest> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetailReceivedLRequest> detalle) {
        this.detalle = detalle;
    }
}
