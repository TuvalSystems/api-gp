package tuval.api.web.models.entity.tuval.guiaremision;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeaderGuideTransferVtaResponse {

    private int codRet; // @codret OUT
    private String mensaje; // @msj OUT
    private int idMovimiento; // @idmov IN/OUT
    private String docGp;

    // public HeaderGuideTransferVtaResponse(int codRet, String mensaje, int idMovimiento, String docGp) {
    //     this.codRet = codRet;
    //     this.mensaje = mensaje;
    //     this.idMovimiento = idMovimiento;
    //     this.docGp = docGp;
    // }
}
