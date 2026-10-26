package tuval.api.web.bodega.despachos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuidePurchaseOrderRequest {
  @NotNull(message = "El id es requerido porque la tabla no es IDENTITY")
  private Integer id;

  @JsonProperty("id_guia_remision")
  private Integer idGuiaRemision;

  @JsonProperty("id_guia_reserva")
  private Integer idGuiaReserva;

  @JsonProperty("id_pedido_id")
  private Integer idPedidoId;

  @JsonProperty("tipo_guia")
  @Size(max = 255)
  private String tipoGuia;

  @JsonProperty("id_oc")
  @Size(max = 17)
  private String idOc;

  @JsonProperty("fecha_ingreso")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime fechaIngreso;
}
