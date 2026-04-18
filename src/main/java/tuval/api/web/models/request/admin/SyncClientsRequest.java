package tuval.api.web.models.request.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncClientsRequest {
    private String cliente;
    private String fechaCreacion;
    private String fechaModif;    

}
