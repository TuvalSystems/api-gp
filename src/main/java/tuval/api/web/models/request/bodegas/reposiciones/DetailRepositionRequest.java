package tuval.api.web.models.request.bodegas.reposiciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailRepositionRequest {
    private String idCompany;
    private String numDoc;
}
