package tuval.api.web.reportes.cotizacionborrador.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/** Deliberate response allowlist: never expose upstream debug fields, models or prices. */
public record CotizacionBorradorReporteDto(
    Long id, String numero, String estado, @JsonProperty("request_id") String requestId,
    Boolean reutilizado, Usuario usuario, Usuario actor,
    @JsonProperty("modo_prueba") Boolean modoPrueba,
    @JsonProperty("punto_venta") PuntoVenta puntoVenta, Cliente cliente,
    List<Incluido> incluidos, List<Advertencia> advertencias) {
  public record Usuario(Long id, String codigo) {}
  public record PuntoVenta(Long id, String codigo, String descripcion) {}
  public record Cliente(Long id, String ruc) {}
  public record Incluido(Integer indice, String codigo, @JsonProperty("art_id") Long artId,
      BigDecimal cantidad, @JsonProperty("cantidad_asumida") Boolean cantidadAsumida, String unidad) {}
  public record Advertencia(Integer indice, String codigo, String mensaje) {}
}
