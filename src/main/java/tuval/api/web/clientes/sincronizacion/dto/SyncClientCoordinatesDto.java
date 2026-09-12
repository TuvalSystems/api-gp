package tuval.api.web.clientes.sincronizacion.dto;

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
