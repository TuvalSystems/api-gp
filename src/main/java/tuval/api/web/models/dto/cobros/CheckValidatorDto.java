package tuval.api.web.models.dto.cobros;


public class CheckValidatorDto {

    private String ruc;
    private double cupoAsignado;
    private double cupoDisponibleGp;
    private int suspendido;
    public String getRuc() {
        return ruc;
    }
    public void setRuc(String ruc) {
        this.ruc = ruc;
    }
    public double getCupoAsignado() {
        return cupoAsignado;
    }
    public void setCupoAsignado(double cupoAsignado) {
        this.cupoAsignado = cupoAsignado;
    }
    public double getCupoDisponibleGp() {
        return cupoDisponibleGp;
    }
    public void setCupoDisponibleGp(double cupoDisponibleGp) {
        this.cupoDisponibleGp = cupoDisponibleGp;
    }
    public int getSuspendido() {
        return suspendido;
    }
    public void setSuspendido(int suspendido) {
        this.suspendido = suspendido;
    }
  

   

}
