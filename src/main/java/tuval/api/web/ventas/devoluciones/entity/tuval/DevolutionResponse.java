package tuval.api.web.ventas.devoluciones.entity.tuval;

public class DevolutionResponse {

  private int codRet; // @codret OUT
  private String mensaje; // @msj OUT
  private int idMovimiento; // @idmov IN/OUT
  private String docGp;
  public int getCodRet() {
    return codRet;
  }
  public void setCodRet(int codRet) {
    this.codRet = codRet;
  }
  public String getMensaje() {
    return mensaje;
  }
  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }
  public int getIdMovimiento() {
    return idMovimiento;
  }
  public void setIdMovimiento(int idMovimiento) {
    this.idMovimiento = idMovimiento;
  }
  public String getDocGp() {
    return docGp;
  }
  public void setDocGp(String docGp) {
    this.docGp = docGp;
  }

  public  DevolutionResponse(int codRet, String mensaje, int idMovimiento , String docGp) {
        this.codRet = codRet;
        this.mensaje = mensaje;
        this.idMovimiento = idMovimiento;
        this.docGp = docGp;
    }
}
