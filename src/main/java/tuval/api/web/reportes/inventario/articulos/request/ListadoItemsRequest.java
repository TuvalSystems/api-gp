package tuval.api.web.reportes.inventario.articulos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ListadoItemsRequest {
  @Size(max = 31) @Pattern(regexp = "(?s).*\\S.*")
  private String codigo;
  @Size(max = 101) @Pattern(regexp = "(?s).*\\S.*")
  private String descripcion;
  @Size(max = 100) @Pattern(regexp = "(?s).*\\S.*")
  private String marca;
  @Size(max = 100) @Pattern(regexp = "(?s).*\\S.*")
  private String material;
  @NotNull @Min(0) @Max(10000)
  private Integer page = 0;
  @NotNull @Min(1) @Max(500)
  private Integer size = 100;
}
