package tuval.api.web.models.request.bodegas.articulos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleConfigSegmentRequest {
  private Integer codigoSegmento;
  private String codigoCategoria;
  private String codigoLinea;
}
