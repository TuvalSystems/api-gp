package tuval.api.web.models.dto.bodegas.guiaRemision.crearGuia;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DestinationDeliveryNoteRequest {
  private String guiaRemision;
  private Integer secuencialDestinatario;
  private String identificacionDestinatario;
  private String razonSocialDestinatario;
  private String direccionDestinatario;
  private String motivoTraslado;
  private String docAduanero;
  private String codEstablecimientoDestino;
  private String descEstablecimientoDestino;
  private String ruta;
  private String docSustento;
  private String numDocSustento;
  private String numAutoDocSustento;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm[:ss]", timezone = "America/Guayaquil")
  private LocalDateTime  fechaEmisionDocSustento;
  public String getGuiaRemision() {
    return guiaRemision;
  }
  public void setGuiaRemision(String guiaRemision) {
    this.guiaRemision = guiaRemision;
  }
  public Integer getSecuencialDestinatario() {
    return secuencialDestinatario;
  }
  public void setSecuencialDestinatario(Integer secuencialDestinatario) {
    this.secuencialDestinatario = secuencialDestinatario;
  }
  public String getIdentificacionDestinatario() {
    return identificacionDestinatario;
  }
  public void setIdentificacionDestinatario(String identificacionDestinatario) {
    this.identificacionDestinatario = identificacionDestinatario;
  }
  public String getRazonSocialDestinatario() {
    return razonSocialDestinatario;
  }
  public void setRazonSocialDestinatario(String razonSocialDestinatario) {
    this.razonSocialDestinatario = razonSocialDestinatario;
  }
  public String getDireccionDestinatario() {
    return direccionDestinatario;
  }
  public void setDireccionDestinatario(String direccionDestinatario) {
    this.direccionDestinatario = direccionDestinatario;
  }
  public String getMotivoTraslado() {
    return motivoTraslado;
  }
  public void setMotivoTraslado(String motivoTraslado) {
    this.motivoTraslado = motivoTraslado;
  }
  public String getDocAduanero() {
    return docAduanero;
  }
  public void setDocAduanero(String docAduanero) {
    this.docAduanero = docAduanero;
  }
  public String getCodEstablecimientoDestino() {
    return codEstablecimientoDestino;
  }
  public void setCodEstablecimientoDestino(String codEstablecimientoDestino) {
    this.codEstablecimientoDestino = codEstablecimientoDestino;
  }
  public String getDescEstablecimientoDestino() {
    return descEstablecimientoDestino;
  }
  public void setDescEstablecimientoDestino(String descEstablecimientoDestino) {
    this.descEstablecimientoDestino = descEstablecimientoDestino;
  }
  public String getRuta() {
    return ruta;
  }
  public void setRuta(String ruta) {
    this.ruta = ruta;
  }
  public String getDocSustento() {
    return docSustento;
  }
  public void setDocSustento(String docSustento) {
    this.docSustento = docSustento;
  }
  public String getNumDocSustento() {
    return numDocSustento;
  }
  public void setNumDocSustento(String numDocSustento) {
    this.numDocSustento = numDocSustento;
  }
  public String getNumAutoDocSustento() {
    return numAutoDocSustento;
  }
  public void setNumAutoDocSustento(String numAutoDocSustento) {
    this.numAutoDocSustento = numAutoDocSustento;
  }
  public LocalDateTime getFechaEmisionDocSustento() {
    return fechaEmisionDocSustento;
  }
  public void setFechaEmisionDocSustento(LocalDateTime fechaEmisionDocSustento) {
    this.fechaEmisionDocSustento = fechaEmisionDocSustento;
  }

  
}
