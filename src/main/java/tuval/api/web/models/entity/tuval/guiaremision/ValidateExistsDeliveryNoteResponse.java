package tuval.api.web.models.entity.tuval.guiaremision;

public class ValidateExistsDeliveryNoteResponse {

  private int codRet; // @codret OUT
  private String mensaje; // @msj OUT

  
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

  public ValidateExistsDeliveryNoteResponse(int codRet, String mensaje) {
    this.codRet = codRet;
    this.mensaje = mensaje;
  }
}
