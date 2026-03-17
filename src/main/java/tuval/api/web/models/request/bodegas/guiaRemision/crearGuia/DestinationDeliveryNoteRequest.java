package tuval.api.web.models.request.bodegas.guiaremision.crearGuia;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DestinationDeliveryNoteRequest {
  private String guiaRemision;
  private Integer secuencialDestinatario;
  private String identificacionDestinatario;
  private String razonSocialDestinatario;
  private String direccionDestinatario;
  private String motivoTraslado;
  private String docAduanero;
  private String codEstablecimientoDestino;
  private String descEstablecimientoDestino;
  private String ruta;
  private String docSustento;
  private String numDocSustento;
  private String numAutoDocSustento;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm[:ss]", timezone = "America/Guayaquil")
  private LocalDateTime  fechaEmisionDocSustento;
  private String ordenCompra;

  
}
