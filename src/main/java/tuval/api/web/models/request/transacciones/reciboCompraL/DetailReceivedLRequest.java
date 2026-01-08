package tuval.api.web.models.request.transacciones.reciboCompraL;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailReceivedLRequest {

  @JsonProperty("REC_COM_DOCU_ORIGEN")
  private String recComDocuOrigen;

  @JsonProperty("REC_COM_ID_TIPO")
  private Integer recComIdTipo;

  @JsonProperty("REC_COM_ID_ARTICULO")
  private String recCOmIdArticulo;

  @JsonProperty("REC_COM_DESCRIPCION_ARTICULO")
  private String recComDescripcionArticulo;

  @JsonProperty("REC_COM_UNIDAD_MEDIDA")
  private String recComUnidadMedida;

  @JsonProperty("REC_COM_BODEGA")
  private String recComBodega;

  @JsonProperty("REC_COM_CANTIDAD")
  private BigDecimal recComCantidad;
}
