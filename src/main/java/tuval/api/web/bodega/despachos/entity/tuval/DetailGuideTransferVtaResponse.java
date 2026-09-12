package tuval.api.web.bodega.despachos.entity.tuval;

public class DetailGuideTransferVtaResponse {
    
    private int codRet;
    private String mensaje;
    private int idMovimiento;
    private String item;
    private int secuencia;

    public int getCodRet() {
        return codRet;
    }

    public void setCodRet(int codRet) {
        this.codRet = codRet;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(int secuencia) {
        this.secuencia = secuencia;
    }


    public DetailGuideTransferVtaResponse(int codRet, String mensaje, int idMovimiento, String item, int secuencia) {
        this.codRet = codRet;
        this.mensaje = mensaje;
        this.idMovimiento = idMovimiento;
        this.item = item;
        this.secuencia = secuencia;
    }
}
