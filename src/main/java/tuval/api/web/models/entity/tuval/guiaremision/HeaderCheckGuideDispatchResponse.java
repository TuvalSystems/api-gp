package tuval.api.web.models.entity.tuval.guiaremision;

import java.sql.Date;

public class HeaderCheckGuideDispatchResponse {

  private Long idMovimiento;
  private String numDoc;
  private String numDocOri;
  private String origen;
  private String destino;

  private String userIngreso; // OK, no es palabra reservada en Java
  private Date fechaGp; // o Timestamp/LocalDate según tu config
  private String docGp;
  private Integer idGuiaRemision;
  private String guiaRemision;

  
  public Long getIdMovimiento() {
    return idMovimiento;
  }

  public void setIdMovimiento(Long idMovimiento) {
    this.idMovimiento = idMovimiento;
  }

  public String getNumDoc() {
    return numDoc;
  }

  public void setNumDoc(String numDoc) {
    this.numDoc = numDoc;
  }

  public String getOrigen() {
    return origen;
  }

  public void setOrigen(String origen) {
    this.origen = origen;
  }

  public String getUserIngreso() {
    return userIngreso;
  }

  public void setUserIngreso(String userIngreso) {
    this.userIngreso = userIngreso;
  }

  public Date getFechaGp() {
    return fechaGp;
  }

  public void setFechaGp(Date fechaGp) {
    this.fechaGp = fechaGp;
  }

  public String getDocGp() {
    return docGp;
  }

  public void setDocGp(String docGp) {
    this.docGp = docGp;
  }

  public String getNumDocOri() {
    return numDocOri;
  }

  public void setNumDocOri(String numDocOri) {
    this.numDocOri = numDocOri;
  }

  public String getDestino() {
    return destino;
  }

  public void setDestino(String destino) {
    this.destino = destino;
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
}
