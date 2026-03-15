package tuval.api.web.models.entity.tuval.guiaremision;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckCodeAccessDeliveryNoteResponse {

    private String claveAcceso;
    private String documentoOrigen;
    private int codigo;
    private String mensaje;
    private String documentoGenerado;
   


    public CheckCodeAccessDeliveryNoteResponse(String claveAcceso, String documentoOrigen, int codigo, String mensaje, String documentoGenerado) {
        this.claveAcceso = claveAcceso;
        this.documentoOrigen = documentoOrigen;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.documentoGenerado = documentoGenerado;
    }
}
