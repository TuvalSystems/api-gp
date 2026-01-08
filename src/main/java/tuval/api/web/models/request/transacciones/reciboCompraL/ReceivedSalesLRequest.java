package tuval.api.web.models.request.transacciones.reciboCompraL;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedSalesLRequest {

    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private HeaderReceivedLRequest cabecera;

    @JsonProperty("detalle")
    @Valid
    private List<DetailReceivedLRequest> detalle = List.of();

  
}
