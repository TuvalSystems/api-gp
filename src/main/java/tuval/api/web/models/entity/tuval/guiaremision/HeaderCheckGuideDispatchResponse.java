package tuval.api.web.models.entity.tuval.guiaremision;



import java.sql.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class HeaderCheckGuideDispatchResponse {

  private Long idMovimiento;
  private String numDoc;
  private String origen;
  private String userIngreso; // OK, no es palabra reservada en Java
  private Date fechaGp; // o Timestamp/LocalDate según tu config
  private String docGp;
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
}
