package tuval.api.web.models.request.bodegas.guiaRemision;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class TransferVtaRequest {
    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private HeaderGuideTransferVtaRequest cabecera;

    @JsonProperty("detalle")
    @Valid
    private List<DetailGuideTransferVtaRequest> detalle = List.of();

    public HeaderGuideTransferVtaRequest getCabecera() {
        return cabecera;
    }

    public void setCabecera(HeaderGuideTransferVtaRequest cabecera) {
        this.cabecera = cabecera;
    }

    public List<DetailGuideTransferVtaRequest> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetailGuideTransferVtaRequest> detalle) {
        this.detalle = detalle;
    }
}
