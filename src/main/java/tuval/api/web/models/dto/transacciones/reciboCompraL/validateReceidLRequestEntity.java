package tuval.api.web.models.dto.transacciones.reciboCompraL;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class validateReceidLRequestEntity {

    @NotBlank
    @JsonProperty("documento")
    public String documento;

    @NotNull
    @JsonProperty("tipo")
    public String tipo;

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
