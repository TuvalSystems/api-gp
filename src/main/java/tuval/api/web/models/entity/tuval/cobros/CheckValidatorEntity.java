package tuval.api.web.models.entity.tuval.cobros;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CheckValidatorEntity {

    @JsonProperty("RUC")
    private String RUC;

    @JsonProperty("CUPO_ASIGNADO")
    private double CUPO_ASIGNADO;

    @JsonProperty("CUPO_DISPONIBLE_GP")
    private double CUPO_DISPONIBLE_GP;

    @JsonProperty("SUSPENDIDO")
    private int SUSPENDIDO;
    

}
