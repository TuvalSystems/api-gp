package tuval.api.web.models.entity.tuval.bodegas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StockWarehouseEntity {

    private String ARTICULO;
    private String DESCRIPCION_ARTICULO;
    private String BODEGA;
    private String UMEDIDA;
    private String STOCK_MANO;
    private String STOCK_RESERVADO;
    private String STOCK_DISPONIBLE;
    private String STOCK_USO;
    private String STOCK_DANADO;
    private String COMPARTIMIENTO;

    @JsonProperty("COMPARTIMIENTO")
    public String getCOMPARTIMIENTO() {
        return COMPARTIMIENTO != null ? COMPARTIMIENTO.trim() : null;
    }

    public void setCOMPARTIMIENTO(String COMPARTIMIENTO) {
        this.COMPARTIMIENTO = COMPARTIMIENTO;
    }

    @JsonProperty("ARTICULO")
    public String getARTICULO() {
        return ARTICULO != null ? ARTICULO.trim() : null;
    }

    public void setARTICULO(String ARTICULO) {
        this.ARTICULO = ARTICULO;
    }

    @JsonProperty("DESCRIPCION_ARTICULO")
    public String getDESCRIPCION_ARTICULO() {
        return DESCRIPCION_ARTICULO != null ? DESCRIPCION_ARTICULO.trim() : null;
    }

    public void setDESCRIPCION_ARTICULO(String DESCRIPCION_ARTICULO) {
        this.DESCRIPCION_ARTICULO = DESCRIPCION_ARTICULO;
    }

    @JsonProperty("BODEGA")
    public String getBODEGA() {
        return BODEGA != null ? BODEGA.trim() : null;
    }

    public void setBODEGA(String bODEGA) {
        this.BODEGA = bODEGA;
    }

    @JsonProperty("UMEDIDA")
    public String getUMEDIDA() {
        return UMEDIDA != null ? UMEDIDA.trim() : null;
    }

    public void setUMEDIDA(String uMEDIDA) {
        this.UMEDIDA = uMEDIDA;
    }

    @JsonProperty("STOCK_MANO")
    public String getSTOCK_MANO() {
        return STOCK_MANO != null ? STOCK_MANO.trim() : null;
    }

    public void setSTOCK_MANO(String sTOCK_MANO) {
        this.STOCK_MANO = sTOCK_MANO;
    }

    @JsonProperty("STOCK_RESERVADO")
    public String getSTOCK_RESERVADO() {
        return STOCK_RESERVADO != null ? STOCK_RESERVADO.trim() : null;
    }

    public void setSTOCK_RESERVADO(String sTOCK_RESERVADO) {
        this.STOCK_RESERVADO = sTOCK_RESERVADO;
    }

    @JsonProperty("STOCK_DISPONIBLE")
    public String getSTOCK_DISPONIBLE() {
        return STOCK_DISPONIBLE != null ? STOCK_DISPONIBLE.trim() : null;
    }

    public void setSTOCK_DISPONIBLE(String sTOCK_DISPONIBLE) {
        this.STOCK_DISPONIBLE = sTOCK_DISPONIBLE;
    }

    @JsonProperty("STOCK_USO")
    public String getSTOCK_USO() {
        return STOCK_USO != null ? STOCK_USO.trim() : null;
    }

    public void setSTOCK_USO(String sTOCK_USO) {
        this.STOCK_USO = sTOCK_USO;
    }

    @JsonProperty("STOCK_DANADO")
    public String getSTOCK_DANADO() {
        return STOCK_DANADO != null ? STOCK_DANADO.trim() : null;
    }

    public void setSTOCK_DANADO(String sTOCK_DANADO) {
        this.STOCK_DANADO = sTOCK_DANADO;
    }

    public StockWarehouseEntity() {
    }

    @Override
    public String toString() {
        return "{" +
                "ARTICULO='" + ARTICULO + '\'' +
                ", DESCRIPCION_ARTICULO='" + DESCRIPCION_ARTICULO + '\'' +
                ", BODEGA='" + BODEGA + '\'' +
                ", UMEDIDA='" + UMEDIDA + '\'' +
                ", STOCK_MANO='" + STOCK_MANO + '\'' +
                ", STOCK_RESERVADO='" + STOCK_RESERVADO + '\'' +
                ", STOCK_DISPONIBLE='" + STOCK_DISPONIBLE + '\'' +
                ", STOCK_USO='" + STOCK_USO + '\'' +
                ", STOCK_DANADO='" + STOCK_DANADO + '\'' +
                '}';
    }

}
