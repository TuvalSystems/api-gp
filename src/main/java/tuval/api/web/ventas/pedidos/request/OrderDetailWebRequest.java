package tuval.api.web.ventas.pedidos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailWebRequest {
    private String pedido;
    private String artCodigo;
    private Integer orden;
}
