package tuval.api.web.cobranzas.cobros.entity.tuval;

import java.math.BigDecimal;

public class ClientStatusAccountHeaderEntity {
    public String identificacion;
    public BigDecimal cupo;
    public String suspendido;
    public String mensaje;
    public String getIdentificacion() {
        return identificacion;
    }
    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }
    public BigDecimal getCupo() {
        return cupo;
    }
    public void setCupo(BigDecimal cupo) {
        this.cupo = cupo;
    }
    public String getSuspendido() {
        return suspendido;
    }
    public void setSuspendido(String suspendido) {
        this.suspendido = suspendido;
    }
    public String getMensaje() {
        return mensaje;
    }
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
