package tuval.api.web.models.request.cliente;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public class SellerRequest {

    @NotBlank
    @Valid
    @JsonProperty("usuario")
    public String usuario;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
