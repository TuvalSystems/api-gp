package tuval.api.web.models.dto.transacciones.facturacionVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HeaderInvoiceRequest {

    @JsonProperty("FAC_VTA_DOCU_ORIGEN")
    private String factVtaDocuOrigen;

    @JsonProperty("FAC_VTA_DOC_COMPRA")
    private String factVtaDocuCompra;

    @JsonProperty("FAC_VTA_ID_TIPO")
    private String factVtaIdTipo; 

    @JsonProperty("FAC_VTA_PUNTO_VENTA")
    private String facVtaPuntoVenta; 

    @JsonProperty("FAC_VTA_DOC_ID")
    private String facVtaDocId; 

    @JsonProperty("FAC_VTA_ID_CLIENTE")
    private String factVtaIdCliente; 

    @JsonProperty("FAC_VTA_ID_VENDEDOR")
    private String factVtaIdVendedor; 

    @JsonProperty("FAC_VTA_ID_DIRECCION")
    private String facVtaIdDireccion; 

    @JsonProperty("FAC_VTA_ID_DIRECCION_ENVIO")
    private String facVtaIdDireccionEnvio;

    @JsonProperty("FAC_VTA_COND_PAGO")
    private String facVtaCondPago;

    @JsonProperty("FAC_VTA_NOTA_DOCU")
    private String facVtaNotaDocu;

    @JsonProperty("FAC_VTA_DATE_DOCU")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime facVtaDateDocu; 

    @JsonProperty("FAC_VTA_FORMA_PAGO")
    private String facVtaFormaPago;

    @JsonProperty("FAC_VTA_BODEGA")
    private String facVtaBodega;

    @JsonProperty("FAC_VTA_SUBTOTAL")
    private BigDecimal facVtaSubtotal; 

    @JsonProperty("FAC_VTA_DESCUENTO_COMERCIAL")
    private BigDecimal facVtaDescuentoComercial; 

    @JsonProperty("FAC_VTA_IMPUESTO")
    private BigDecimal facVtaImpuesto; 

    @JsonProperty("FAC_VTA_TOTAL")
    private BigDecimal facVtaTotal;

    @JsonProperty("FAC_VTA_TIPO_DESPACHO")
    private String facVtaTipoDespacho;

    @JsonProperty("FAC_VTA_NOTA_CLIENTE")
    private String facVtaNotaCliente;

    public String getFactVtaDocuOrigen() {
        return factVtaDocuOrigen;
    }

    public void setFactVtaDocuOrigen(String factVtaDocuOrigen) {
        this.factVtaDocuOrigen = factVtaDocuOrigen;
    }

    public String getFactVtaDocuCompra() {
        return factVtaDocuCompra;
    }

    public void setFactVtaDocuCompra(String factVtaDocuCompra) {
        this.factVtaDocuCompra = factVtaDocuCompra;
    }

    public String getFactVtaIdTipo() {
        return factVtaIdTipo;
    }

    public void setFactVtaIdTipo(String factVtaIdTipo) {
        this.factVtaIdTipo = factVtaIdTipo;
    }

    public String getFacVtaPuntoVenta() {
        return facVtaPuntoVenta;
    }

    public void setFacVtaPuntoVenta(String facVtaPuntoVenta) {
        this.facVtaPuntoVenta = facVtaPuntoVenta;
    }

    public String getFacVtaDocId() {
        return facVtaDocId;
    }

    public void setFacVtaDocId(String facVtaDocId) {
        this.facVtaDocId = facVtaDocId;
    }

    public String getFactVtaIdCliente() {
        return factVtaIdCliente;
    }

    public void setFactVtaIdCliente(String factVtaIdCliente) {
        this.factVtaIdCliente = factVtaIdCliente;
    }

    public String getFactVtaIdVendedor() {
        return factVtaIdVendedor;
    }

    public void setFactVtaIdVendedor(String factVtaIdVendedor) {
        this.factVtaIdVendedor = factVtaIdVendedor;
    }

    public String getFacVtaIdDireccion() {
        return facVtaIdDireccion;
    }

    public void setFacVtaIdDireccion(String facVtaIdDireccion) {
        this.facVtaIdDireccion = facVtaIdDireccion;
    }

    public String getFacVtaIdDireccionEnvio() {
        return facVtaIdDireccionEnvio;
    }

    public void setFacVtaIdDireccionEnvio(String facVtaIdDireccionEnvio) {
        this.facVtaIdDireccionEnvio = facVtaIdDireccionEnvio;
    }

    public String getFacVtaCondPago() {
        return facVtaCondPago;
    }

    public void setFacVtaCondPago(String facVtaCondPago) {
        this.facVtaCondPago = facVtaCondPago;
    }

    public String getFacVtaNotaDocu() {
        return facVtaNotaDocu;
    }

    public void setFacVtaNotaDocu(String facVtaNotaDocu) {
        this.facVtaNotaDocu = facVtaNotaDocu;
    }

    public LocalDateTime getFacVtaDateDocu() {
        return facVtaDateDocu;
    }

    public void setFacVtaDateDocu(LocalDateTime facVtaDateDocu) {
        this.facVtaDateDocu = facVtaDateDocu;
    }

    public String getFacVtaFormaPago() {
        return facVtaFormaPago;
    }

    public void setFacVtaFormaPago(String facVtaFormaPago) {
        this.facVtaFormaPago = facVtaFormaPago;
    }

    public String getFacVtaBodega() {
        return facVtaBodega;
    }

    public void setFacVtaBodega(String facVtaBodega) {
        this.facVtaBodega = facVtaBodega;
    }

    public BigDecimal getFacVtaSubtotal() {
        return facVtaSubtotal;
    }

    public void setFacVtaSubtotal(BigDecimal facVtaSubtotal) {
        this.facVtaSubtotal = facVtaSubtotal;
    }

    public BigDecimal getFacVtaDescuentoComercial() {
        return facVtaDescuentoComercial;
    }

    public void setFacVtaDescuentoComercial(BigDecimal facVtaDescuentoComercial) {
        this.facVtaDescuentoComercial = facVtaDescuentoComercial;
    }

    public BigDecimal getFacVtaImpuesto() {
        return facVtaImpuesto;
    }

    public void setFacVtaImpuesto(BigDecimal facVtaImpuesto) {
        this.facVtaImpuesto = facVtaImpuesto;
    }

    public BigDecimal getFacVtaTotal() {
        return facVtaTotal;
    }

    public void setFacVtaTotal(BigDecimal facVtaTotal) {
        this.facVtaTotal = facVtaTotal;
    }

    public String getFacVtaTipoDespacho() {
        return facVtaTipoDespacho;
    }

    public void setFacVtaTipoDespacho(String facVtaTipoDespacho) {
        this.facVtaTipoDespacho = facVtaTipoDespacho;
    }

    public String getFacVtaNotaCliente() {
        return facVtaNotaCliente;
    }

    public void setFacVtaNotaCliente(String facVtaNotaCliente) {
        this.facVtaNotaCliente = facVtaNotaCliente;
    }

}
