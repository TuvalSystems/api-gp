package tuval.api.web.bodega.recepciones.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class validateReceidLRequest {

    @NotBlank
    @JsonProperty("documento")
    public String documento;

    @NotNull
    @JsonProperty("tipo")
    public String tipo;

}
