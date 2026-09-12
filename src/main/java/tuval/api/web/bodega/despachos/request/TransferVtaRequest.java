package tuval.api.web.bodega.despachos.request;

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
public class TransferVtaRequest {
    @NotNull
    @JsonProperty("cabecera")
    @Valid
    private HeaderGuideTransferVtaRequest cabecera;

    @JsonProperty("detalle")
    @Valid
    private List<DetailGuideTransferVtaRequest> detalle = List.of();

}
