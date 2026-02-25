package tuval.api.web.models.request.bodegas.guiaremision;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateGuideSalesTransferedRequest {
    private String orderNumber;
    private String origen;
    private String destino;
    private Integer idGuiaRemision;
    private String guiaRemision;
    private String tipo;
}
