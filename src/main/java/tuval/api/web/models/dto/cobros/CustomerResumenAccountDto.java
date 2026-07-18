package tuval.api.web.models.dto.cobros;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResumenAccountDto {
     private String identificacion;
     private BigDecimal actual;
     private BigDecimal dias030;
     private BigDecimal dias3160;
     private BigDecimal dias6190;
     private BigDecimal dias91Mas;
     private BigDecimal total;

}
