package tuval.api.web.models.entity.transferencia;

import java.util.List;

public class TransferHeaderResponseEntity {
    private int codRet;
    private String msj;
    private List<TransferHeaderEntity> data;

    public TransferHeaderResponseEntity() {
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

    public List<TransferHeaderEntity> getData() {
        return data;
    }

    public void setData(List<TransferHeaderEntity> data) {
        this.data = data;
    }

}
