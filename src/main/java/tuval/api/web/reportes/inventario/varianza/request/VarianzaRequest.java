package tuval.api.web.reportes.inventario.varianza.request;

import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.BindParam;

public record VarianzaRequest(
    @BindParam("NumeroDocumento") @Size(max = 50) String numeroDocumento) {

  public boolean isConsultaValida() {
    return numeroDocumento == null || numeroDocumento.length() <= 50;
  }
}
