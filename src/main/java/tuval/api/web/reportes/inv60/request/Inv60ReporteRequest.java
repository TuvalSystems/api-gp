package tuval.api.web.reportes.inv60.request;

import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.BindParam;

public record Inv60ReporteRequest(
    @BindParam("NumeroDocumento") @Size(max = 50) String numeroDocumento) {

  public boolean isConsultaValida() {
    return numeroDocumento == null || numeroDocumento.length() <= 50;
  }
}
