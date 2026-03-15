package tuval.api.web.models.request.bodegas.guiaremision.crearGuia;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class DeliveryNoteRequest {
    
    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private HeaderDeliveryNoteRequest cabecera;

     @NotNull
    @JsonProperty("destinatario")
    private DestinationDeliveryNoteRequest destinatario;


    @JsonProperty("detalle")
    @Valid
    private List<DetailDeliveryNoteRequest> detalle = List.of();


    public HeaderDeliveryNoteRequest getCabecera() {
        return cabecera;
    }


    public void setCabecera(HeaderDeliveryNoteRequest cabecera) {
        this.cabecera = cabecera;
    }


    public DestinationDeliveryNoteRequest getDestinatario() {
        return destinatario;
    }


    public void setDestinatario(DestinationDeliveryNoteRequest destinatario) {
        this.destinatario = destinatario;
    }


    public List<DetailDeliveryNoteRequest> getDetalle() {
        return detalle;
    }


    public void setDetalle(List<DetailDeliveryNoteRequest> detalle) {
        this.detalle = detalle;
    }
}
