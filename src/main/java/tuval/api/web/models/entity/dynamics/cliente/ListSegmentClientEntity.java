package tuval.api.web.models.entity.dynamics.cliente;

// import com.fasterxml.jackson.annotation.JsonProperty;

public class ListSegmentClientEntity {

  private Integer coAtrPadre;
  private String segmento;
  private String observacionPadre;
  private Integer coAtrHijo;
  private String subsegmento;
  private String observacionHijo;
  private String txCli;

  public Integer getCoAtrPadre() {
    return coAtrPadre;
  }

  public void setCoAtrPadre(Integer coAtrPadre) {
    this.coAtrPadre = coAtrPadre;
  }

  public String getSegmento() {
    return segmento;
  }

  public void setSegmento(String segmento) {
    this.segmento = segmento;
  }

  public String getObservacionPadre() {
    return observacionPadre;
  }

  public void setObservacionPadre(String observacionPadre) {
    this.observacionPadre = observacionPadre;
  }

  public Integer getCoAtrHijo() {
    return coAtrHijo;
  }

  public void setCoAtrHijo(Integer coAtrHijo) {
    this.coAtrHijo = coAtrHijo;
  }

  public String getSubsegmento() {
    return subsegmento;
  }

  public void setSubsegmento(String subsegmento) {
    this.subsegmento = subsegmento;
  }

  public String getObservacionHijo() {
    return observacionHijo;
  }

  public void setObservacionHijo(String observacionHijo) {
    this.observacionHijo = observacionHijo;
  }

  public String getTxCli() {
    return txCli;
  }

  public void setTxCli(String txCli) {
    this.txCli = txCli;
  }
}
