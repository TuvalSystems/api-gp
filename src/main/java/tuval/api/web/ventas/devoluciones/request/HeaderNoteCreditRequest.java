package tuval.api.web.ventas.devoluciones.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HeaderNoteCreditRequest {
    private String docOrigen;
    private String idTipo;
    private String ptoVta;
    private String docId;
    private String idCliente;
    private String idVendedor;
    private String idDireccion;
    private String condPago;
    private String notaDocu;
    private LocalDate fecha;
    private String bodega;
    private BigDecimal subtotal;
    private BigDecimal descComercial;
    private BigDecimal impuesto;
    private BigDecimal total;

}
