package tuval.api.web.models.request.devoluciones;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class DevolutionRequest {
    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private HeaderDevolutionRequest cabecera;

    @JsonProperty("detalle")
    @Valid
    private List<DetailDevolutionRequest> detalle = List.of();

    public HeaderDevolutionRequest getCabecera() {
        return cabecera;
    }

    public void setCabecera(HeaderDevolutionRequest cabecera) {
        this.cabecera = cabecera;
    }

    public List<DetailDevolutionRequest> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetailDevolutionRequest> detalle) {
        this.detalle = detalle;
    }


}
