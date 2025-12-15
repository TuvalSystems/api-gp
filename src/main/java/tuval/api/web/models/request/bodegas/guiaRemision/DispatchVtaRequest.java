package tuval.api.web.models.request.bodegas.guiaRemision;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class DispatchVtaRequest {
    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private HeaderGuideDispatchShipmentRequest cabecera;

    @JsonProperty("detalle")
    @Valid
    private List<DetailGuideTransferVtaRequest> detalle = List.of();

    public HeaderGuideDispatchShipmentRequest getCabecera() {
        return cabecera;
    }

    public void setCabecera(HeaderGuideDispatchShipmentRequest cabecera) {
        this.cabecera = cabecera;
    }

    public List<DetailGuideTransferVtaRequest> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetailGuideTransferVtaRequest> detalle) {
        this.detalle = detalle;
    }
}
