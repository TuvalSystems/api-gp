package tuval.api.web.bodega.despachos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateGuideDispatchRequest {
    private String orderNumber;
    private String newOrderNumber;
    private String origen;
    private Integer idGuiaRemision;
    private String guiaRemision;
    private String tipo;
    private String tipoTrx;
    private String hashInterno;
}
