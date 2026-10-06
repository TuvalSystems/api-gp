package tuval.api.web.ventas.facturacion.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    private String descripcionOrginal;
    private BigDecimal valorOrginal;
    private BigDecimal descuentoOrginal;
    private BigDecimal descuento;
    private Long pedidoId;                     
    private Long detalleId;                     
    private String categoria;
    private String linea;
    private String clase;
    private String configuracionItemL;


    
}
