package tuval.api.web.bodega.despachos.entity.tuval;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

 @Data
 @NoArgsConstructor
 @AllArgsConstructor
public class HeaderCheckGuideDispatchResponse {

  private Long idMovimiento;
  private String numDoc;
  private String numDocOri;
  private String origen;
  private String destino;

  private String userIngreso; // OK, no es palabra reservada en Java
  private Date fechaGp; // o Timestamp/LocalDate según tu config
  private String docGp;
  private Integer idGuiaRemision;
  private String guiaRemision;

  

}
