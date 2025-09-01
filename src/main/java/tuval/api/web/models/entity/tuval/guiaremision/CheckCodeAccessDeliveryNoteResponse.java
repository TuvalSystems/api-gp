package tuval.api.web.models.entity.tuval.guiaremision;

public class CheckCodeAccessDeliveryNoteResponse {

    private String claveAcceso;
    private String documentoOrigen;
    private int codigo;
    private String mensaje;
    private String documentoGenerado;
   
    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public String getDocumentoOrigen() {
        return documentoOrigen;
    }

    public void setDocumentoOrigen(String documentoOrigen) {
        this.documentoOrigen = documentoOrigen;
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

    public String getDocumentoGenerado() {
        return documentoGenerado;
    }

    public void setDocumentoGenerado(String documentoGenerado) {
        this.documentoGenerado = documentoGenerado;
    }

    public CheckCodeAccessDeliveryNoteResponse(String claveAcceso, String documentoOrigen, int codigo, String mensaje, String documentoGenerado) {
        this.claveAcceso = claveAcceso;
        this.documentoOrigen = documentoOrigen;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.documentoGenerado = documentoGenerado;
    }
}
