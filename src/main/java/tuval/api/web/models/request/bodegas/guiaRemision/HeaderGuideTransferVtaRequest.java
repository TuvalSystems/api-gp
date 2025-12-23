package tuval.api.web.models.request.bodegas.guiaRemision;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

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

  public String getBodegaOrigen() {
    return bodegaOrigen;
  }

  public void setBodegaOrigen(String bodegaOrigen) {
    this.bodegaOrigen = bodegaOrigen;
  }

  public String getBodegaDestino() {
    return bodegaDestino;
  }

  public void setBodegaDestino(String bodegaDestino) {
    this.bodegaDestino = bodegaDestino;
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

  public Integer getIdGuiaRemision() {
    return idGuiaRemision;
  }

  public void setIdGuiaRemision(Integer idGuiaRemision) {
    this.idGuiaRemision = idGuiaRemision;
  }

  public String getGuiaRemision() {
    return guiaRemision;
  }

  public void setGuiaRemision(String guiaRemision) {
    this.guiaRemision = guiaRemision;
  }

    public String getNumTranferencia() {
    return numTranferencia;
  }

  public void setNumTranferencia(String numTranferencia) {
    this.numTranferencia = numTranferencia;
  }
}
