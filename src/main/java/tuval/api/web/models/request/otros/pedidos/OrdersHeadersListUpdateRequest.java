package tuval.api.web.models.request.otros.pedidos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersHeadersListUpdateRequest {
    private List<OrdersHeadersRequest> headers;
}
