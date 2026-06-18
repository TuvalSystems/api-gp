package tuval.api.web.models.dto.cobros;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCollectionDto {
    private String documentoId;
    private LocalDate documentoFecha;
    private LocalDate fechaAplicacion;
    private String mesCobro;
    private String ruc;
    private String cliente;
    private String establecimiento;
    private String factura;
    private BigDecimal valorFactura;
    private Integer tipoPago;
    private String documentoCobro; // cobro
    private BigDecimal valorCobro;
    private LocalDate fechaCobro;
    private String numeroPoCliente;
    private String numeroRecibo;
}
