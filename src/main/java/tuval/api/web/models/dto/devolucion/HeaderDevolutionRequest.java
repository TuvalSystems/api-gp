package tuval.api.web.models.dto.devolucion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public class HeaderDevolutionRequest {

  @NotBlank private String idCompany;
  @Valid @NotBlank private String documento;
  private String pedidoOrigen;
  @Valid @NotBlank private String bodega;
  private String observacion;
  @Valid @NotBlank private String usuario;
  public String getIdCompany() {
    return idCompany;
  }
  public void setIdCompany(String idCompany) {
    this.idCompany = idCompany;
  }
  public String getDocumento() {
    return documento;
  }
  public void setDocumento(String documento) {
    this.documento = documento;
  }
  public String getPedidoOrigen() {
    return pedidoOrigen;
  }
  public void setPedidoOrigen(String pedidoOrigen) {
    this.pedidoOrigen = pedidoOrigen;
  }
  public String getBodega() {
    return bodega;
  }
  public void setBodega(String bodega) {
    this.bodega = bodega;
  }
  public String getObservacion() {
    return observacion;
  }
  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }
  public String getUsuario() {
    return usuario;
  }
  public void setUsuario(String usuario) {
    this.usuario = usuario;
  }

  
}
