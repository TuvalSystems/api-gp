package tuval.api.web.models.entity.tuval.guiaremision;

import java.math.BigDecimal;
import java.sql.Date;

public class DetailCheckGuideAnyResponse {

  private Long idMovimiento;
  private String tipoTrx;
  private String numDoc;
  private String numDocOri;
  private String item;
  private BigDecimal cantidad;
  private String origen;
  private Date fechaGp; // o Timestamp/LocalDate según tu config
  private String docGp;
  public Long getIdMovimiento() {
    return idMovimiento;
  }
  public void setIdMovimiento(Long idMovimiento) {
    this.idMovimiento = idMovimiento;
  }
  public String getTipoTrx() {
    return tipoTrx;
  }
  public void setTipoTrx(String tipoTrx) {
    this.tipoTrx = tipoTrx;
  }
  public String getNumDoc() {
    return numDoc;
  }
  public void setNumDoc(String numDoc) {
    this.numDoc = numDoc;
  }
  public String getNumDocOri() {
    return numDocOri;
  }
  public void setNumDocOri(String numDocOri) {
    this.numDocOri = numDocOri;
  }
  public String getItem() {
    return item;
  }
  public void setItem(String item) {
    this.item = item;
  }
  public BigDecimal getCantidad() {
    return cantidad;
  }
  public void setCantidad(BigDecimal cantidad) {
    this.cantidad = cantidad;
  }
  public String getOrigen() {
    return origen;
  }
  public void setOrigen(String origen) {
    this.origen = origen;
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
