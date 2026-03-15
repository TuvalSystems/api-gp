package tuval.api.web.models.dto.tranferencias;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositionHeaderDto {
    
    private String numdoc;
    private String codmotivo;
    private String motivo;
    private String origen;
    private String destino;
    private Date fecha;
}
