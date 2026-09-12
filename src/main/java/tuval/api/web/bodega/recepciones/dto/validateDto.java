package tuval.api.web.bodega.recepciones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class validateDto {

    private String codigo;
    private String mensaje;
    private String tipoDocGenerado;
    private String numDocGenerado;


}
