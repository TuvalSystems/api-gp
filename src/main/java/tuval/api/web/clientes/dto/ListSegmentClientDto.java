package tuval.api.web.clientes.dto;

import lombok.Getter;
import lombok.Setter;

// import com.fasterxml.jackson.annotation.JsonProperty;
@Getter
@Setter
public class ListSegmentClientDto {

  private Integer coAtrPadre;
  private String segmento;
  private String observacionPadre;
  private Integer coAtrHijo;
  private String subsegmento;
  private String observacionHijo;
  private String txCli;

  
}
