package tuval.api.web.models.entity.transferencia;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferHeaderEntity {

    @JsonProperty("numdoc")
    private String numdoc;
    @JsonProperty("codmotivo")
    private String codmotivo;
    @JsonProperty("motivo")
    private String motivo;
    @JsonProperty("origen")
    private String origen;
    @JsonProperty("destino")
    private String destino;
    @JsonProperty("fecha")
    private Date fecha;

    public TransferHeaderEntity() {
    }

   public String getNumdoc() {
       return numdoc;
   }

    public void setNumdoc(String numdoc) {
        this.numdoc = numdoc;
    }


    public String getCodmotivo() {
        return codmotivo;
    }

    public void setCodmotivo(String codmotivo) {  
        this.codmotivo = codmotivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {  
        this.motivo = motivo;
    }

    public String getOrigen() {   
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }
    
    public String getDestino() {
        return destino;
    }
    
    public void setDestino(String destino) {
        this.destino = destino;
    }
    
    public Date getFecha(){
        return fecha;
    }

    public void setFecha(Date fecha){
        this.fecha = fecha;
    }


    @Override

    public String toString() {
        return "TransferHeaderEntity{" +
                "numdoc='" + numdoc + '\'' +
                ", codmotivo='" + codmotivo + '\'' +
                ", motivo='" + motivo + '\'' +
                ", origen='" + origen + '\'' +
                ", destino='" + destino + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}
