package tuval.api.web.reportes.resolucionarticulos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/** Allowlist for the current unauthenticated test endpoint, not the older UUID/Bearer API. */
public record ResolucionBorradorDto(Long id, String numero, String estado, Usuario usuario,
    @JsonProperty("punto_venta") PuntoVenta puntoVenta, Cliente cliente, List<Incluido> incluidos) {
  public record Usuario(Long id, String codigo) {}
  public record PuntoVenta(Long id, String codigo, String descripcion) {}
  public record Cliente(Long id, String ruc) {}
  public record Incluido(Integer indice, String codigo, @JsonProperty("art_id") Long artId,
      BigDecimal cantidad, String unidad) {}
}