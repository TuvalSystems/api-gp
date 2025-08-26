package tuval.api.web.models.dto.transacciones.facturacionVenta;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DetailInvoiceRequest {

   @JsonProperty("FAC_VTA_DET_DOCU_ORIGEN")
    private String facVtaDetDocuOrigen;             

    @JsonProperty("FAC_VTA_DET_ID_TIPO")
    private String facVtaDetIdTipo;               

    @JsonProperty("FAC_VTA_DET_LINEA")
    private Long facVtaDetLinea;                     

    @JsonProperty("FAC_VTA_DET_ARTICULO")
    private String facVtaDetArticulo;              

    @JsonProperty("FAC_VTA_DET_DESC_ARTICULO")
    private String facVtaDetDescArticulo;           

    @JsonProperty("FAC_VTA_DET_UNIDAD_MEDIDA")
    private String facVtaDetUnidadMedida;          

    @JsonProperty("FAC_VTA_DET_BODEGA")
    private String facVtadetBodega;                

    @JsonProperty("FAC_VTA_DET_CANTIDAD")
    private BigDecimal facVtaDetCantidad;           

    @JsonProperty("FAC_VTA_DET_COSTO_UNITARIO")
    private BigDecimal facVtaCostoUnitario;          

    @JsonProperty("FAC_VTA_DET_PRECIO_UNITARIO")
    private BigDecimal facVtaDetPrecioUnitario;      

    @JsonProperty("FAC_VTA_DET_DESCUENTO")
    private BigDecimal facVtaDetDescuento;          

    @JsonProperty("FAC_VTA_DET_IMPUESTO_IVA")
    private BigDecimal facVtaDetImpuestoIva;       

    @JsonProperty("FAC_VTA_DET_TOTAL")
    private BigDecimal facVtaDetTotal;

    public String getFacVtaDetDocuOrigen() {
        return facVtaDetDocuOrigen;
    }

    public void setFacVtaDetDocuOrigen(String facVtaDetDocuOrigen) {
        this.facVtaDetDocuOrigen = facVtaDetDocuOrigen;
    }

    public String getFacVtaDetIdTipo() {
        return facVtaDetIdTipo;
    }

    public void setFacVtaDetIdTipo(String facVtaDetIdTipo) {
        this.facVtaDetIdTipo = facVtaDetIdTipo;
    }

    public Long getFacVtaDetLinea() {
        return facVtaDetLinea;
    }

    public void setFacVtaDetLinea(Long facVtaDetLinea) {
        this.facVtaDetLinea = facVtaDetLinea;
    }

    public String getFacVtaDetArticulo() {
        return facVtaDetArticulo;
    }

    public void setFacVtaDetArticulo(String facVtaDetArticulo) {
        this.facVtaDetArticulo = facVtaDetArticulo;
    }

    public String getFacVtaDetDescArticulo() {
        return facVtaDetDescArticulo;
    }

    public void setFacVtaDetDescArticulo(String facVtaDetDescArticulo) {
        this.facVtaDetDescArticulo = facVtaDetDescArticulo;
    }

    public String getFacVtaDetUnidadMedida() {
        return facVtaDetUnidadMedida;
    }

    public void setFacVtaDetUnidadMedida(String facVtaDetUnidadMedida) {
        this.facVtaDetUnidadMedida = facVtaDetUnidadMedida;
    }

    public String getFacVtadetBodega() {
        return facVtadetBodega;
    }

    public void setFacVtadetBodega(String facVtadetBodega) {
        this.facVtadetBodega = facVtadetBodega;
    }

    public BigDecimal getFacVtaDetCantidad() {
        return facVtaDetCantidad;
    }

    public void setFacVtaDetCantidad(BigDecimal facVtaDetCantidad) {
        this.facVtaDetCantidad = facVtaDetCantidad;
    }

    public BigDecimal getFacVtaCostoUnitario() {
        return facVtaCostoUnitario;
    }

    public void setFacVtaCostoUnitario(BigDecimal facVtaCostoUnitario) {
        this.facVtaCostoUnitario = facVtaCostoUnitario;
    }

    public BigDecimal getFacVtaDetPrecioUnitario() {
        return facVtaDetPrecioUnitario;
    }

    public void setFacVtaDetPrecioUnitario(BigDecimal facVtaDetPrecioUnitario) {
        this.facVtaDetPrecioUnitario = facVtaDetPrecioUnitario;
    }

    public BigDecimal getFacVtaDetDescuento() {
        return facVtaDetDescuento;
    }

    public void setFacVtaDetDescuento(BigDecimal facVtaDetDescuento) {
        this.facVtaDetDescuento = facVtaDetDescuento;
    }

    public BigDecimal getFacVtaDetImpuestoIva() {
        return facVtaDetImpuestoIva;
    }

    public void setFacVtaDetImpuestoIva(BigDecimal facVtaDetImpuestoIva) {
        this.facVtaDetImpuestoIva = facVtaDetImpuestoIva;
    }

    public BigDecimal getFacVtaDetTotal() {
        return facVtaDetTotal;
    }

    public void setFacVtaDetTotal(BigDecimal facVtaDetTotal) {
        this.facVtaDetTotal = facVtaDetTotal;
    }  
}
