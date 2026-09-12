package tuval.api.web.bodega.despachos.request;

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
    private String tipoTrx;
    private String tipo;
    private String hashInterno;
}
