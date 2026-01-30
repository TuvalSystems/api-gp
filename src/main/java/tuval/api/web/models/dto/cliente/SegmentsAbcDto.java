package tuval.api.web.models.dto.cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SegmentsAbcDto {
    private String item;
    private String valorVenta;
    private String peso;
    private String frecuencia;
    private String tuval;
    private String clase;

}
