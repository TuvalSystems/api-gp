package tuval.api.web.models.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncClientCoordinatesDto {
    private String idCliente;
    private String addressCode;
    private String datosCoordenadas;
}
