package tuval.api.web.reportes.cotizacionborrador.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public record CotizacionBorradorReporteRequest(
    @JsonProperty("request_id") @JsonDeserialize(using = Texto.class)
    @NotBlank @Pattern(regexp = "[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}") String requestId,
    @JsonProperty("cliente_ruc") @JsonDeserialize(using = Texto.class) @Size(max = 30) String clienteRuc,
    @JsonProperty("pv_id") @JsonDeserialize(using = Entero.class) @Min(1) Long pvId,
    @JsonProperty("modo_prueba") @JsonDeserialize(using = Booleano.class) Boolean modoPrueba,
    @JsonProperty("user_id") @JsonDeserialize(using = Entero.class) @Min(1) Long userId,
    @NotNull @Size(min = 1, max = 20) List<@NotNull @Valid Item> items) {

  @AssertTrue
  public boolean isContextoValido() {
    return (clienteRuc == null || !clienteRuc.isBlank())
        && (Boolean.TRUE.equals(modoPrueba) || (userId == null && clienteRuc != null && pvId != null));
  }

  @JsonAnySetter
  public void campoDesconocido(String campo, JsonNode valor) { throw new IllegalArgumentException(); }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Item(
      @JsonDeserialize(using = Texto.class) @NotBlank @Size(max = 30) String codigo,
      @JsonDeserialize(using = Cantidad.class) @DecimalMin(value = "0", inclusive = false)
      @DecimalMax("1000000") @Digits(integer = 7, fraction = 2) BigDecimal cantidad,
      @JsonDeserialize(using = Texto.class) @Size(min = 1, max = 30) String unidad) {
    @JsonAnySetter
    public void campoDesconocido(String campo, JsonNode valor) { throw new IllegalArgumentException(); }
  }

  // Preserve omitted fields (Laravel applies defaults), but never turn explicit null into omission.
  public abstract static class Estricto<T> extends JsonDeserializer<T> {
    @Override public T getAbsentValue(DeserializationContext context) { return null; }
    @Override public T getNullValue(DeserializationContext context) throws MismatchedInputException {
      throw MismatchedInputException.from(context.getParser(), Object.class, "No se admite null");
    }
    protected void exigir(JsonParser parser, boolean valid) throws MismatchedInputException {
      if (!valid) throw MismatchedInputException.from(parser, Object.class, "Tipo o formato invalido");
    }
  }
  public static class Texto extends Estricto<String> {
    @Override public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      exigir(parser, parser.hasToken(JsonToken.VALUE_STRING));
      return parser.getText();
    }
  }
  public static class Entero extends Estricto<Long> {
    @Override public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      exigir(parser, parser.hasToken(JsonToken.VALUE_NUMBER_INT));
      return parser.getLongValue();
    }
  }
  public static class Booleano extends Estricto<Boolean> {
    @Override public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      exigir(parser, parser.hasToken(JsonToken.VALUE_TRUE) || parser.hasToken(JsonToken.VALUE_FALSE));
      return parser.getBooleanValue();
    }
  }
  public static class Cantidad extends Estricto<BigDecimal> {
    @Override public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      exigir(parser, parser.currentToken().isNumeric() || parser.hasToken(JsonToken.VALUE_STRING));
      String value = parser.getText();
      exigir(parser, value.length() <= 32 && value.matches("[0-9]+(\\.[0-9]{1,2})?"));
      return new BigDecimal(value);
    }
  }
}
