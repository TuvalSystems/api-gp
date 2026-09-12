package tuval.api.web.ventas.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderHeaderDto {
    private String docOrigen;
    private Integer idTipo;
    private String ptoVta;
    private String docGenerado;
    private Long dexRowId;
    private String bodega;

}
