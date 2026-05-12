package tuval.api.web.models.request.otros.pedidos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersDetailRequest {
    private Long pedidoDetalleId; 
    private Long pedidoId; 

    private String pedido;
    private String artCodigo;
    private Integer orden;

    private Long dexRowId;
    private String bodega;

    private BigDecimal descuento;
}
