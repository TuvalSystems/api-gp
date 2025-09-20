package tuval.api.web.models.entity.tuval.cobros;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ClientSinSoporteEntity {
    private String ruc ;
    private LocalDate  fEmision;
    private LocalDate  fVencimiento;
    private String factura;
    private BigDecimal saldoPendiente;
    public String getRuc() {
        return ruc;
    }
    public void setRuc(String ruc) {
        this.ruc = ruc;
    }
    public LocalDate getfEmision() {
        return fEmision;
    }
    public void setfEmision(LocalDate fEmision) {
        this.fEmision = fEmision;
    }
    public LocalDate getfVencimiento() {
        return fVencimiento;
    }
    public void setfVencimiento(LocalDate fVencimiento) {
        this.fVencimiento = fVencimiento;
    }
    public String getFactura() {
        return factura;
    }
    public void setFactura(String factura) {
        this.factura = factura;
    }
    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }
    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    
}
