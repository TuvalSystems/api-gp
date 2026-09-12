package tuval.api.web.bodega.recepciones.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusReceptionOrderItemLDto {
    private String docuOrigen;
    private String idTipo;
    private String estado;
}
