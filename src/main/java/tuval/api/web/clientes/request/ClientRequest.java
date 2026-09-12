package tuval.api.web.clientes.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRequest {

  public String ruc;
  public String razonSocial;
  public String nombreCorto;
  public String clase;
  public Integer prioridad;
  public String contacto;
  public String direccion1;
  public String direccion2;
  public String ciudad;
  public String provincia;
  public String pais;
  public String territorio;
  public String telefono1;
  public String telefono2;
  public String telefono3;
  public String telefono4;
  public String correoSri;
  public String correoNotificacion;
  public String vendedor;
  public String grupo;
  public String subGrupo;
  public BigDecimal descComercial;
  public String condPago;
  public String nivelPrecio;
  public String puntoVenta;
  public String tipoId;
}
