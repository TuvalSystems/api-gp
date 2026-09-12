package tuval.api.web.ventas.pedidos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailListRequest {
     private List<OrderDetailWebRequest> detalles;
}
