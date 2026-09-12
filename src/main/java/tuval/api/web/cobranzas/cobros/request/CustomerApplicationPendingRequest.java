package tuval.api.web.cobranzas.cobros.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerApplicationPendingRequest {
    private String documento;
    private String ruc;
}
