package tuval.api.web.ventas.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class responseInvoiceVtaDto {
    
    public String documentoOrigen;
    public String idTipoDocumento;
    public String codigo;
    public String mensaje;
    public String tipoDocGenerado;
    public String numeroDocGenerado;
  
}
