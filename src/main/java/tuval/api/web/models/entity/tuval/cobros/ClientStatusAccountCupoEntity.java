package tuval.api.web.models.entity.tuval.cobros;
import java.math.BigDecimal;
/*nuevo dos */
public class ClientStatusAccountCupoEntity 
{
    private String ruc;
    private BigDecimal cupo;
    private BigDecimal cupoPermitido;
    private BigDecimal cupoSuspendido;
    public String getRuc() {
        return ruc;
    }
    public void setRuc(String ruc) {
        this.ruc = ruc;
    }
    public BigDecimal getCupo() {
        return cupo;
    }
    public void setCupo(BigDecimal cupo) {
        this.cupo = cupo;
    }
    public BigDecimal getCupoPermitido() {
        return cupoPermitido;
    }
    public void setCupoPermitido(BigDecimal cupoPermitido) {
        this.cupoPermitido = cupoPermitido;
    }
    public BigDecimal getCupoSuspendido() {
        return cupoSuspendido;
    }
    public void setCupoSuspendido(BigDecimal cupoSuspendido) {
        this.cupoSuspendido = cupoSuspendido;
    }

}
