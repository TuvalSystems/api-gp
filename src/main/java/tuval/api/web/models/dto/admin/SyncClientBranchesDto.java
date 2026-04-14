package tuval.api.web.models.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncClientBranchesDto {
    private String cliente;
    private String descripcion;
    private List<SyncClientAddressDetailDto> direcciones;
}
