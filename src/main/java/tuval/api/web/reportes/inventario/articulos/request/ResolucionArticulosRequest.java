package tuval.api.web.reportes.inventario.articulos.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public record ResolucionArticulosRequest(
    @NotNull @Size(min = 1, max = 20) List<@NotNull @Valid Renglon> renglones,
    @JsonDeserialize(using = EnteroEstricto.class) @Min(1) @Max(5) Integer maxCandidatos) {
  public record Renglon(
      @JsonDeserialize(using = TextoEstricto.class) @NotBlank @Size(max = 200) String texto,
      @DecimalMin(value = "0", inclusive = false) @DecimalMax("1000000")
      @Digits(integer = 7, fraction = 3) BigDecimal cantidad) {}

  // Local to this contract: do not change Jackson coercion for existing endpoints.
  public static class EnteroEstricto extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
        throw MismatchedInputException.from(parser, Integer.class, "Se requiere un entero JSON");
      }
      return parser.getIntValue();
    }
  }

  public static class TextoEstricto extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      if (!parser.hasToken(JsonToken.VALUE_STRING)) {
        throw MismatchedInputException.from(parser, String.class, "Se requiere texto JSON");
      }
      return parser.getText();
    }
  }
}
