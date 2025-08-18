package tuval.api.web.models.dto.transacciones.reciboCompraL;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DetailReceivedLRequest {

    @JsonProperty("REC_COM_DOCU_ORIGEN")
    private  String recComDocuOrigen;

    @JsonProperty("REC_COM_ID_TIPO")
    private  Integer recComIdTipo;

    @JsonProperty("REC_COM_ID_ARTICULO")
    private  String recCOmIdArticulo;

    @JsonProperty("REC_COM_DESCRIPCION_ARTICULO")
    private  String recComDescripcionArticulo;

    @JsonProperty("REC_COM_UNIDAD_MEDIDA")
    private  String recComUnidadMedida;

    @JsonProperty("REC_COM_BODEGA")
    private  String recComBodega;

    @JsonProperty("REC_COM_CANTIDAD")
    private  BigDecimal recComCantidad;

    public String getRecComDocuOrigen() {
        return recComDocuOrigen;
    }

    public void setRecComDocuOrigen(String recComDocuOrigen) {
        this.recComDocuOrigen = recComDocuOrigen;
    }

    public Integer getRecComIdTipo() {
        return recComIdTipo;
    }

    public void setRecComIdTipo(Integer recComIdTipo) {
        this.recComIdTipo = recComIdTipo;
    }

    public String getRecCOmIdArticulo() {
        return recCOmIdArticulo;
    }

    public void setRecCOmIdArticulo(String recCOmIdArticulo) {
        this.recCOmIdArticulo = recCOmIdArticulo;
    }

    public String getRecComDescripcionArticulo() {
        return recComDescripcionArticulo;
    }

    public void setRecComDescripcionArticulo(String recComDescripcionArticulo) {
        this.recComDescripcionArticulo = recComDescripcionArticulo;
    }

    public String getRecComUnidadMedida() {
        return recComUnidadMedida;
    }

    public void setRecComUnidadMedida(String recComUnidadMedida) {
        this.recComUnidadMedida = recComUnidadMedida;
    }

    public String getRecComBodega() {
        return recComBodega;
    }

    public void setRecComBodega(String recComBodega) {
        this.recComBodega = recComBodega;
    }

    public BigDecimal getRecComCantidad() {
        return recComCantidad;
    }

    public void setRecComCantidad(BigDecimal recComCantidad) {
        this.recComCantidad = recComCantidad;
    }

}
