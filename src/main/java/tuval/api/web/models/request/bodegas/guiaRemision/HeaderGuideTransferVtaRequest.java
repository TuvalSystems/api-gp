package tuval.api.web.models.request.bodegas.guiaRemision;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeaderGuideTransferVtaRequest {
  @NotBlank private String idCompany;
  @Valid @NotBlank private String pedido;
  @Valid @NotBlank private String bodegaOrigen;
  @Valid @NotBlank private String bodegaDestino;
  @Valid @NotBlank private String usuario;
  private String observacion;
  private Integer idGuiaRemision;
  private String guiaRemision;
  private String numTranferencia;
}
