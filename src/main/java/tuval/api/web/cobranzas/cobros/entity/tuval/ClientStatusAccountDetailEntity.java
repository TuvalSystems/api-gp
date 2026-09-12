package tuval.api.web.cobranzas.cobros.entity.tuval;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ClientStatusAccountDetailEntity {
    
    public String identificacion;
    public String documento;
    public String tipo;
    public String vendedor;
    public LocalDate fechaEmision;
    public LocalDate fechaVencimiento;
    public String condicionPago;
    public BigDecimal montoOriginal;
    public BigDecimal saldoBaseCliente;
    public BigDecimal saldoCliente;
    public String getIdentificacion() {
        return identificacion;
    }
    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }
    public String getDocumento() {
        return documento;
    }
    public void setDocumento(String documento) {
        this.documento = documento;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public String getVendedor() {
        return vendedor;
    }
    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }
    public LocalDate getFechaEmision() {
        return fechaEmision;
    }
    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }
    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
    public String getCondicionPago() {
        return condicionPago;
    }
    public void setCondicionPago(String condicionPago) {
        this.condicionPago = condicionPago;
    }
    public BigDecimal getMontoOriginal() {
        return montoOriginal;
    }
    public void setMontoOriginal(BigDecimal montoOriginal) {
        this.montoOriginal = montoOriginal;
    }
    public BigDecimal getSaldoBaseCliente() {
        return saldoBaseCliente;
    }
    public void setSaldoBaseCliente(BigDecimal saldoBaseCliente) {
        this.saldoBaseCliente = saldoBaseCliente;
    }
    public BigDecimal getSaldoCliente() {
        return saldoCliente;
    }
    public void setSaldoCliente(BigDecimal saldoCliente) {
        this.saldoCliente = saldoCliente;
    }


}
