package tuval.api.web.reportes.resolucionarticulos.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import tuval.api.web.reportes.cotizacionborrador.request.CotizacionBorradorReporteRequest.*;

/** Separate write contract: consultar keeps its original read-only semantics. */
public record ResolucionBorradorRequest(
    @JsonProperty("request_id") @JsonDeserialize(using = Texto.class)
    @NotBlank @Pattern(regexp = "[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}") String requestId,
    @JsonProperty("cliente_ruc") @JsonDeserialize(using = Texto.class)
    @Size(min = 1, max = 30) @Pattern(regexp = ".*\\S.*") String clienteRuc,
    @JsonProperty("pv_id") @JsonDeserialize(using = Entero.class) @Min(1) Long pvId,
    @NotNull @Size(min = 1, max = 20) List<@NotNull @Valid Renglon> renglones) {
  @JsonAnySetter public void desconocido(String name, JsonNode value) { throw new IllegalArgumentException(); }

  public record Renglon(
      @JsonDeserialize(using = Texto.class) @NotBlank @Size(max = 200) String texto,
      @JsonDeserialize(using = Cantidad.class) @DecimalMin(value = "0", inclusive = false)
      @DecimalMax("1000000") @Digits(integer = 7, fraction = 2) BigDecimal cantidad,
      @JsonDeserialize(using = Texto.class) @Size(min = 1, max = 30)
      @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._/-]{0,29}") String codigoSeleccionado) {
    @JsonAnySetter public void desconocido(String name, JsonNode value) { throw new IllegalArgumentException(); }
  }
}