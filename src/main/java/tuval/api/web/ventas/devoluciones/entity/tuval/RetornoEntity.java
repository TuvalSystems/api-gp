package tuval.api.web.ventas.devoluciones.entity.tuval;

public class RetornoEntity {
    
    private Boolean success;
    private int codigo;
    private String mensaje;
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

   
    
    
    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

   

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public RetornoEntity( Boolean success, int codigo, String mensaje) {
        this.success = success;
        this.codigo = codigo;
        this.mensaje = mensaje;
    }
}
