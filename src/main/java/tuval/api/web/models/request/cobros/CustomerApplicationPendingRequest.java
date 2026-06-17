package tuval.api.web.models.request.cobros;

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
