package tuval.api.web.models.dto.cobros;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPendingApplicationDocumentDto {
        private String identificacion;
        private String documento;
        private String tipo;
        private String observacion;
        private LocalDate fechaEmision;
        private String condicionPago;
        private BigDecimal montoOriginal;
        private BigDecimal saldoCliente;


}
