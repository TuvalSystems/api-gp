package tuval.api.web.models.dto.transacciones.facturacionVenta;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseConsultInvoiceRequest {

    @JsonProperty("documentoOrigen")
    public String documento;

    @JsonProperty("tipoDocumento")
    public String tipoDocumento;

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
}
