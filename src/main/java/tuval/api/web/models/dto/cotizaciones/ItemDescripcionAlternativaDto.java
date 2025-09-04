package tuval.api.web.models.dto.cotizaciones;

public class ItemDescripcionAlternativaDto {
    
    private String tx_itm ;
    private Integer co_reg;
    private String tx_desLar;
    
    public String getTx_itm() {
        return tx_itm;
    }
    public void setTx_itm(String tx_itm) {
        this.tx_itm = tx_itm;
    }
    public Integer getCo_reg() {
        return co_reg;
    }
    public void setCo_reg(Integer co_reg) {
        this.co_reg = co_reg;
    }
    public String getTx_desLar() {
        return tx_desLar;
    }
    public void setTx_desLar(String tx_desLar) {
        this.tx_desLar = tx_desLar;
    }

}
