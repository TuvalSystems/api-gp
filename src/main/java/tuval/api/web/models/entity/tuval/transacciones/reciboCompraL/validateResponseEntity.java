package tuval.api.web.models.entity.tuval.transacciones.reciboCompraL;

public class validateResponseEntity {

    private String codigo;
    private String mensaje;
    private String tipoDocGenerado;
    private String numDocGenerado;

    
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipoDocGenerado() {
        return tipoDocGenerado;
    }

    public void setTipoDocGenerado(String tipoDocGenerado) {
        this.tipoDocGenerado = tipoDocGenerado;
    }

    public String getNumDocGenerado() {
        return numDocGenerado;
    }

    public void setNumDocGenerado(String numDocGenerado) {
        this.numDocGenerado = numDocGenerado;
    }



}
