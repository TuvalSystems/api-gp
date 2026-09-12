package tuval.api.web.ventas.devoluciones.dto.notacredito;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentNcDto {
  private String docOrigen;
  private String idTipo;
  private String codigo;
  private String mensaje;
  private String docGenerado;
  private LocalDateTime fecha;
  private String tipoDocGenerado;
}
