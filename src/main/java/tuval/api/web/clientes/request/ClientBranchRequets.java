package tuval.api.web.clientes.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientBranchRequets {
    private String ruc;
    private String idDireccion;
    private String direccion;
    private String estado;
    private String usuarioIngreso;
    private String fechaIngreso;
    private String usuarioModificacion;
    private String fechaModificacion;
    private Integer modo;
}
