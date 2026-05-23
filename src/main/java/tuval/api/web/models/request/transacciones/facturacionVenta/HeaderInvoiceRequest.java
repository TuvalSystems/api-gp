package tuval.api.web.models.request.transacciones.facturacionVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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


    private String userIdCreator;
    private String condIdOriginal;
    private String userApprovalId;
    private Long pedidoId;

    

}
