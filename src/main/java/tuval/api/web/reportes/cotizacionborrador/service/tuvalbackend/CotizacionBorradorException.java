package tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend;

/** Contains only a public HTTP status, never an upstream body, token or URL. */
public class CotizacionBorradorException extends RuntimeException {
  private final int status;
  public CotizacionBorradorException(int status) { this.status = status; }
  public int status() { return status; }
}
