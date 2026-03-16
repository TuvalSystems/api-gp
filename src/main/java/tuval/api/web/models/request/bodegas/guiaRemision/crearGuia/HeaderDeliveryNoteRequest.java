package tuval.api.web.models.request.bodegas.guiaremision.crearGuia;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
  private String ordenCompra;

}
