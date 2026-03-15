package tuval.api.web.models.request.bodegas.guiaremision;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeaderGuideDispatchShipmentRequest {

  @NotBlank private String idCompany;
  @Valid @NotBlank private String pedido;
  private String pedidoNuevo;
  @Valid @NotBlank private String bodegaOrigen;
  private String observacion;
  @Valid @NotBlank private String usuario;
  private Integer idGuiaRemision;
  private String guiaRemision;
  private String hashInterno;

 
}
