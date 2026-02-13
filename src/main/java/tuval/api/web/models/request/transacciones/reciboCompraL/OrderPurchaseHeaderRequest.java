package tuval.api.web.models.request.transacciones.reciboCompraL;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPurchaseHeaderRequest {
    private String docuOrigen;
    private String linea;
    private LocalDateTime fechaDocumento;
    private LocalDateTime fechaEntrega;
    private String idCliente;
    private String idProveedor;
    private String idDireccion;
    private String notaDocu;
    private String bodega;
    private String articulo;
    private String descripcionArticulo;
    private String umedida;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal total;
    private BigDecimal impuesto;
    private String idUsuario;
}
