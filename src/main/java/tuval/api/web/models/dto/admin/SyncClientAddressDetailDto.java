package tuval.api.web.models.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncClientAddressDetailDto {
    
    private String idDireccion;
    private String direccion;
    private String referencia;
    private String ciudad;
    private String provincia;
    private String pais ;
    private String territorio;
    private String vendedor;
    private String telefono;
    private String contacto;
    private String correoSri;
    private String correoInterno;
}
