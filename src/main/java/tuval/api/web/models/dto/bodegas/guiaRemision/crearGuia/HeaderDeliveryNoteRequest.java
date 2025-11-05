package tuval.api.web.models.dto.bodegas.guiaRemision.crearGuia;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class HeaderDeliveryNoteRequest {
  private String guiaRemision;
  private String establecimiento;
  private String ptoEmision;
  private String secuencial;
  private String direccionPartida;
  private String razonSocialTransportista;
  private String identificacionTransportista;
  private String tipoIdentificacionTransportista;
  private String rise;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm[:ss]", timezone = "America/Guayaquil")
  private LocalDateTime fechaInicio;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm[:ss]", timezone = "America/Guayaquil")
  private LocalDateTime fechaFin;
  private String placa;
  private String correoElectronico;
  private String observacion;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm[:ss]", timezone = "America/Guayaquil")
  private LocalDateTime fechaEmisionSustento;

  public String getGuiaRemision() {
    return guiaRemision;
  }

  public void setGuiaRemision(String guiaRemision) {
    this.guiaRemision = guiaRemision;
  }

  public String getEstablecimiento() {
    return establecimiento;
  }

  public void setEstablecimiento(String establecimiento) {
    this.establecimiento = establecimiento;
  }

  public String getPtoEmision() {
    return ptoEmision;
  }

  public void setPtoEmision(String ptoEmision) {
    this.ptoEmision = ptoEmision;
  }

  public String getSecuencial() {
    return secuencial;
  }

  public void setSecuencial(String secuencial) {
    this.secuencial = secuencial;
  }

  public String getDireccionPartida() {
    return direccionPartida;
  }

  public void setDireccionPartida(String direccionPartida) {
    this.direccionPartida = direccionPartida;
  }

  public String getRazonSocialTransportista() {
    return razonSocialTransportista;
  }

  public void setRazonSocialTransportista(String razonSocialTransportista) {
    this.razonSocialTransportista = razonSocialTransportista;
  }

  public String getIdentificacionTransportista() {
    return identificacionTransportista;
  }

  public void setIdentificacionTransportista(String identificacionTransportista) {
    this.identificacionTransportista = identificacionTransportista;
  }

  public String getTipoIdentificacionTransportista() {
    return tipoIdentificacionTransportista;
  }

  public void setTipoIdentificacionTransportista(String tipoIdentificacionTransportista) {
    this.tipoIdentificacionTransportista = tipoIdentificacionTransportista;
  }

  public String getRise() {
    return rise;
  }

  public void setRise(String rise) {
    this.rise = rise;
  }

  public LocalDateTime getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDateTime fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDateTime getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDateTime fechaFin) {
    this.fechaFin = fechaFin;
  }

  public String getPlaca() {
    return placa;
  }

  public void setPlaca(String placa) {
    this.placa = placa;
  }

  public String getCorreoElectronico() {
    return correoElectronico;
  }

  public void setCorreoElectronico(String correoElectronico) {
    this.correoElectronico = correoElectronico;
  }

  public String getObservacion() {
    return observacion;
  }

  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }

  public LocalDateTime getFechaEmisionSustento() {
    return fechaEmisionSustento;
  }

  public void setFechaEmisionSustento(LocalDateTime fechaEmisionSustento) {
    this.fechaEmisionSustento = fechaEmisionSustento;
  }
}
