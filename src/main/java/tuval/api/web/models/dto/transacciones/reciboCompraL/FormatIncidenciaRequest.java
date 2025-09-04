package tuval.api.web.models.dto.transacciones.reciboCompraL;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FormatIncidenciaRequest {

    @JsonProperty("REC_COM_DOCU_ORIGEN")
    private  String recComDocuOrigen;

    @JsonProperty("REC_COM_ID_TIPO")
    private  String recComIdTipo;

    @JsonProperty("REC_COM_ID_PROVEEDOR")
    private  String recComIdProveedor;

    @JsonProperty("REC_COM_FECHA")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private  LocalDate recComFecha;

    public String getRecComDocuOrigen() {
        return recComDocuOrigen;
    }

    public void setRecComDocuOrigen(String recComDocuOrigen) {
        this.recComDocuOrigen = recComDocuOrigen;
    }

    public String getRecComIdTipo() {
        return recComIdTipo;
    }

    public void setRecComIdTipo(String recComIdTipo) {
        this.recComIdTipo = recComIdTipo;
    }

    public String getRecComIdProveedor() {
        return recComIdProveedor;
    }

    public void setRecComIdProveedor(String recComIdProveedor) {
        this.recComIdProveedor = recComIdProveedor;
    }

    public LocalDate getRecComFecha() {
        return recComFecha;
    }

    public void setRecComFecha(LocalDate recComFecha) {
        this.recComFecha = recComFecha;
    }


}
