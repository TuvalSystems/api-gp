package tuval.api.web.bodega.despachos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeaderGuideDispatchShipmentRequest {

  @NotBlank private String idCompany;
  @NotBlank private String pedido;
  private String pedidoNuevo;
  @NotBlank private String bodegaOrigen;
  private String bodegaDestino;
  private String observacion;
  @NotBlank private String usuario;
  private Integer idGuiaRemision;
  private String guiaRemision;
  private String hashInterno;
}
