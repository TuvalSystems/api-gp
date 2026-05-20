package tuval.api.web.models.dto.articulos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravapItemOriginDto {

  private String txItemOri;
  private String txItemDes;
  private Integer neCan;
  private Integer neOrd;
}
