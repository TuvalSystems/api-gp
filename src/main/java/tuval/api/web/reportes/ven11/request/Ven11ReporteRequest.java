package tuval.api.web.reportes.ven11.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Arrays;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.BindParam;

public record Ven11ReporteRequest(
    @BindParam("FechaIni") @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIni,
    @BindParam("FechaFin") @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
    @BindParam("Sucursales") @NotBlank @Size(max = 8000) String sucursales) {

  // HTTP input bound, not a declaration of the SQL VARCHAR(MAX) capacity.
  public static final int MAX_SUCURSALES_LENGTH = 8000;

  @AssertTrue
  public boolean isConsultaValida() {
    return fechaIni != null && fechaFin != null
        && fechaIni.getYear() >= 1 && fechaFin.getYear() <= 9999
        && !fechaIni.isAfter(fechaFin)
        && sucursales != null && !sucursales.isBlank()
        && sucursales.length() <= MAX_SUCURSALES_LENGTH
        && sucursales.chars().noneMatch(Character::isISOControl)
        && Arrays.stream(sucursales.split(",", -1))
            .allMatch(code -> !code.isBlank() && code.equals(code.strip()));
  }
}
