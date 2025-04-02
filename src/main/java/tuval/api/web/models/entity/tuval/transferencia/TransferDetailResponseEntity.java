package tuval.api.web.models.entity.tuval.transferencia;

import java.util.List;

public class TransferDetailResponseEntity {
    
    private int codRet;
    private String msj;
    private List<TransferDetailEntity> data;
    
    public TransferDetailResponseEntity() {
    }

    public int getCodRet() {
        return codRet;
    }

    public void setCodRet(int codRet) {
        this.codRet = codRet;
    }

    public String getMsj() {
        return msj;
    }

    public void setMsj(String msj) {
        this.msj = msj;
    }

    public List<TransferDetailEntity> getData() {
        return data;
    }

    public void setData(List<TransferDetailEntity> data) {
        this.data = data;
    }
}
