package tuval.api.web.reportes.inventario.articulos.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResolucionArticulosDto(String texto, BigDecimal cantidad, String estado,
    List<Coincidencia> coincidencias) {
  public record Coincidencia(String descripcion, String codigoGP, String codigo3L,
      BigDecimal porcentajeCoincidencia) {}
}
