package tuval.api.web.models.request.cliente;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewAddressRequest {

  public String ruc;
  public String contacto;
  public String direccionId;
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
  public String puntoVenta;
}
