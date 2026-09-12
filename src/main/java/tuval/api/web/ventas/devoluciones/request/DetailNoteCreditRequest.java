package tuval.api.web.ventas.devoluciones.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DetailNoteCreditRequest {
    private Integer linea;
    private String docuOrigen;
    private String idTipo;
    private String articulo;
    private String descripcion;
    private String umedia;
    private String bodega;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal precioUnitario;
    private BigDecimal descuento;
    private BigDecimal iva;
    private BigDecimal total;



}
