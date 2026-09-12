package tuval.api.web.cobranzas.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ClientOverdueDocumentDto {
    private String ruc ;
    private int diasVencidos;
    private LocalDate  fEmision;
    private LocalDate  fVencimiento;
    private String factura;
    private BigDecimal pendienteGpMc;
    public String getRuc() {
        return ruc;
    }
    public void setRuc(String ruc) {
        this.ruc = ruc;
    }
    public int getDiasVencidos() {
        return diasVencidos;
    }
    public void setDiasVencidos(int diasVencidos) {
        this.diasVencidos = diasVencidos;
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
    public BigDecimal getPendienteGpMc() {
        return pendienteGpMc;
    }
    public void setPendienteGpMc(BigDecimal pendienteGpMc) {
        this.pendienteGpMc = pendienteGpMc;
    }
}
