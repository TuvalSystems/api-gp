package tuval.api.web.models.request.transacciones.reciboCompraL;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeaderReceivedLRequest {

    @JsonProperty("REC_COM_DOCU_ORIGEN")
    private  String recComDocuOrigen;

    @JsonProperty("REC_COM_ID_TIPO")
    private  String recComIdTipo;

    @JsonProperty("REC_COM_ID_PROVEEDOR")
    private  String recComIdProveedor;

    @JsonProperty("REC_COM_FECHA")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private  LocalDate recComFecha;

   


}
