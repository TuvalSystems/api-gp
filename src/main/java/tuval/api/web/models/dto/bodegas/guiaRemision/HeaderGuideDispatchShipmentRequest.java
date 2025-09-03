package tuval.api.web.models.dto.bodegas.guiaRemision;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public class HeaderGuideDispatchShipmentRequest {

  @NotBlank private String idCompany;
  @Valid @NotBlank private String pedido;
  private String pedidoNuevo;
  @Valid @NotBlank private String bodegaOrigen;
  private String observacion;
  @Valid @NotBlank private String usuario;

  public String getIdCompany() {
    return idCompany;
  }

  public void setIdCompany(String idCompany) {
    this.idCompany = idCompany;
  }

  public String getPedido() {
    return pedido;
  }

  public void setPedido(String pedido) {
    this.pedido = pedido;
  }

  public String getPedidoNuevo() {
    return pedidoNuevo;
  }

  public void setPedidoNuevo(String pedidoNuevo) {
    this.pedidoNuevo = pedidoNuevo;
  }

  public String getBodegaOrigen() {
    return bodegaOrigen;
  }

  public void setBodegaOrigen(String bodegaOrigen) {
    this.bodegaOrigen = bodegaOrigen;
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
