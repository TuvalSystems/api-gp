package tuval.api.web.models.request.otros.pedidos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersHeadersRequest {
    private Long dexRowId;
    private String pedido;
    private String userIdCreator;
    private String condIdOriginal;

}
