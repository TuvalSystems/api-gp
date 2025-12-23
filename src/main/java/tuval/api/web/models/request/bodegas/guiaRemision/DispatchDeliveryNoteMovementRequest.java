package tuval.api.web.models.request.bodegas.guiaRemision;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DispatchDeliveryNoteMovementRequest {

  @NotBlank private String idCompany;
  @Valid @NotBlank private String pedido;
  private String pedidoNuevo;
  @Valid @NotBlank private String bodegaOrigen;
  private String observacion;
  @Valid @NotBlank private String usuario;
  private Integer idGuiaRemision;
  private String guiaRemision;
  private int idMovimiento;
}
