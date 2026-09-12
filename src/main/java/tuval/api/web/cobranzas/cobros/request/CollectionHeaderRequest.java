package tuval.api.web.cobranzas.cobros.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionHeaderRequest {
    
    public String numeroRecibo;
    public String ruc;
    public String fechaCobro;
    public String tipoCobro;
    public String observacion;
    public String solicitudDescuento;
    public String idVendedor;
    public BigDecimal montoCobro;
    public BigDecimal saldoPendiente;
    public String bancoTuval;
    public String fechaDeposito;
    public String numeroPapeleta;

    //para retenciones 
    public String fechaCaducidad;
    public String numeroRetencion;
    public String numeroAutorizacion;
    public BigDecimal porcentajeRetencion;



}
