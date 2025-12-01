package tuval.api.web.models.entity.tuval.bodegas;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class PriceItemEntity {
    private String item;
    private String curncyId;
    private String uofm;
    private String idLista;
    private BigDecimal valor;
   
    public String getItem() {
        return item;
    }
    public void setItem(String item) {
        this.item = item;
    }
    public String getCurncyId() {
        return curncyId;
    }
    public void setCurncyId(String curncyId) {
        this.curncyId = curncyId;
    }
    public String getUofm() {
        return uofm;
    }
    public void setUofm(String uofm) {
        this.uofm = uofm;
    }
    public String getIdLista() {
        return idLista;
    }
    public void setIdLista(String idLista) {
        this.idLista = idLista;
    }
 
     public BigDecimal getValor() {
        return valor;
    }
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

}
