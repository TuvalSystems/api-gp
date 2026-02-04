package tuval.api.web.models.request.otros.pedidos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersDetailRequest {
    private Long dexRowId;
    private String bodega;
    private String artWeb;
    private String artCodigo;
    private String descripcionOrginal;
    private BigDecimal valorOrginal;
    private BigDecimal descuentoOrginal;
    private String docGenerado;
}
