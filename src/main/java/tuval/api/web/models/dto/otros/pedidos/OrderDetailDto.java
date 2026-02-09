package tuval.api.web.models.dto.otros.pedidos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private String docOrigen;
    private Integer idTipo;
    private String artCodigo;
    private String bodega;
    private Long dexRowId;
    private Integer linea;
}
