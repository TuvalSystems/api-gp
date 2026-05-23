package tuval.api.web.models.request.otros.pedidos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersHeadersRequest {
     private Long orderId;

    private Long dexRowId;
    private String docGenerado;
    private String pedido;
    private String ptoVta;
    private String bodega;

    private String userApprovalId;

}
